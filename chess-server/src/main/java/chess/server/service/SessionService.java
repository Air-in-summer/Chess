package chess.server.service;

import chess.server.model.Room;
import chess.server.model.RoomPlayer;
import chess.server.protocol.ServerMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    // Giữ toàn bộ WebSocket session đang mở, key = session id.
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public SessionService() {
        this.objectMapper = new ObjectMapper();
        // Không serialize field null để JSON gọn hơn.
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    // Lưu session khi client kết nối.
    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    // Xóa session khi client ngắt kết nối.
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    // Gửi ServerMessage tới một session cụ thể.
    public void sendToSession(String sessionId, ServerMessage message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            System.err.println("[SessionService] Gửi tin nhắn thất bại tới " + sessionId + ": " + e.getMessage());
        }
    }

    // Gửi ServerMessage tới tất cả người chơi trong phòng.
    public void broadcastToRoom(Room room, ServerMessage message) {
        if (room == null) {
            return;
        }
        for (RoomPlayer player : room.getPlayers()) {
            sendToSession(player.getSessionId(), message);
        }
    }
}
