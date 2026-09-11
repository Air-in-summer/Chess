package chess.server.service.rule;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;
import chess.server.service.RuleService;

import java.util.ArrayList;
import java.util.List;

public class Elephant implements IPiece {

    @Override
    public boolean checkMovement(int startX, int startY, int endX, int endY, Piece[][] board) {
        // Kiểm tra xem quân cờ tại vị trí (startX, startY) có phải là Tượng (Bishop) không
        Piece piece = board[startX][startY];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rB && piece != Piece.bB)
        {
            return false; // Không phải Tượng
        }

        // Tượng chỉ di chuyển chéo 2 ô
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        if (isBishopBlocked(startX, startY, endX - startX, endY - startY, board))
        {
            return false;
        }
        if (dx == 2 && dy == 2)
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
        if (piece != Piece.rB && piece != Piece.bB)
        {
            return possibleMoves; // Không phải Tượng
        }

        // Các nước đi hợp lệ của Tượng
        int[][] moves = new int[][]
        {
            { 2, 2 }, { 2, -2 }, { -2, 2 }, { -2, -2 }
        };

        for (int i = 0; i < moves.length; i++)
        {
            int newX = x + moves[i][0];
            int newY = y + moves[i][1];
            
            if (newX >= 0 && newX < 10 && newY >= 0 && newY < 9)
            {
                //kiểm tra tượng có bị chắn không
                if (isBishopBlocked(x, y, moves[i][0], moves[i][1], board)) continue;
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

                if (isGeneralInCheck || isFacingGenerals)
                {
                    continue; // Bỏ qua nước đi này vì làm Tướng bị chiếu hoặc chạm mặt Tướng
                }
                if (board[newX][newY] == Piece.EMPTY || RuleService.isOpponentPiece(board[newX][newY], pieceColor))
                {
                    possibleMoves.add(new Position(newX, newY));
                }
            }
        }
        return possibleMoves;
    }

    private boolean isBishopBlocked(int startX, int startY, int dx, int dy, Piece[][] board)
    {
        int blockX = startX + dx / 2;
        int blockY = startY + dy / 2;  
        return board[blockX][blockY] != Piece.EMPTY;
    }
}
