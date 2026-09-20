package chess.server.handler;

import chess.server.dispatcher.MessageDispatcher;
import chess.server.protocol.ClientMessage;
import chess.server.service.RoomService;
import chess.server.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Cổng giao tiếp (Entry point) tiếp nhận mọi kết nối và tin nhắn WebSocket từ Client.
 * Kế thừa từ TextWebSocketHandler của Spring Boot, class này đóng vai trò như một Controller mạng cơ bản:
 * - Theo dõi sự kiện Client kết nối / ngắt kết nối.
 * - Chuyển đổi (Parse) chuỗi JSON thô nhận được thành đối tượng Java (ClientMessage).
 * - Chuyển tiếp (Forward) tin nhắn đã parse sang MessageDispatcher để phân loại và xử lý.
 */
@Component
public class ChessWebSocketHandler extends TextWebSocketHandler {

    private final SessionService sessionService;
    private final RoomService roomService;
    private final MessageDispatcher messageDispatcher;
    private final ObjectMapper objectMapper;

    public ChessWebSocketHandler(SessionService sessionService, RoomService roomService, MessageDispatcher messageDispatcher) {
        this.sessionService = sessionService;
        this.roomService = roomService;
        this.messageDispatcher = messageDispatcher;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Kích hoạt tự động khi một Client mở kết nối WebSocket thành công.
     * Lưu trữ thông tin session mới vào SessionService để quản lý.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionService.addSession(session);
        System.out.println("[WebSocket] Client kết nối: " + session.getId());
    }

    /**
     * Xử lý mỗi khi có tin nhắn văn bản (chuỗi JSON) gửi lên từ Client.
     * Thực hiện parse chuỗi JSON sang đối tượng ClientMessage và đẩy sang Dispatcher điều phối.
     * Bắt exception nếu JSON sai định dạng để tránh crash server.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ClientMessage clientMessage = objectMapper.readValue(message.getPayload(), ClientMessage.class);
            messageDispatcher.dispatch(session.getId(), clientMessage);
        } catch (Exception e) {
            System.err.println("[WebSocket] Lỗi parse JSON từ " + session.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Kích hoạt tự động khi kết nối WebSocket bị đóng (người dùng tắt trình duyệt, mất mạng...).
     * Xóa session khỏi hệ thống mạng và gọi RoomService để dọn dẹp (xử lý thoát phòng, xử thua...).
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionService.removeSession(session.getId());
        roomService.disconnect(session.getId());
        System.out.println("[WebSocket] Client ngắt kết nối: " + session.getId());
    }
}
