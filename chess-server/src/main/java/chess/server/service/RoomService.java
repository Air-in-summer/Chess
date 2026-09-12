package chess.server.service;

import chess.server.exception.RoomException;
import chess.server.model.Definition.GameResult;
import chess.server.model.Definition.PlayerRole;
import chess.server.model.Definition.RoomStatus;
import chess.server.model.Definition.Side;
import chess.server.model.Room;
import chess.server.model.RoomPlayer;
import chess.server.protocol.ErrorCode;
import chess.server.protocol.MessageType;
import chess.server.protocol.ServerMessage;
import chess.server.protocol.payload.GamePayload;
import chess.server.protocol.payload.PlayerPayload;
import chess.server.protocol.payload.RoomPayload;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final SessionService sessionService;
    private final GameService gameService;

    public RoomService(SessionService sessionService, @Lazy GameService gameService) {
        this.sessionService = sessionService;
        this.gameService = gameService;
    }

    private String generateRoomId() {
        String roomId;
        do {
            roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (rooms.containsKey(roomId));
        return roomId;
    }

    public Room findRoomBySessionId(String sessionId) {
        for (Room room : rooms.values()) {
            if (room.findPlayer(sessionId) != null) {
                return room;
            }
        }
        return null;
    }

    private List<PlayerPayload> buildPlayersPayload(Room room) {
        List<PlayerPayload> list = new ArrayList<>();
        for (RoomPlayer rp : room.getPlayers()) {
            list.add(new PlayerPayload(rp.getSessionId(), rp.getRole(), rp.isReady(), rp.isConnected()));
        }
        return list;
    }

    public void createRoom(String sessionId) {
        if (findRoomBySessionId(sessionId) != null) {
            throw new RoomException(MessageType.ERROR, ErrorCode.INVALID_MESSAGE, "Bạn đã ở trong một phòng rồi.");
        }

        String roomId = generateRoomId();
        Room room = new Room(roomId, sessionId);
        rooms.put(roomId, room);

        ServerMessage msg = new ServerMessage(MessageType.ROOM_CREATED);
        RoomPayload rp = new RoomPayload();
        rp.setRoomId(roomId);
        rp.setRole(PlayerRole.OWNER);
        rp.setRoomStatus(RoomStatus.WAITING);
        msg.setRoomPayload(rp);

        sessionService.sendToSession(sessionId, msg);
    }

    public void joinRoom(String roomId, String sessionId) {
        if (findRoomBySessionId(sessionId) != null) {
            throw new RoomException(MessageType.ERROR, ErrorCode.INVALID_MESSAGE, "Bạn đã ở trong một phòng rồi.");
        }

        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RoomException(MessageType.ROOM_JOIN_FAILED, ErrorCode.ROOM_NOT_FOUND, "Không tìm thấy phòng.");
        }

        if (room.isFull()) {
            throw new RoomException(MessageType.ROOM_JOIN_FAILED, ErrorCode.ROOM_FULL, "Phòng đã đầy.");
        }

        if (room.getStatus() == RoomStatus.PLAYING) {
            throw new RoomException(MessageType.ROOM_JOIN_FAILED, ErrorCode.ROOM_ALREADY_PLAYING, "Trận đấu đang diễn ra.");
        }

        if (room.getStatus() == RoomStatus.FINISHED) {
            throw new RoomException(MessageType.ROOM_JOIN_FAILED, ErrorCode.ROOM_FINISHED, "Trận đấu đã kết thúc.");
        }

        room.addPlayer(sessionId, PlayerRole.GUEST);

        RoomPlayer guest = room.findPlayer(sessionId);
        PlayerPayload guestPayload = new PlayerPayload(guest.getSessionId(), guest.getRole(), guest.isReady(), guest.isConnected());
        List<PlayerPayload> allPlayers = buildPlayersPayload(room);

        // Gửi thông báo cho người vừa join
        ServerMessage joinedMsg = new ServerMessage(MessageType.JOINED_ROOM);
        RoomPayload rpJoined = new RoomPayload();
        rpJoined.setRoomId(roomId);
        rpJoined.setRole(PlayerRole.GUEST);
        rpJoined.setRoomStatus(room.getStatus());
        joinedMsg.setRoomPayload(rpJoined);
        sessionService.sendToSession(sessionId, joinedMsg);

        // Gửi thông báo cho chủ phòng
        RoomPlayer owner = null;
        for (RoomPlayer rp : room.getPlayers()) {
            if (rp.getRole() == PlayerRole.OWNER) {
                owner = rp;
                break;
            }
        }
        if (owner != null) {
            ServerMessage joinedOwnerMsg = new ServerMessage(MessageType.PLAYER_JOINED);
            RoomPayload rpOwnerJoined = new RoomPayload();
            rpOwnerJoined.setRoomId(roomId);
            rpOwnerJoined.setPlayer(guestPayload);
            rpOwnerJoined.setPlayers(allPlayers);
            joinedOwnerMsg.setRoomPayload(rpOwnerJoined);
            sessionService.sendToSession(owner.getSessionId(), joinedOwnerMsg);
        }

        // Gửi trạng thái LOBBY cho cả hai
        ServerMessage lobbyMsg = new ServerMessage(MessageType.LOBBY_STATE);
        RoomPayload rpLobby = new RoomPayload();
        rpLobby.setRoomId(roomId);
        rpLobby.setRoomStatus(RoomStatus.LOBBY);
        rpLobby.setPlayers(allPlayers);
        lobbyMsg.setRoomPayload(rpLobby);
        sessionService.broadcastToRoom(room, lobbyMsg);
    }

    public void ready(String sessionId) {
        Room room = findRoomBySessionId(sessionId);
        if (room == null) {
            throw new RoomException(MessageType.ERROR, ErrorCode.NOT_IN_ROOM, "Bạn không ở trong phòng nào.");
        }

        if (room.getStatus() != RoomStatus.WAITING && room.getStatus() != RoomStatus.LOBBY) {
            throw new RoomException(MessageType.ERROR, ErrorCode.ROOM_NOT_IN_LOBBY, "Phòng không ở trạng thái chờ (lobby).");
        }

        room.setReady(sessionId, true);

        ServerMessage msg = new ServerMessage(MessageType.READY_STATE_CHANGED);
        RoomPayload rp = new RoomPayload();
        rp.setRoomId(room.getRoomId());
        rp.setPlayerId(sessionId);
        rp.setReady(true);
        rp.setPlayers(buildPlayersPayload(room));
        msg.setRoomPayload(rp);
        sessionService.broadcastToRoom(room, msg);

        if (room.canStartGame()) {
            gameService.startGame(room);
        }
    }

    public void unready(String sessionId) {
        Room room = findRoomBySessionId(sessionId);
        if (room == null) return;
        if (room.getStatus() != RoomStatus.WAITING && room.getStatus() != RoomStatus.LOBBY) return;

        room.setReady(sessionId, false);

        ServerMessage msg = new ServerMessage(MessageType.READY_STATE_CHANGED);
        RoomPayload rp = new RoomPayload();
        rp.setRoomId(room.getRoomId());
        rp.setPlayerId(sessionId);
        rp.setReady(false);
        rp.setPlayers(buildPlayersPayload(room));
        msg.setRoomPayload(rp);
        sessionService.broadcastToRoom(room, msg);
    }

    public void leave(String sessionId) {
        Room room = findRoomBySessionId(sessionId);
        if (room == null) return;

        RoomPlayer leavingPlayer = room.findPlayer(sessionId);
        if (leavingPlayer == null) return;

        RoomStatus status = room.getStatus();

        if (status == RoomStatus.WAITING) {
            rooms.remove(room.getRoomId());
        } else if (status == RoomStatus.LOBBY) {
            if (leavingPlayer.getRole() == PlayerRole.OWNER) {
                ServerMessage msg = new ServerMessage(MessageType.ROOM_CLOSED);
                msg.setMessage("Chủ phòng đã rời đi.");
                sessionService.broadcastToRoom(room, msg);
                rooms.remove(room.getRoomId());
            } else {
                room.removePlayer(sessionId);
                ServerMessage msg = new ServerMessage(MessageType.PLAYER_LEFT);
                RoomPayload rp = new RoomPayload();
                rp.setRoomId(room.getRoomId());
                rp.setLeftPlayerId(sessionId);
                rp.setPlayers(buildPlayersPayload(room));
                rp.setRoomStatus(RoomStatus.WAITING);
                msg.setRoomPayload(rp);
                
                RoomPlayer owner = room.getPlayers().get(0); // Chỉ còn lại chủ phòng
                sessionService.sendToSession(owner.getSessionId(), msg);
            }
        } else if (status == RoomStatus.PLAYING) {
            gameService.cancelTimeout(room.getRoomId());
            // Tìm người thắng (người còn lại)
            RoomPlayer winner = null;
            for (RoomPlayer rp : room.getPlayers()) {
                if (!rp.getSessionId().equals(sessionId)) {
                    winner = rp;
                    break;
                }
            }

            if (winner != null && room.getGame() != null) {
                GameResult result = winner.getPlayerSide() == Side.RED ? GameResult.RED_WIN : GameResult.BLACK_WIN;
                room.getGame().setResult(result);
                room.getGame().setGameOver(true);

                System.out.println("DEBUG: Sending GAME_OVER to room " + room.getRoomId() + ". Winner: " + winner.getSessionId() + ", Loser: " + sessionId);

                ServerMessage msg = new ServerMessage(MessageType.GAME_OVER);
                GamePayload gp = new GamePayload();
                gp.setRoomId(room.getRoomId());
                gp.setWinner(winner.getSessionId());
                gp.setLoser(sessionId);
                gp.setReason("Đối thủ đã thoát");
                gp.setResult(result);
                msg.setGamePayload(gp);
                sessionService.broadcastToRoom(room, msg);
            }
            
            room.finishGame();
            room.removePlayer(sessionId);
        } else if (status == RoomStatus.FINISHED) {
            room.removePlayer(sessionId);
            if (room.getPlayers().isEmpty()) {
                rooms.remove(room.getRoomId());
            }
        }
    }

    public void disconnect(String sessionId) {
        leave(sessionId);
    }
}