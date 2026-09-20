package chess.server.protocol;

public enum MessageType {
    // Client yêu cầu tạo phòng mới.
    // Payload: không cần.
    CREATE_ROOM,

    // Client yêu cầu join vào phòng bằng roomId.
    // Payload: RoomPayload.roomId.
    JOIN_ROOM,

    // Client đánh dấu sẵn sàng trong phòng chờ.
    // Payload: không cần.
    READY,

    // Client bỏ trạng thái sẵn sàng trong phòng chờ.
    // Payload: không cần.
    UNREADY,

    // Client yêu cầu đi một quân.
    // Payload: MovePayload.fromX, fromY, toX, toY.
    MOVE,

    // Client xin đầu hàng.
    // Payload: không cần.
    SURRENDER,

    // Client chủ động rời phòng.
    // Payload: không cần.
    LEAVE,

    // Server báo tạo phòng thành công cho owner.
    // Payload: RoomPayload.roomId, role, roomStatus.
    ROOM_CREATED,

    // Server báo join phòng thành công cho guest.
    // Payload: RoomPayload.roomId, role, roomStatus.
    JOINED_ROOM,

    // Server báo join phòng thất bại.
    // Payload: RoomPayload.reason, message hoặc ErrorCode.
    ROOM_JOIN_FAILED,

    // Server đồng bộ trạng thái phòng chờ.
    // Payload: RoomPayload.roomId, players, roomStatus.
    LOBBY_STATE,

    // Server báo có người mới vào phòng chờ.
    // Payload: RoomPayload.roomId, PlayerPayload.player, players.
    PLAYER_JOINED,

    // Server báo có người rời phòng chờ.
    // Payload: RoomPayload.roomId, leftPlayerId, players, roomStatus.
    PLAYER_LEFT,

    // Server báo trạng thái ready của một người chơi thay đổi.
    // Payload: RoomPayload.roomId, playerId, ready, players.
    READY_STATE_CHANGED,

    // Server báo game bắt đầu khi đủ 2 người và cả hai ready.
    // Payload: GamePayload.roomId, redPlayerId, blackPlayerId, board, redTurn, redTime, blackTime.
    START_GAME,

    // Server đồng bộ toàn bộ trạng thái bàn cờ.
    // Payload: GamePayload.board, redTurn, redTime, blackTime, checkSide.
    BOARD_STATE,

    // Server trả danh sách nước đi có thể cho một quân nếu client cần hỏi server.
    // Payload: MovePayload.fromX, fromY, moves.
    POSSIBLE_MOVES,

    // Server báo nước đi hợp lệ đã được áp dụng.
    // Payload: MovePayload.fromX, fromY, toX, toY, piece, capturedPiece, redTurn, checkSide, redTime, blackTime.
    MOVE_APPLIED,

    // Server từ chối nước đi.
    // Payload: MovePayload.reason, message hoặc ErrorCode.
    MOVE_REJECTED,

    // Server báo một bên đang bị chiếu.
    // Payload: GamePayload.checkedSide.
    CHECK,

    // Server cập nhật thời gian còn lại.
    // Payload: GamePayload.redTime, blackTime, redTurn.
    TIMER_UPDATED,

    // Server báo game kết thúc.
    // Payload: GamePayload.winner, loser, reason.
    GAME_OVER,

    // Server yêu cầu client quay về giao diện ban đầu.
    // Payload: GamePayload.reason hoặc RoomPayload.reason.
    RETURN_HOME,

    // Server báo phòng đã bị đóng.
    // Payload: RoomPayload.reason.
    ROOM_CLOSED,

    // Server báo lỗi chung không thuộc case cụ thể.
    // Payload: ErrorCode.code, message.
    ERROR
}
