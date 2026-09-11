package chess.server.service.rule;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;
import chess.server.service.RuleService;

import java.util.ArrayList;
import java.util.List;

public class Advisor implements IPiece {

    @Override
    public boolean checkMovement(int startX, int startY, int endX, int endY, Piece[][] board) {
        // Kiểm tra xem quân cờ tại vị trí (startX, startY) có phải là Sĩ (Advisor) không
        Piece piece = board[startX][startY];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rA && piece != Piece.bA)
        {
            return false; // Không phải Sĩ
        }

        // Sĩ chỉ di chuyển chéo 1 ô trong cung
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        // Kiểm tra nếu ô đích nằm trong cung
        boolean inPalace = (endX >= 0 && endX <= 2 && endY >= 3 && endY <= 5) || (endX >= 7 && endX <= 9 && endY >= 3 && endY <= 5);

        if (dx == 1 && dy == 1 && inPalace)
        {
            // Kiểm tra nếu ô đích có quân cờ đối phương
            if (board[endX][endY] != Piece.EMPTY && RuleService.isOpponentPiece(board[endX][endY], pieceColor))
            {
                return true; // Di chuyển hợp lệ và ăn quân đối phương
            }
            else if (board[endX][endY] == Piece.EMPTY)
            {
                return true; // Di chuyển hợp lệ vào ô trống
            }
        }

        return false; // Nước đi không hợp lệ
    }

    @Override
    public List<Position> getAllPossibleMoves(int x, int y, Piece[][] board) {
        List<Position> possibleMoves = new ArrayList<>();
        Piece piece = board[x][y];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rA && piece != Piece.bA)
        {
            return possibleMoves; // Không phải Sĩ
        }

        // Các nước đi hợp lệ của Sĩ
        int[][] moves = new int[][]
        {
            { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
        };


        // Duyệt qua tất cả các nước đi hợp lệ
        for (int i = 0; i < moves.length; i++)
        {
            int newX = x + moves[i][0];
            int newY = y + moves[i][1];
            boolean inPalace = (newX >= 0 && newX <= 2 && newY >= 3 && newY <= 5) || (newX >= 7 && newX <= 9 && newY >= 3 && newY <= 5);
            if (newX >= 0 && newX < 10 && newY >= 0 && newY < 9 && inPalace)
            {
                // Kiểm tra nếu di chuyển quân cờ không làm Tướng bị chiếu, không làm chạm mặt Tướng
                Piece originalPiece = board[newX][newY];
                
                // Bỏ qua nếu ô đích có quân đồng đội
                if (!RuleService.isOpponentPiece(originalPiece, pieceColor) && originalPiece != Piece.EMPTY)
                {
                    continue;
                }
                board[newX][newY] = piece;
                board[x][y] = Piece.EMPTY;
                boolean isGeneralInCheck = RuleService.isGeneralInChecked(board, piece.toString().startsWith("r") ? "Red" : "Black");
                boolean isFacingGenerals = RuleService.isGeneralFaceToFace(board);
                board[x][y] = piece;
                board[newX][newY] = originalPiece;

                // Kiểm tra nếu ô đích trống hoặc có quân cờ đối phương
                if ((board[newX][newY] == Piece.EMPTY || RuleService.isOpponentPiece(board[newX][newY], pieceColor)) && !isGeneralInCheck && !isFacingGenerals)
                {
                    possibleMoves.add(new Position(newX, newY));
                }
            }
        }
        return possibleMoves;
    }
}
