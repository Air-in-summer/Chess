package chess.server.model;

public class Definition {
    private Definition() {}

    public enum Piece {
        // r = red, b = black
        // P = Pawn (Tốt)
        // R = Rook (Xe)
        // K = Knight (Mã)
        // B = Bishop (Tượng)
        // A = Advisor (Sĩ)
        // G = General (Tướng)
        // C = Cannon (Pháo)
        EMPTY,
        rP, rR, rK, rB, rA, rG, rC,
        bP, bR, bK, bB, bA, bG, bC
    }

    public enum GameResult {
        ONGOING,
        RED_WIN,
        BLACK_WIN,
        DRAW
    }

    public enum RoomStatus {
        WAITING,
        LOBBY,
        PLAYING,
        FINISHED
    }

    public enum PlayerRole {
        OWNER,
        GUEST
    }
    
    public enum Side {
        BLACK,
        RED
    }
}
