package chess.server.protocol.payload;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;

import java.util.List;

public class MovePayload {

    // Hàng bắt đầu của nước đi.
    private int fromX;

    // Cột bắt đầu của nước đi.
    private int fromY;

    // Hàng đích của nước đi.
    private int toX;

    // Cột đích của nước đi.
    private int toY;

    // Quân được di chuyển, server tự xác định từ board.
    private Piece piece;

    // Quân bị ăn nếu nước đi có ăn quân.
    private Piece capturedPiece;

    // Danh sách nước đi có thể, dùng cho POSSIBLE_MOVES.
    private List<Position> moves;

    // Sau nước đi này có tới lượt đỏ hay không.
    private boolean redTurn;

    // Bên đang bị chiếu nếu có.
    private String checkSide;

    // Thời gian còn lại của bên đỏ.
    private long redTime;

    // Thời gian còn lại của bên đen.
    private long blackTime;

    // Lý do nước đi bị từ chối.
    private String reason;

    // Message mô tả ngắn cho client hiển thị nếu cần.
    private String message;

    public MovePayload() {
    }

    public int getFromX() {
        return fromX;
    }

    public void setFromX(int fromX) {
        this.fromX = fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public void setFromY(int fromY) {
        this.fromY = fromY;
    }

    public int getToX() {
        return toX;
    }

    public void setToX(int toX) {
        this.toX = toX;
    }

    public int getToY() {
        return toY;
    }

    public void setToY(int toY) {
        this.toY = toY;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public void setCapturedPiece(Piece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }

    public List<Position> getMoves() {
        return moves;
    }

    public void setMoves(List<Position> moves) {
        this.moves = moves;
    }

    public boolean isRedTurn() {
        return redTurn;
    }

    public void setRedTurn(boolean redTurn) {
        this.redTurn = redTurn;
    }

    public String getCheckSide() {
        return checkSide;
    }

    public void setCheckSide(String checkSide) {
        this.checkSide = checkSide;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
