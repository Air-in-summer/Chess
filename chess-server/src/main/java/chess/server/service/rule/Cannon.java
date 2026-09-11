package chess.server.service.rule;

import chess.server.model.Definition.Piece;
import chess.server.model.Position;
import chess.server.service.RuleService;

import java.util.ArrayList;
import java.util.List;

public class Cannon implements IPiece {

    @Override
    public boolean checkMovement(int startX, int startY, int endX, int endY, Piece[][] board) {
        // Kiểm tra xem quân cờ tại vị trí (startX, startY) có phải là Pháo (Cannon) không
        Piece piece = board[startX][startY];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rC && piece != Piece.bC)
        {
            return false; // Không phải Pháo
        }

        // Pháo chỉ di chuyển thẳng hàng hoặc cột
        if (startX != endX && startY != endY)
        {
            return false; // Di chuyển không hợp lệ
        }

        // Kiểm tra số quân cờ chắn đường
        // stepX và stepY xác định hướng di chuyển (1, 0), (-1, 0), (0, 1), (0, -1)
        // currentX, currentY để duyệt qua các ô từ (startX, startY) đến (endX, endY)
        // obstacleCount đếm số quân cờ chắn đường
        int stepX = (endX - startX) == 0 ? 0 : (endX - startX) / Math.abs(endX - startX);
        int stepY = (endY - startY) == 0 ? 0 : (endY - startY) / Math.abs(endY - startY);

        int currentX = startX + stepX;
        int currentY = startY + stepY;
        int obstacleCount = 0;

        while (currentX != endX || currentY != endY)
        {
            if (board[currentX][currentY] != Piece.EMPTY)
            {
                obstacleCount++;
            }
            currentX += stepX;
            currentY += stepY;
        }

        // Nếu không có quân cờ chắn đường, chỉ có thể di chuyển vào ô trống
        if (obstacleCount == 0)
        {
            if (board[endX][endY] == Piece.EMPTY)
            {
                return true; // Di chuyển hợp lệ vào ô trống
            }
            else
            {
                return false; // Không thể ăn quân đối phương khi không có quân chắn đường
            }
        }
        // Nếu có đúng một quân cờ chắn đường, có thể ăn quân đối phương
        else if (obstacleCount == 1)
        {
            if (board[endX][endY] != Piece.EMPTY && RuleService.isOpponentPiece(board[endX][endY], pieceColor))
            {
                return true; // Di chuyển hợp lệ và ăn quân đối phương
            }
            else
            {
                return false; // Không thể di chuyển vào ô trống hoặc ăn quân đồng đội
            }
        }
        else
        {
            return false; // Quá nhiều quân cờ chắn đường
        }
    }

    @Override
    public List<Position> getAllPossibleMoves(int x, int y, Piece[][] board) {
        List<Position> possibleMoves = new ArrayList<>();
        Piece piece = board[x][y];
        String pieceColor = piece.toString().startsWith("r") ? "Red" : "Black";
        if (piece != Piece.rC && piece != Piece.bC)
        {
            return possibleMoves; // Không phải Pháo
        }

        // Kiểm tra các hướng di chuyển thẳng hàng và cột
        int[][] directions = new int[][]
        {
            { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }
        };
        System.out.println("Lấy các nước đi của Pháo");
        for (int i = 0; i < directions.length; i++)
        {
            int stepX = directions[i][0];
            int stepY = directions[i][1];
            int currentX = x + stepX;
            int currentY = y + stepY;
            int obstacle = 0;

            while (currentX >= 0 && currentX < 10 && currentY >= 0 && currentY < 9)
            {
                // Kiểm tra nếu di chuyển quân cờ không làm Tướng bị chiếu, không làm chạm mặt Tướng
                Piece originalPiece = board[currentX][currentY];
                if(!RuleService.isOpponentPiece(originalPiece, pieceColor) && originalPiece != Piece.EMPTY)
                {  
                    //if (obstacle == 0) obstacle = true;
                    obstacle++;
                    currentX += stepX;
                    currentY += stepY;
                    continue; // Bỏ qua nếu ô đích có quân đồng đội
                }
                board[currentX][currentY] = piece;
                board[x][y] = Piece.EMPTY;
                boolean isGeneralInCheck = RuleService.isGeneralInChecked(board, piece.toString().startsWith("r") ? "Red" : "Black");
                boolean isFacingGenerals = RuleService.isGeneralFaceToFace(board);
                board[x][y] = piece;
                board[currentX][currentY] = originalPiece;

                if (isGeneralInCheck || isFacingGenerals)
                {
                    if (originalPiece != Piece.EMPTY) obstacle++;
                    currentX += stepX;
                    currentY += stepY;
                    continue; // Bỏ qua nước đi này vì làm Tướng bị chiếu hoặc chạm mặt Tướng
                }
                if (board[currentX][currentY] != Piece.EMPTY)
                {
                    if (obstacle == 0)
                    {
                        obstacle++;// Đã gặp quân chắn đường
                    }
                    else
                    {
                        //if (!isOpponentPiece(piece, board))
                        // Nếu đã có quân chắn đường và gặp quân đối phương, có thể ăn
                        if (RuleService.isOpponentPiece(board[currentX][currentY], pieceColor) && obstacle == 1)
                        {
                            possibleMoves.add(new Position(currentX, currentY));
                        }
                        break; // Dừng lại sau khi gặp quân cờ thứ hai
                    }
                }
                else
                {
                    if (obstacle == 0)
                    {
                        possibleMoves.add(new Position(currentX, currentY)); // Ô trống trước khi gặp quân chắn đường
                    }
                }

                currentX += stepX;
                currentY += stepY;
            }
        }

        return possibleMoves;
    }
}
