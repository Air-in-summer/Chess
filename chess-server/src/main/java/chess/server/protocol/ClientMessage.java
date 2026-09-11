package chess.server.protocol;

import chess.server.protocol.payload.MovePayload;
import chess.server.protocol.payload.RoomPayload;

public class ClientMessage {

    // Loại message client gửi lên server.
    private MessageType type;

    // Payload liên quan tới phòng: JOIN_ROOM dùng roomId.
    private RoomPayload roomPayload;

    // Payload liên quan tới nước đi: MOVE dùng fromX, fromY, toX, toY.
    private MovePayload movePayload;

    public ClientMessage() {
    }

    public ClientMessage(MessageType type) {
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
}
