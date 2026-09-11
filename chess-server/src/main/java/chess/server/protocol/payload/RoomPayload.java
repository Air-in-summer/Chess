package chess.server.protocol.payload;

import chess.server.model.Definition.PlayerRole;
import chess.server.model.Definition.RoomStatus;

import java.util.List;

public class RoomPayload {

    // Id phòng dùng để tạo, join và đồng bộ trạng thái phòng.
    private String roomId;

    // Vai trò của người nhận message trong phòng.
    private PlayerRole role;

    // Trạng thái hiện tại của phòng.
    private RoomStatus roomStatus;

    // Danh sách người chơi hiện có trong phòng.
    private List<PlayerPayload> players;

    // Người chơi liên quan tới event PLAYER_JOINED.
    private PlayerPayload player;

    // Session id của người vừa rời phòng.
    private String leftPlayerId;

    // Session id của người vừa đổi trạng thái ready.
    private String playerId;

    // Trạng thái ready mới của người chơi.
    private boolean ready;

    // Message mô tả ngắn cho client hiển thị nếu cần.
    private String message;

    public RoomPayload() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(RoomStatus roomStatus) {
        this.roomStatus = roomStatus;
    }

    public List<PlayerPayload> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerPayload> players) {
        this.players = players;
    }

    public PlayerPayload getPlayer() {
        return player;
    }

    public void setPlayer(PlayerPayload player) {
        this.player = player;
    }

    public String getLeftPlayerId() {
        return leftPlayerId;
    }

    public void setLeftPlayerId(String leftPlayerId) {
        this.leftPlayerId = leftPlayerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
