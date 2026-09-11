package chess.server.model;

import chess.server.model.Definition.PlayerRole;
import chess.server.model.Definition.RoomStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Room {

    // Mã phòng để người chơi khác nhập vào khi join.
    private String roomId;

    // Danh sách người chơi trong phòng. Bản đầu chỉ cho tối đa 2 người.
    private List<RoomPlayer> players;

    // Trạng thái hiện tại của phòng: chờ người, lobby, đang chơi hoặc đã kết thúc.
    private RoomStatus status;

    // Ván cờ của phòng, chỉ được tạo khi cả hai người chơi đã sẵn sàng.
    private Game game;

    public Room(String roomId, String ownerSessionId) {
        this.roomId = roomId;
        this.players = new ArrayList<>();
        this.status = RoomStatus.WAITING;
        this.game = null;

        addPlayer(ownerSessionId, PlayerRole.OWNER);
    }

    public String getRoomId() {
        return roomId;
    }

    public List<RoomPlayer> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public RoomStatus getStatus() {
        return status;
    }

    public Game getGame() {
        return game;
    }

    public boolean isFull() {
        return players.size() >= 2;
    }

    // Tìm người chơi theo WebSocket session id.
    public RoomPlayer findPlayer(String sessionId) {
        for (RoomPlayer player : players) {
            if (player.getSessionId().equals(sessionId)) {
                return player;
            }
        }

        return null;
    }

    // Thêm người chơi vào phòng nếu phòng còn chỗ và session này chưa tồn tại.
    public void addPlayer(String sessionId, PlayerRole role) {
        if (isFull()) {
            return;
        }

        RoomPlayer existingPlayer = findPlayer(sessionId);
        if (existingPlayer != null) {
            return;
        }

        RoomPlayer player = new RoomPlayer(sessionId, role);
        players.add(player);

        if (players.size() == 2) {
            status = RoomStatus.LOBBY;
        }
    }

    // Xóa người chơi khỏi phòng và cập nhật lại trạng thái phòng nếu cần.
    public boolean removePlayer(String sessionId) {
        RoomPlayer player = findPlayer(sessionId);

        if (player == null) {
            return false;
        }

        players.remove(player);

        if (status == RoomStatus.LOBBY && players.size() == 1) {
            players.get(0).setReady(false);
            status = RoomStatus.WAITING;
        }

        if (players.isEmpty()) {
            status = RoomStatus.FINISHED;
        }

        return true;
    }

    // Cập nhật trạng thái sẵn sàng của người chơi trong lobby.
    public void setReady(String sessionId, boolean ready) {
        RoomPlayer player = findPlayer(sessionId);

        if (player != null) {
            player.setReady(ready);
        }
    }

    // Game chỉ bắt đầu khi đủ 2 người, cả hai đều ready và còn kết nối.
    public boolean canStartGame() {
        if (players.size() != 2) {
            return false;
        }

        for (RoomPlayer player : players) {
            if (!player.isReady() || !player.isConnected()) {
                return false;
            }
        }

        return true;
    }

    // Tạo ván cờ mới và chuyển phòng sang trạng thái đang chơi.
    public boolean startGame() {
        if (!canStartGame()) {
            return false;
        }

        game = new Game();
        status = RoomStatus.PLAYING;
        return true;
    }

    // Đánh dấu phòng đã kết thúc.
    public void finishGame() {
        status = RoomStatus.FINISHED;
    }
}
