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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class GameService {

    private final SessionService sessionService;
    private final RoomService roomService;

    public GameService(SessionService sessionService, @Lazy RoomService roomService) {
        this.sessionService = sessionService;
        this.roomService = roomService;
    }

    private String sideToColorString(Side side) {
        return side == Side.RED ? "Red" : "Black";
    }

    private RoomPlayer findPlayerBySide(Room room, Side side) {
        for (RoomPlayer player : room.getPlayers()) {
            if (player.getPlayerSide() == side) {
                return player;
            }
        }
        return null;
    }

    private Piece[][] copyBoard(Piece[][] board) {
        Piece[][] copy = new Piece[10][9];
        for (int i = 0; i < 10; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 9);
        }
        return copy;
    }

    public void startGame(Room room) {
        if (!room.startGame()) {
            return;
        }

        int rand = ThreadLocalRandom.current().nextInt(2);
        room.getPlayers().get(rand).setPlayerSide(Side.RED);
        room.getPlayers().get(1 - rand).setPlayerSide(Side.BLACK);

        Game game = room.getGame();
        game.setTurnStartTime(System.currentTimeMillis());

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
    }

    public void handleMove(String sessionId, Room room, MovePayload payload) {
        if (room == null || room.getGame() == null || room.getGame().isGameOver()) return;

        Game game = room.getGame();
        RoomPlayer player = room.findPlayer(sessionId);
        if (player == null) return;

        Side side = player.getPlayerSide();
        boolean isRed = (side == Side.RED);

        if ((isRed && !game.isRedTurn()) || (!isRed && game.isRedTurn())) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.NOT_YOUR_TURN, "Chưa đến lượt của bạn.");
        }

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

        boolean isValidMove = RuleService.getPieceBehaviour(piece).checkMovement(fromX, fromY, toX, toY, game.getBoard());
        if (!isValidMove) {
            throw new RoomException(MessageType.MOVE_REJECTED, ErrorCode.INVALID_MOVE, "Nước đi không hợp lệ.");
        }

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

        long elapsed = System.currentTimeMillis() - game.getTurnStartTime();
        if (isRed) {
            game.setRedTimeMillis(game.getRedTimeMillis() - elapsed);
        } else {
            game.setBlackTimeMillis(game.getBlackTimeMillis() - elapsed);
        }

        if ((isRed && game.getRedTimeMillis() <= 0) || (!isRed && game.getBlackTimeMillis() <= 0)) {
            game.setGameOver(true);
            GameResult result = isRed ? GameResult.BLACK_WIN : GameResult.RED_WIN;
            game.setResult(result);
            
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

        Piece capturedPiece = game.getPiece(toX, toY);
        game.setPiece(toX, toY, piece);
        game.setPiece(fromX, fromY, Piece.EMPTY);

        game.setRedTurn(!game.isRedTurn());
        game.setTurnStartTime(System.currentTimeMillis());

        String opponentColor = isRed ? "Black" : "Red";
        String checkSide = null;
        if (RuleService.isGeneralInChecked(game.getBoard(), opponentColor)) {
            if (!RuleService.HasAnyLegalMoves(game.getBoard(), opponentColor)) {
                game.setGameOver(true);
                GameResult result = isRed ? GameResult.RED_WIN : GameResult.BLACK_WIN;
                game.setResult(result);

                RoomPlayer winner = findPlayerBySide(room, isRed ? Side.RED : Side.BLACK);
                RoomPlayer loser = findPlayerBySide(room, isRed ? Side.BLACK : Side.RED);

                ServerMessage msg = new ServerMessage(MessageType.GAME_OVER);
                GamePayload gp = new GamePayload();
                gp.setRoomId(room.getRoomId());
                gp.setWinner(winner != null ? winner.getSessionId() : null);
                gp.setLoser(loser != null ? loser.getSessionId() : null);
                gp.setReason("Chiếu hết");
                gp.setResult(result);
                msg.setGamePayload(gp);
                sessionService.broadcastToRoom(room, msg);

                room.finishGame();
                return;
            } else {
                checkSide = opponentColor;
            }
        }

        ServerMessage msg = new ServerMessage(MessageType.MOVE_APPLIED);
        MovePayload mp = new MovePayload();
        mp.setFromX(fromX);
        mp.setFromY(fromY);
        mp.setToX(toX);
        mp.setToY(toY);
        mp.setPiece(piece);
        mp.setCapturedPiece(capturedPiece);
        mp.setRedTurn(game.isRedTurn());
        mp.setCheckSide(checkSide);
        mp.setRedTime(game.getRedTimeMillis());
        mp.setBlackTime(game.getBlackTimeMillis());
        msg.setMovePayload(mp);
        sessionService.broadcastToRoom(room, msg);
    }
}
