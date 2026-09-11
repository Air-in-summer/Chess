package chess.server.service.rule;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;

import java.util.List;

public interface IPiece {

    // Di chuyển (theo luật)
    boolean checkMovement(int startX, int startY, int endX, int endY, Piece[][] board);

    // Trả về danh sách tất cả các nước đi hợp lệ của quân cờ tại vị trí (x,y)
    List<Position> getAllPossibleMoves(int x, int y, Piece[][] board);
}
