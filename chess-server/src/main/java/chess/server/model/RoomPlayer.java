package chess.server.model;

import chess.server.model.Definition.PlayerRole;
import chess.server.model.Definition.Side;

public class RoomPlayer {

    // Id của WebSocket session, dùng để xác định kết nối nào thuộc người chơi này.
    private String sessionId;

    // Vai trò trong phòng: người tạo phòng hoặc người join.
    private PlayerRole role;

    // Trạng thái sẵn sàng trong lobby.
    private boolean isReady;

    // Cho biết kết nối WebSocket của người chơi còn hoạt động hay không.
    private boolean isConnected;

    private Side playerSide;

    public RoomPlayer(String sessionId, PlayerRole role) {
        this.sessionId = sessionId;
        this.role = role;
        this.isReady = false;
        this.isConnected = true;
    }

    public String getSessionId() {
        return sessionId;
    }

    public PlayerRole getRole() {
        return role;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public Side getPlayerSide() {
        return playerSide;
    }

    public void setPlayerSide(Side newSide) {
        playerSide = newSide;
    }
}
