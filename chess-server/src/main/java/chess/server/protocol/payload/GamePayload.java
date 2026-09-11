package chess.server.protocol.payload;

import chess.server.model.Definition.GameResult;
import chess.server.model.Definition.Piece;

public class GamePayload {

    // Id phòng chứa ván cờ.
    private String roomId;

    // Session id của người cầm đỏ.
    private String redPlayerId;

    // Session id của người cầm đen.
    private String blackPlayerId;

    // Trạng thái bàn cờ hiện tại.
    private Piece[][] board;

    // Có đang tới lượt đỏ hay không.
    private boolean redTurn;

    // Thời gian còn lại của bên đỏ.
    private long redTime;

    // Thời gian còn lại của bên đen.
    private long blackTime;

    // Bên đang bị chiếu nếu có.
    private String checkSide;

    // Bên thắng khi game kết thúc.
    private String winner;

    // Bên thua khi game kết thúc.
    private String loser;

    // Kết quả nội bộ của game.
    private GameResult result;

    // Lý do game kết thúc hoặc lý do client cần quay về màn hình đầu.
    private String reason;

    public GamePayload() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRedPlayerId() {
        return redPlayerId;
    }

    public void setRedPlayerId(String redPlayerId) {
        this.redPlayerId = redPlayerId;
    }

    public String getBlackPlayerId() {
        return blackPlayerId;
    }

    public void setBlackPlayerId(String blackPlayerId) {
        this.blackPlayerId = blackPlayerId;
    }

    public Piece[][] getBoard() {
        return board;
    }

    public void setBoard(Piece[][] board) {
        this.board = board;
    }

    public boolean isRedTurn() {
        return redTurn;
    }

    public void setRedTurn(boolean redTurn) {
        this.redTurn = redTurn;
    }

    public long getRedTime() {
        return redTime;
    }

    public void setRedTime(long redTime) {
        this.redTime = redTime;
    }

    public long getBlackTime() {
        return blackTime;
    }

    public void setBlackTime(long blackTime) {
        this.blackTime = blackTime;
    }

    public String getCheckSide() {
        return checkSide;
    }

    public void setCheckSide(String checkSide) {
        this.checkSide = checkSide;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getLoser() {
        return loser;
    }

    public void setLoser(String loser) {
        this.loser = loser;
    }

    public GameResult getResult() {
        return result;
    }

    public void setResult(GameResult result) {
        this.result = result;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
