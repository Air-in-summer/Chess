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

/**
 * Class SessionService chịu trách nhiệm quản lý toàn bộ các kết nối WebSocket (sessions) của hệ thống.
 * Chức năng chính bao gồm:
 * - Lưu trữ danh sách các session đang mở (active sessions) trong bộ nhớ.
 * - Cung cấp các hàm tiện ích để parse Java Object thành chuỗi JSON một cách tối ưu.
 * - Đảm nhiệm việc gửi tin nhắn (ServerMessage) đến một client cụ thể hoặc phát sóng (broadcast) tới toàn bộ người chơi trong phòng.
 * Class này đóng vai trò cách ly tầng giao tiếp mạng (network communication) khỏi logic nghiệp vụ (business logic) của game.
 */
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
        } catch (Exception e) {
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
