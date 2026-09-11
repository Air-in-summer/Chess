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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionService.addSession(session);
        System.out.println("[WebSocket] Client kết nối: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ClientMessage clientMessage = objectMapper.readValue(message.getPayload(), ClientMessage.class);
            messageDispatcher.dispatch(session.getId(), clientMessage);
        } catch (Exception e) {
            System.err.println("[WebSocket] Lỗi parse JSON từ " + session.getId() + ": " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        roomService.disconnect(session.getId());
        sessionService.removeSession(session.getId());
        System.out.println("[WebSocket] Client ngắt kết nối: " + session.getId());
    }
}
