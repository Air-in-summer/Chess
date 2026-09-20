package chess.server.service;

import chess.server.exception.RoomException;
import chess.server.model.Definition.GameResult;
import chess.server.model.Definition.Piece;
import chess.server.model.Definition.Side;
import chess.server.model.Game;
import chess.server.model.Room;
import chess.server.model.RoomPlayer;
import chess.server.protocol.ErrorCode;
import chess.server.protocol.MessageType;
import chess.server.protocol.ServerMessage;
import chess.server.protocol.payload.GamePayload;
import chess.server.protocol.payload.MovePayload;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Class GameService xử lý vòng đời và luồng thực thi của một ván cờ.
 * Nhiệm vụ chính bao gồm:
 * - Khởi tạo ván đấu mới, phân phối màu cờ và trạng thái bàn cờ ban đầu.
 * - Tiếp nhận, phối hợp với RuleService để xác thực tính hợp lệ của từng nước đi, sau đó cập nhật bàn cờ.
 * - Quản lý hệ thống đồng hồ đếm ngược (timer) độc lập cho từng lượt đi thông qua ScheduledExecutorService.
 * - Xử lý mọi kịch bản kết thúc ván đấu (chiếu hết, hết giờ, đầu hàng) và kích hoạt việc trả kết quả về cho Client.
 */
@Service
public class GameService {

    private final SessionService sessionService;
    private final RoomService roomService;

    // Hàng đợi Min-Heap cho bộ đếm giờ
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> roomTimers = new ConcurrentHashMap<>();

    public GameService(SessionService sessionService, @Lazy RoomService roomService) {
        this.sessionService = sessionService;
        this.roomService = roomService;
    }

    /**
     * Hủy bỏ bộ đếm giờ (timer) đang chạy của một phòng (nếu có).
     * Được gọi khi ván đấu kết thúc sớm (chiếu hết, đầu hàng, có người thoát) để tránh timer tiếp tục kích hoạt.
     */
    public void cancelTimeout(String roomId) {
        ScheduledFuture<?> existing = roomTimers.remove(roomId);
        if (existing != null) {
            existing.cancel(false);
        }
    }

    /**
     * Xử lý hành động Đầu hàng (Surrender) của người chơi.
     * Cập nhật kết quả thua cho người gửi yêu cầu, phát thông báo GAME_OVER cho cả phòng, 
     * nhưng tuyệt đối không kích người chơi ra khỏi phòng (để họ có thể xem lại biên bản bàn cờ).
     */
    public void surrender(String sessionId) {
        Room room = roomService.findRoomBySessionId(sessionId);
        if (room == null || room.getStatus() != chess.server.model.Definition.RoomStatus.PLAYING) return;
        
        cancelTimeout(room.getRoomId());
        
        RoomPlayer loser = room.findPlayer(sessionId);
        RoomPlayer winner = null;
        for (RoomPlayer rp : room.getPlayers()) {
            if (!rp.getSessionId().equals(sessionId)) {
                winner = rp;
                break;
            }
        }
        
        if (winner != null && room.getGame() != null) {
            GameResult result = winner.getPlayerSide() == Side.RED ? GameResult.RED_WIN : GameResult.BLACK_WIN;
            room.getGame().endGame(result);
            
            ServerMessage msg = new ServerMessage(MessageType.GAME_OVER);
            GamePayload gp = new GamePayload();
            gp.setRoomId(room.getRoomId());
            gp.setWinner(winner.getSessionId());
            gp.setLoser(sessionId);
            gp.setReason("Đối thủ đã đầu hàng");
            gp.setResult(result);
            msg.setGamePayload(gp);
            sessionService.broadcastToRoom(room, msg);
        }
        
        room.finishGame();
    }

    /**
     * Lên lịch đếm ngược thời gian cho lượt đi hiện tại của ván đấu.
     * Sử dụng Min-Heap (ScheduledExecutorService) để kích hoạt ngầm (ngay sau khoảng thời gian còn lại của người chơi).
     */
    private void scheduleTimeout(Room room) {
        cancelTimeout(room.getRoomId());

        Game game = room.getGame();
        if (game == null || game.isGameOver()) return;

        long remainingTime = game.isRedTurn() ? game.getRedTimeMillis() : game.getBlackTimeMillis();
        
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            handleTimeout(room);
        }, remainingTime, TimeUnit.MILLISECONDS);

        roomTimers.put(room.getRoomId(), future);
    }

    /**
     * Hàm được gọi tự động (callback) từ luồng background khi timer đếm ngược kết thúc (fire).
     * Tiến hành kiểm tra thời gian thực tế xem đã thực sự hết chưa. Nếu hết, xử thua người chơi đang tới lượt,
     * phát thông báo GAME_OVER (hết giờ) cho toàn phòng và dọn dẹp ván đấu.
     */
    private void handleTimeout(Room room) {
        Game game = room.getGame();
        if (game == null || game.isGameOver()) return;

        long elapsed = System.currentTimeMillis() - game.getTurnStartTime();
        long remaining = game.isRedTurn() ? game.getRedTimeMillis() : game.getBlackTimeMillis();
        
        // Nếu thực sự hết giờ
        if (elapsed >= remaining) {
            boolean isRedTurn = game.isRedTurn();
            GameResult result = isRedTurn ? GameResult.BLACK_WIN : GameResult.RED_WIN;
            game.endGame(result);
            
            RoomPlayer winner = findPlayerBySide(room, isRedTurn ? Side.BLACK : Side.RED);
            RoomPlayer loser = findPlayerBySide(room, isRedTurn ? Side.RED : Side.BLACK);

            ServerMessage msg = new ServerMessage(MessageType.GAME_OVER);
            GamePayload gp = new GamePayload();
            gp.setRoomId(room.getRoomId());
            gp.setWinner(winner != null ? winner.getSessionId() : null);
            gp.setLoser(loser != null ? loser.getSessionId() : null);
            gp.setReason("Hết giờ");
            gp.setResult(result);
            msg.setGamePayload(gp);
            
            sessionService.broadcastToRoom(room, msg);
            room.finishGame();
            roomTimers.remove(room.getRoomId());
        }
    }

    /**
     * Tiện ích chuyển đổi Enum Side (RED/BLACK) sang chuỗi màu cờ ("Red"/"Black") 
     * để tương thích với cấu trúc đầu vào của Rule Engine (RuleService).
     */
    private String sideToColorString(Side side) {
        return side == Side.RED ? "Red" : "Black";
    }

    /**
     * Tìm kiếm thông tin người chơi trong một phòng dựa vào phe (Đỏ/Đen) của họ.
     */
    private RoomPlayer findPlayerBySide(Room room, Side side) {
        for (RoomPlayer player : room.getPlayers()) {
            if (player.getPlayerSide() == side) {
                return player;
            }
        }
        return null;
    }

    /**
     * Tạo một bản sao (Deep Copy) của mảng 2D bàn cờ hiện tại.
     * Dùng để thử mô phỏng các nước đi (vd: xem đi thử thì Tướng có bị chiếu không) mà không làm hỏng dữ liệu gốc thật.
     */
    private Piece[][] copyBoard(Piece[][] board) {
        Piece[][] copy = new Piece[10][9];
        for (int i = 0; i < 10; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 9);
        }
        return copy;
    }

    /**
     * Bắt đầu một ván đấu mới.
     * Chức năng:
     * - Phân ngẫu nhiên phe Đỏ/Đen cho 2 người chơi.
     * - Tạo thông báo START_GAME chứa dữ liệu khởi tạo (bàn cờ gốc, lượt đi, thời gian).
     * - Broadcast thông báo cho cả 2 client và kích hoạt timer cho lượt đầu tiên (Đỏ đi trước).
     */
    public void startGame(Room room) {
        if (!room.startGame()) {
            return;
        }

        int rand = ThreadLocalRandom.current().nextInt(2);
        room.getPlayers().get(rand).setPlayerSide(Side.RED);
        room.getPlayers().get(1 - rand).setPlayerSide(Side.BLACK);

        Game game = room.getGame();

        RoomPlayer redPlayer = findPlayerBySide(room, Side.RED);
        RoomPlayer blackPlayer = findPlayerBySide(room, Side.BLACK);

        if (redPlayer == null || blackPlayer == null) return;

        ServerMessage msg = new ServerMessage(MessageType.START_GAME);
        GamePayload gp = new GamePayload();
        gp.setRoomId(room.getRoomId());
        gp.setRedPlayerId(redPlayer.getSessionId());
        gp.setBlackPlayerId(blackPlayer.getSessionId());
        gp.setBoard(game.getBoard());
        gp.setRedTurn(game.isRedTurn());
        gp.setRedTime(game.getRedTimeMillis());
        gp.setBlackTime(game.getBlackTimeMillis());
        msg.setGamePayload(gp);

        sessionService.broadcastToRoom(room, msg);
        
        scheduleTimeout(room);
    }

    /**
     * Hàm lõi xử lý mọi nước đi (move) được gửi lên từ Client.
     * Quá trình xử lý diễn ra theo trình tự các bước nghiêm ngặt sau đây:
     * 
     * Bước 1: Khởi tạo & Kiểm tra cơ bản
     *         - Bỏ qua nếu phòng không tồn tại, ván đấu chưa tạo hoặc đã kết thúc.
     *         - Xác định người chơi gửi lệnh và phe (Đỏ/Đen) của họ.
     * 
     * Bước 2: Chống gian lận (Lượt đi)
     *         - Kiểm tra xem có đúng là đang tới lượt của phe người chơi đánh hay không.
     * 
     * Bước 3: Validate tọa độ & Quân cờ gốc
     *         - Tọa độ fromX, fromY, toX, toY phải nằm trọn trong mảng (10x9).
     *         - Phải có quân cờ tồn tại ở điểm xuất phát (from).
     *         - Chống đi hộ: Cấm đánh quân cờ của đối phương.
     * 
     * Bước 4: Áp dụng Rule Engine (Kiểm tra cách đi)
     *         - Gọi RuleService.getPieceBehaviour() để kiểm tra xem quân cờ đó đi tới điểm đích
     *           có đúng luật hay không (Mã có bị cản không, Xe có đi thẳng không, Pháo có ngòi không...).
     * 
     * Bước 5: Mô phỏng nước đi (Shadow Testing)
     *         - Copy bàn cờ ra một bản nháp, đi thử nước cờ đó trên bản nháp.
     *         - Nếu đi xong mà Tướng phe mình bị đối thủ chiếu -> Nước đi tự sát (Cấm).
     *         - Nếu đi xong mà khiến 2 Tướng đối mặt (không có quân chắn) -> Phạm luật (Cấm).
     * 
     * Bước 6: Áp dụng nước đi thật (Apply Move)
     *         - Lấy quân cờ bị ăn (nếu có).
     *         - Cập nhật vị trí mới trên mảng thật, xóa vị trí cũ.
     *         - Chuyển lượt sang đối phương và trừ thời gian.
     * 
     * Bước 7: Kiểm tra hết giờ (Timeout check ngay lúc đánh)
     *         - Nếu người chơi đi quá chậm và bị âm thời gian, lập tức xử thua và kết thúc ván.
     * 
     * Bước 8: Kiểm tra trạng thái của Đối thủ
     *         - Dùng RuleService xem đối thủ có bị chiếu (Check) hay không.
     *         - Nếu bị chiếu, kiểm tra xem đối thủ còn bất kỳ nước đi nào hợp lệ để đỡ không (HasAnyLegalMoves).
     *         - Nếu không còn nước nào -> Đánh dấu là Chiếu Hết (Checkmate).
     * 
     * Bước 9: Đồng bộ và Kết thúc (Broadcast)
     *         - Luôn gửi tín hiệu MOVE_APPLIED trước để Client cập nhật hình ảnh nước đi cuối.
     *         - Nếu là Chiếu Hết (Checkmate) -> Hủy timer, xử thắng thua, gửi tín hiệu GAME_OVER và dọn dẹp game.
     *         - Nếu ván đấu còn tiếp diễn -> Kích hoạt timer cho lượt của đối thủ.
     * 
     * @param sessionId ID người thực hiện nước đi
     * @param room Phòng diễn ra ván đấu
     * @param payload Dữ liệu nước đi (từ tọa độ x,y đến x,y)
     */
    public void handleMove(String sessionId, Room room, MovePayload payload) {
        // Bước 1: Khởi tạo & Kiểm tra cơ bản
        if (room == null || room.getGame() == null || room.getGame().isGameOver()) return;

        Game game = room.getGame();
        RoomPlayer player = room.findPlayer(sessionId);
        if (player == null) return;

        Side side = player.getPlayerSide();
        boolean isRed = (side == Side.RED);

        // Bước 2: Chống gian lận (Lượt đi)
        if ((isRed && !game.isRedTurn()) || (!isRed && game.isRedTurn())) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.NOT_YOUR_TURN, "Chưa đến lượt của bạn.");
        }

        // Bước 3: Validate tọa độ & Quân cờ gốc
        int fromX = payload.getFromX();
        int fromY = payload.getFromY();
        int toX = payload.getToX();
        int toY = payload.getToY();

        if (fromX < 0 || fromX > 9 || fromY < 0 || fromY > 8 || toX < 0 || toX > 9 || toY < 0 || toY > 8) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.INVALID_POSITION, "Vị trí không hợp lệ.");
        }

        Piece piece = game.getPiece(fromX, fromY);
        if (piece == Piece.EMPTY) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.EMPTY_FROM_POSITION, "Không có quân cờ tại vị trí đã chọn.");
        }

        String pieceName = piece.name();
        if (isRed && !pieceName.startsWith("r")) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.NOT_YOUR_PIECE, "Bạn chỉ có thể di chuyển quân đỏ.");
        }
        if (!isRed && !pieceName.startsWith("b")) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.NOT_YOUR_PIECE, "Bạn chỉ có thể di chuyển quân đen.");
        }

        // Bước 4: Áp dụng Rule Engine (Kiểm tra cách đi)
        boolean isValidMove = RuleService.getPieceBehaviour(piece).checkMovement(fromX, fromY, toX, toY, game.getBoard());
        if (!isValidMove) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.INVALID_MOVE, "Nước đi không hợp lệ.");
        }

        // Bước 5: Mô phỏng nước đi (Shadow Testing)
        String myColor = sideToColorString(side);
        Piece[][] copy = copyBoard(game.getBoard());
        copy[toX][toY] = piece;
        copy[fromX][fromY] = Piece.EMPTY;

        if (RuleService.isGeneralInChecked(copy, myColor)) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.INVALID_MOVE, "Nước đi này khiến Tướng bị chiếu.");
        }

        if (RuleService.isGeneralFaceToFace(copy)) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.INVALID_MOVE, "Hai Tướng không được đối mặt.");
        }

        // Bước 6: Áp dụng nước đi thật (Apply Move)
        Piece capturedPiece = game.getPiece(toX, toY);
        game.setPiece(toX, toY, piece);
        game.setPiece(fromX, fromY, Piece.EMPTY);
        game.switchTurn();

        // Bước 7: Kiểm tra hết giờ (Timeout check ngay lúc đánh)
        if ((isRed && game.getRedTimeMillis() <= 0) || (!isRed && game.getBlackTimeMillis() <= 0)) {
            GameResult result = isRed ? GameResult.BLACK_WIN : GameResult.RED_WIN;
            game.endGame(result);

            RoomPlayer winner = findPlayerBySide(room, isRed ? Side.BLACK : Side.RED);

            ServerMessage msg = new ServerMessage(MessageType.GAME_OVER);
            GamePayload gp = new GamePayload();
            gp.setRoomId(room.getRoomId());
            gp.setWinner(winner != null ? winner.getSessionId() : null);
            gp.setLoser(sessionId);
            gp.setReason("Hết giờ");
            gp.setResult(result);
            msg.setGamePayload(gp);
            sessionService.broadcastToRoom(room, msg);

            room.finishGame();
            return;
        }

        // Bước 8: Kiểm tra trạng thái của Đối thủ
        String opponentColor = isRed ? "Black" : "Red";
        String checkSide = null;
        boolean isCheckmate = false;

        if (RuleService.isGeneralInChecked(game.getBoard(), opponentColor)) {
            if (!RuleService.HasAnyLegalMoves(game.getBoard(), opponentColor)) {
                isCheckmate = true;
            } else {
                checkSide = opponentColor;
            }
        }

        // Bước 9: Đồng bộ và Kết thúc (Broadcast)
        // 1. LUÔN GỬI MOVE_APPLIED CHO NƯỚC ĐI VỪA RỒI
        ServerMessage moveMsg = new ServerMessage(MessageType.MOVE_APPLIED);
        MovePayload mp = new MovePayload();
        mp.setFromX(fromX);
        mp.setFromY(fromY);
        mp.setToX(toX);
        mp.setToY(toY);
        mp.setPiece(piece);
        mp.setCapturedPiece(capturedPiece);
        mp.setRedTurn(game.isRedTurn());
        mp.setCheckSide(isCheckmate ? opponentColor : checkSide);
        mp.setRedTime(game.getRedTimeMillis());
        mp.setBlackTime(game.getBlackTimeMillis());
        moveMsg.setMovePayload(mp);
        sessionService.broadcastToRoom(room, moveMsg);

        // 2. NẾU CHIẾU HẾT THÌ GỬI TIẾP GAME_OVER VÀ KẾT THÚC
        if (isCheckmate) {
            cancelTimeout(room.getRoomId());
            GameResult result = isRed ? GameResult.RED_WIN : GameResult.BLACK_WIN;
            game.endGame(result);

            RoomPlayer winner = findPlayerBySide(room, isRed ? Side.RED : Side.BLACK);
            RoomPlayer loser = findPlayerBySide(room, isRed ? Side.BLACK : Side.RED);

            ServerMessage overMsg = new ServerMessage(MessageType.GAME_OVER);
            GamePayload gp = new GamePayload();
            gp.setRoomId(room.getRoomId());
            gp.setWinner(winner != null ? winner.getSessionId() : null);
            gp.setLoser(loser != null ? loser.getSessionId() : null);
            gp.setReason("Chiếu hết");
            gp.setResult(result);
            overMsg.setGamePayload(gp);
            sessionService.broadcastToRoom(room, overMsg);

            room.finishGame();
            return;
        }

        scheduleTimeout(room);
    }
}
