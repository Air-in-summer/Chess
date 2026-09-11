package chess.server.protocol.payload;

import chess.server.model.Definition.PlayerRole;

public class PlayerPayload {

    // Id của WebSocket session đại diện cho người chơi.
    private String sessionId;

    // Vai trò của người chơi trong phòng: owner hoặc guest.
    private PlayerRole role;

    // Người chơi đã bấm sẵn sàng trong phòng chờ hay chưa.
    private boolean ready;

    // Kết nối WebSocket của người chơi còn hoạt động hay không.
    private boolean connected;

    public PlayerPayload() {
    }

    public PlayerPayload(String sessionId, PlayerRole role, boolean ready, boolean connected) {
        this.sessionId = sessionId;
        this.role = role;
        this.ready = ready;
        this.connected = connected;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
