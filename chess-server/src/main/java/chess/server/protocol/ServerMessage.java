package chess.server.protocol;

import chess.server.protocol.payload.GamePayload;
import chess.server.protocol.payload.MovePayload;
import chess.server.protocol.payload.RoomPayload;

public class ServerMessage {

    // Loại message server gửi xuống client.
    private MessageType type;

    // Payload liên quan tới phòng, lobby, join, leave.
    private RoomPayload roomPayload;

    // Payload liên quan tới nước đi và possible moves.
    private MovePayload movePayload;

    // Payload liên quan tới trạng thái game, bàn cờ, timer, kết quả.
    private GamePayload gamePayload;

    // Mã lỗi cố định để frontend xử lý theo case.
    private ErrorCode errorCode;

    // Message mô tả ngắn để frontend hiển thị nếu cần.
    private String message;

    public ServerMessage() {
    }

    public ServerMessage(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public RoomPayload getRoomPayload() {
        return roomPayload;
    }

    public void setRoomPayload(RoomPayload roomPayload) {
        this.roomPayload = roomPayload;
    }

    public MovePayload getMovePayload() {
        return movePayload;
    }

    public void setMovePayload(MovePayload movePayload) {
        this.movePayload = movePayload;
    }

    public GamePayload getGamePayload() {
        return gamePayload;
    }

    public void setGamePayload(GamePayload gamePayload) {
        this.gamePayload = gamePayload;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
