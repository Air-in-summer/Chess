package chess.server.protocol;

public enum ErrorCode {
    // Không tìm thấy phòng theo roomId client gửi lên.
    ROOM_NOT_FOUND,

    // Phòng đã đủ 2 người chơi, không thể join thêm.
    ROOM_FULL,

    // Phòng đã bắt đầu chơi, không thể join hoặc ready lại.
    ROOM_ALREADY_PLAYING,

    // Phòng đã kết thúc hoặc đã bị đóng.
    ROOM_FINISHED,

    // Người gửi message chưa ở trong phòng nào.
    NOT_IN_ROOM,

    // Người gửi message không thuộc phòng đang thao tác.
    NOT_ROOM_PLAYER,

    // Thao tác chỉ được dùng trong phòng chờ nhưng phòng không ở trạng thái phù hợp.
    ROOM_NOT_IN_LOBBY,

    // Game chưa bắt đầu nên chưa thể đi quân hoặc lấy trạng thái game.
    GAME_NOT_STARTED,

    // Game đã kết thúc nên không nhận thêm nước đi.
    GAME_ALREADY_OVER,

    // Người gửi move không phải người đang tới lượt.
    NOT_YOUR_TURN,

    // Ô bắt đầu hoặc ô đích nằm ngoài bàn cờ.
    INVALID_POSITION,

    // Ô bắt đầu không có quân cờ.
    EMPTY_FROM_POSITION,

    // Người chơi chọn quân không thuộc bên mình.
    NOT_YOUR_PIECE,

    // Nước đi không hợp lệ theo luật cờ.
    INVALID_MOVE,

    // Message gửi lên sai định dạng hoặc thiếu dữ liệu cần thiết.
    INVALID_MESSAGE
}
