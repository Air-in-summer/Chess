package chess.server.service.rule;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;
import chess.server.service.RuleService;

import java.util.ArrayList;
import java.util.List;

public class General implements IPiece {

    @Override
    public boolean checkMovement(int startX, int startY, int endX, int endY, Piece[][] board) {
        // Kiểm tra xem quân cờ tại vị trí (startX, startY) có phải là Tướng (General) không
        Piece piece = board[startX][startY];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rG && piece != Piece.bG)
        {
            return false; // Không phải Tướng
        }

        // Tướng chỉ di chuyển trong cung và mỗi lần di chuyển một ô theo chiều ngang hoặc dọc
        if (Math.abs(endX - startX) + Math.abs(endY - startY) != 1)
        {
            return false; // Di chuyển không hợp lệ
        }

        // Kiểm tra nếu ô đích nằm trong cung
        if (!isWithinPalace(endX, endY, piece))
        {
            return false; // Ô đích không nằm trong cung
        }

        // Kiểm tra nếu ô đích có quân cờ đối phương
        if (board[endX][endY] != Piece.EMPTY && RuleService.isOpponentPiece(board[endX][endY], pieceColor))
        {
            return true; // Di chuyển hợp lệ và ăn quân đối phương
        }
        else if (board[endX][endY] == Piece.EMPTY)
        {
            return true; // Di chuyển hợp lệ vào ô trống
        }

        return false; // Nước đi không hợp lệ
    }

    @Override
    public List<Position> getAllPossibleMoves(int x, int y, Piece[][] board) {
        List<Position> possibleMoves = new ArrayList<>();
        Piece piece = board[x][y];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rG && piece != Piece.bG)
        {
            return possibleMoves; // Không phải Tướng
        }

        // Các hướng di chuyển có thể của Tướng (lên, xuống, trái, phải)
        int[][] directions = new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        for (int i = 0; i < directions.length; i++)
        {
            int newX = x + directions[i][0];
            int newY = y + directions[i][1];
            if (isWithinPalace(newX, newY, piece))
            {
                // Kiểm tra nếu di chuyển quân cờ không làm Tướng bị chiếu, không làm chạm mặt Tướng
                Piece originalPiece = board[newX][newY];
                if (!RuleService.isOpponentPiece(originalPiece, pieceColor) && originalPiece != Piece.EMPTY)
                {
                    continue; // Bỏ qua nếu ô đích có quân đồng đội
                }
                board[newX][newY] = piece;
                board[x][y] = Piece.EMPTY;
                boolean isGeneralInCheck = RuleService.isGeneralInChecked(board, piece.toString().startsWith("r") ? "Red" : "Black");
                boolean isFacingGenerals = RuleService.isGeneralFaceToFace(board);
                board[x][y] = piece;
                board[newX][newY] = originalPiece;

                if (!isGeneralInCheck && !isFacingGenerals && (board[newX][newY] == Piece.EMPTY || RuleService.isOpponentPiece(board[newX][newY], pieceColor)))
                {
                    possibleMoves.add(new Position(newX, newY));
                }
            }
        }
        return possibleMoves;
    }

    private boolean isWithinPalace(int x, int y, Piece piece) {
        if (piece == Piece.rG) {
            return x >= 7 && x <= 9 && y >= 3 && y <= 5;
        }

        if (piece == Piece.bG) {
            return x >= 0 && x <= 2 && y >= 3 && y <= 5;
        }

        return false;
    }
}
