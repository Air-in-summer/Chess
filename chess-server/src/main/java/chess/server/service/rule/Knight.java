package chess.server.service.rule;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;
import chess.server.service.RuleService;

import java.util.ArrayList;
import java.util.List;

public class Knight implements IPiece {

    @Override
    public boolean checkMovement(int startX, int startY, int endX, int endY, Piece[][] board) {
        // Kiểm tra xem quân cờ tại vị trí (startX, startY) có phải là Mã (Knight) không
        Piece piece = board[startX][startY];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rK && piece != Piece.bK)
        {
            return false; // Không phải Mã
        }

        // Các nước đi hợp lệ của Mã
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        if ((dx == 2 && dy == 1) || (dx == 1 && dy == 2))
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
        if (piece != Piece.rK && piece != Piece.bK)
        {
            return possibleMoves; // Không phải Mã
        }

        // Các nước đi hợp lệ của Mã
        int[][] moves = new int[][]
        {
            { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 },
            { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }
        };

        for (int i = 0; i < moves.length; i++)
        {
            int newX = x + moves[i][0];
            int newY = y + moves[i][1];

            if (newX >= 0 && newX < 10 && newY >= 0 && newY < 9)
            {
                //kiểm tra Mã có bị cản chân không
                boolean isBlocked = isKnightBlocked(x, y, moves[i][0], moves[i][1], board);
                if (isBlocked)
                {
                    continue;
                }
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

    private boolean isKnightBlocked(int startX, int startY, int dx, int dy, Piece[][] board)
    {
        // Kiểm tra nếu Mã bị cản chân
        if (Math.abs(dx) == 2 && Math.abs(dy) == 1)
        {
            int blockX = startX + dx / 2;
            int blockY = startY;
            return board[blockX][blockY] != Piece.EMPTY;
        }
        else if (Math.abs(dx) == 1 && Math.abs(dy) == 2)
        {
            int blockX = startX;
            int blockY = startY + dy / 2;
            return board[blockX][blockY] != Piece.EMPTY;
        }
        return false;
    }
}
