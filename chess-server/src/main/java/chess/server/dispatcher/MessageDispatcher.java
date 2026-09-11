package chess.server.dispatcher;

import chess.server.exception.RoomException;
import chess.server.model.Room;
import chess.server.protocol.ClientMessage;
import chess.server.protocol.MessageType;
import chess.server.protocol.ServerMessage;
import chess.server.protocol.payload.MovePayload;
import chess.server.service.GameService;
import chess.server.service.RoomService;
import chess.server.service.SessionService;
import org.springframework.stereotype.Component;

@Component
public class MessageDispatcher {

    private final RoomService roomService;
    private final GameService gameService;
    private final SessionService sessionService;

    public MessageDispatcher(RoomService roomService, GameService gameService, SessionService sessionService) {
        this.roomService = roomService;
        this.gameService = gameService;
        this.sessionService = sessionService;
    }

    public void dispatch(String sessionId, ClientMessage message) {
        try {
            switch (message.getType()) {
                case CREATE_ROOM:
                    roomService.createRoom(sessionId);
                    break;
                case JOIN_ROOM:
                    if (message.getRoomPayload() != null && message.getRoomPayload().getRoomId() != null) {
                        roomService.joinRoom(message.getRoomPayload().getRoomId(), sessionId);
                    }
                    break;
                case READY:
                    roomService.ready(sessionId);
                    break;
                case UNREADY:
                    roomService.unready(sessionId);
                    break;
                case LEAVE:
                    roomService.leave(sessionId);
                    break;
                case MOVE:
                    if (message.getMovePayload() != null) {
                        Room room = roomService.findRoomBySessionId(sessionId);
                        gameService.handleMove(sessionId, room, message.getMovePayload());
                    }
                    break;
                default:
                    System.out.println("[MessageDispatcher] Bỏ qua message không hỗ trợ: " + message.getType());
                    break;
            }
        } catch (RoomException e) {
            ServerMessage errorMsg = new ServerMessage(e.getMessageType());
            errorMsg.setErrorCode(e.getErrorCode());
            errorMsg.setMessage(e.getMessage());
            
            if (e.getMessageType() == MessageType.MOVE_REJECTED) {
                MovePayload mp = new MovePayload();
                mp.setReason(e.getMessage());
                errorMsg.setMovePayload(mp);
            }
            
            sessionService.sendToSession(sessionId, errorMsg);
            System.err.println("[MessageDispatcher] Lỗi nghiệp vụ từ session " + sessionId + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[MessageDispatcher] Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }
}