package chess.server.service.rule;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;
import chess.server.service.RuleService;

import java.util.ArrayList;
import java.util.List;

public class Rook implements IPiece {

    @Override
    public boolean checkMovement(int startX, int startY, int endX, int endY, Piece[][] board) {
        // Kiểm tra xem quân cờ tại vị trí (startX, startY) có phải là Xe (Rook) không
        Piece piece = board[startX][startY];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rR && piece != Piece.bR)
        {
            return false; // Không phải Xe
        }

        // Xe chỉ di chuyển thẳng hàng hoặc cột
        if (startX != endX && startY != endY)
        {
            return false; // Di chuyển không hợp lệ
        }

        // Kiểm tra xem có quân cờ nào chắn đường không
        // stepX và stepY xác định hướng di chuyển (1, 0), (-1, 0), (0, 1), (0, -1)
        // currentX, currentY để duyệt qua các ô từ (startX, startY) đến (endX, endY)
        int stepX = (endX - startX) == 0 ? 0 : (endX - startX) / Math.abs(endX - startX);
        int stepY = (endY - startY) == 0 ? 0 : (endY - startY) / Math.abs(endY - startY);

        int currentX = startX + stepX;
        int currentY = startY + stepY;

        while (currentX != endX || currentY != endY)
        {
            if (board[currentX][currentY] != Piece.EMPTY)
            {
                return false; // Có quân cờ chắn đường
            }
            currentX += stepX;
            currentY += stepY;
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
        if (piece != Piece.rR && piece != Piece.bR)
        {
            return possibleMoves; // Không phải Xe
        }
        // Các hướng di chuyển của Xe: lên, xuống, trái, phải
        int[][] directions = new int[][]
        {
            { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }
        };
        for (int i = 0; i < directions.length; i++)
        {
            int stepX = directions[i][0];
            int stepY = directions[i][1];
            int newX = x + stepX;
            int newY = y + stepY;
            while (newX >= 0 && newX < 10 && newY >= 0 && newY < 9)
            {
                // Kiểm tra nếu di chuyển quân cờ không làm Tướng bị chiếu, không làm chạm mặt Tướng
                Piece originalPiece = board[newX][newY];
                if(!RuleService.isOpponentPiece(originalPiece, pieceColor) && originalPiece != Piece.EMPTY)
                {
                    newX += stepX;
                    newY += stepY;
                    continue; // Bỏ qua nếu ô đích có quân đồng đội
                }

                board[newX][newY] = piece;
                board[x][y] = Piece.EMPTY;
                boolean isGeneralInCheck = RuleService.isGeneralInChecked(board, piece.toString().startsWith("r") ? "Red" : "Black");
                boolean isFacingGenerals = RuleService.isGeneralFaceToFace(board);
                board[x][y] = piece;
                board[newX][newY] = originalPiece;

                if (isGeneralInCheck || isFacingGenerals)
                {
                    newX += stepX;
                    newY += stepY;
                    continue; // Bỏ qua nước đi này vì làm Tướng bị chiếu hoặc chạm mặt Tướng
                }
                if (board[newX][newY] == Piece.EMPTY)
                {
                    possibleMoves.add(new Position(newX, newY)); // Ô trống, có thể di chuyển
                }

                else
                {
                    if (RuleService.isOpponentPiece(board[newX][newY], pieceColor))
                    {
                        possibleMoves.add(new Position(newX, newY)); // Quân đối phương, có thể ăn
                    }
                    break; // Bị chắn bởi quân cờ khác, dừng lại
                }
                newX += stepX;
                newY += stepY;
            }
        }
        return possibleMoves;
    }
}
