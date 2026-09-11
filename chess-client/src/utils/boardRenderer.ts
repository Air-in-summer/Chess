import { Piece } from '../types/protocol';
import type { Position } from '../types/protocol';
import { PIECE_MAP } from './pieceMap';

// ===== HẰNG SỐ KÍCH THƯỚC =====
export const CELL_SIZE = 62;
export const PADDING = 45;
export const PIECE_RADIUS = 26;
export const BOARD_COLS = 9;
export const BOARD_ROWS = 10;
export const CANVAS_WIDTH = (BOARD_COLS - 1) * CELL_SIZE + PADDING * 2;
export const CANVAS_HEIGHT = (BOARD_ROWS - 1) * CELL_SIZE + PADDING * 2;

// ===== HÀM CHUYỂN ĐỔI TỌA ĐỘ =====

/** Chuyển tọa độ logic (row, col) sang pixel (px, py) trên Canvas */
export function toPixel(row: number, col: number, isFlipped: boolean): { px: number; py: number } {
    const r = isFlipped ? 9 - row : row;
    const c = isFlipped ? 8 - col : col;
    return {
        px: PADDING + c * CELL_SIZE,
        py: PADDING + r * CELL_SIZE,
    };
}

/** Chuyển tọa độ pixel (px, py) trên Canvas sang tọa độ logic (row, col) */
export function toLogic(px: number, py: number, isFlipped: boolean): { row: number; col: number } | null {
    const c = Math.round((px - PADDING) / CELL_SIZE);
    const r = Math.round((py - PADDING) / CELL_SIZE);

    if (c < 0 || c > 8 || r < 0 || r > 9) return null;

    // Kiểm tra click có đủ gần giao điểm không (trong bán kính quân cờ)
    const { px: snapPx, py: snapPy } = toPixel(isFlipped ? 9 - r : r, isFlipped ? 8 - c : c, false);
    const actualPx = PADDING + c * CELL_SIZE;
    const actualPy = PADDING + r * CELL_SIZE;
    const dist = Math.sqrt((px - actualPx) ** 2 + (py - actualPy) ** 2);
    if (dist > PIECE_RADIUS + 5) return null;

    const row = isFlipped ? 9 - r : r;
    const col = isFlipped ? 8 - c : c;
    return { row, col };
}

// ===== VẼ BÀN CỜ =====

/** Vẽ toàn bộ bàn cờ (nền, lưới, sông, cung, dấu sao) */
export function drawBoard(ctx: CanvasRenderingContext2D) {
    const width = CANVAS_WIDTH;
    const height = CANVAS_HEIGHT;

    // --- Nền gỗ ---
    const gradient = ctx.createLinearGradient(0, 0, width, height);
    gradient.addColorStop(0, '#deb370');
    gradient.addColorStop(0.5, '#d4a55a');
    gradient.addColorStop(1, '#c89640');
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, width, height);

    // Viền ngoài bàn cờ
    ctx.strokeStyle = '#5a3e1b';
    ctx.lineWidth = 3;
    ctx.strokeRect(PADDING - 8, PADDING - 8, (BOARD_COLS - 1) * CELL_SIZE + 16, (BOARD_ROWS - 1) * CELL_SIZE + 16);

    ctx.strokeStyle = '#5a3e1b';
    ctx.lineWidth = 1.5;

    // --- Đường ngang (10 đường) ---
    for (let row = 0; row < BOARD_ROWS; row++) {
        const y = PADDING + row * CELL_SIZE;
        ctx.beginPath();
        ctx.moveTo(PADDING, y);
        ctx.lineTo(PADDING + (BOARD_COLS - 1) * CELL_SIZE, y);
        ctx.stroke();
    }

    // --- Đường dọc (9 đường) ---
    for (let col = 0; col < BOARD_COLS; col++) {
        if (col === 0 || col === 8) {
            // Hai đường biên chạy liền từ trên xuống dưới
            const x = PADDING + col * CELL_SIZE;
            ctx.beginPath();
            ctx.moveTo(x, PADDING);
            ctx.lineTo(x, PADDING + (BOARD_ROWS - 1) * CELL_SIZE);
            ctx.stroke();
        } else {
            // 7 đường giữa bị ngắt bởi sông
            const x = PADDING + col * CELL_SIZE;
            // Nửa trên (hàng 0-4)
            ctx.beginPath();
            ctx.moveTo(x, PADDING);
            ctx.lineTo(x, PADDING + 4 * CELL_SIZE);
            ctx.stroke();
            // Nửa dưới (hàng 5-9)
            ctx.beginPath();
            ctx.moveTo(x, PADDING + 5 * CELL_SIZE);
            ctx.lineTo(x, PADDING + (BOARD_ROWS - 1) * CELL_SIZE);
            ctx.stroke();
        }
    }

    // --- Đường chéo cung Tướng ---
    drawPalaceDiagonals(ctx, 0, 3); // Cung bên Đen (hàng 0-2, cột 3-5)
    drawPalaceDiagonals(ctx, 7, 3); // Cung bên Đỏ (hàng 7-9, cột 3-5)

    // --- Chữ 楚河 漢界 trên sông ---
    drawRiverText(ctx);

    // --- Dấu sao (hoa thị) tại vị trí Pháo và Tốt ---
    drawStarMarks(ctx);
}

/** Vẽ đường chéo trong cung Tướng */
function drawPalaceDiagonals(ctx: CanvasRenderingContext2D, startRow: number, startCol: number) {
    const x1 = PADDING + startCol * CELL_SIZE;
    const y1 = PADDING + startRow * CELL_SIZE;
    const x2 = PADDING + (startCol + 2) * CELL_SIZE;
    const y2 = PADDING + (startRow + 2) * CELL_SIZE;

    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(x2, y1);
    ctx.lineTo(x1, y2);
    ctx.stroke();
}

/** Vẽ chữ 楚河 漢界 */
function drawRiverText(ctx: CanvasRenderingContext2D) {
    const riverY = PADDING + 4.5 * CELL_SIZE;

    ctx.save();
    ctx.font = 'bold 28px "Noto Serif SC", serif';
    ctx.fillStyle = '#5a3e1b';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    // 楚河 bên trái
    const leftX = PADDING + 1.5 * CELL_SIZE;
    ctx.fillText('楚', leftX - 16, riverY);
    ctx.fillText('河', leftX + 22, riverY);

    // 漢界 bên phải
    const rightX = PADDING + 6.5 * CELL_SIZE;
    ctx.fillText('漢', rightX - 16, riverY);
    ctx.fillText('界', rightX + 22, riverY);

    ctx.restore();
}

/** Vẽ dấu sao (十字) tại các vị trí Pháo và Tốt ban đầu */
function drawStarMarks(ctx: CanvasRenderingContext2D) {
    // Vị trí Pháo: (2,1), (2,7), (7,1), (7,7)
    // Vị trí Tốt: (3,0), (3,2), (3,4), (3,6), (3,8), (6,0), (6,2), (6,4), (6,6), (6,8)
    const positions = [
        [2, 1], [2, 7], [7, 1], [7, 7],
        [3, 0], [3, 2], [3, 4], [3, 6], [3, 8],
        [6, 0], [6, 2], [6, 4], [6, 6], [6, 8],
    ];

    const markLen = 6;
    const gap = 4;

    ctx.strokeStyle = '#5a3e1b';
    ctx.lineWidth = 1.2;

    for (const [row, col] of positions) {
        const cx = PADDING + col * CELL_SIZE;
        const cy = PADDING + row * CELL_SIZE;

        // Vẽ 4 góc L nhỏ xung quanh giao điểm
        // Chỉ vẽ bên trái nếu col > 0
        if (col > 0) {
            // Trên-trái
            ctx.beginPath();
            ctx.moveTo(cx - gap, cy - gap - markLen);
            ctx.lineTo(cx - gap, cy - gap);
            ctx.lineTo(cx - gap - markLen, cy - gap);
            ctx.stroke();
            // Dưới-trái
            ctx.beginPath();
            ctx.moveTo(cx - gap, cy + gap + markLen);
            ctx.lineTo(cx - gap, cy + gap);
            ctx.lineTo(cx - gap - markLen, cy + gap);
            ctx.stroke();
        }
        // Chỉ vẽ bên phải nếu col < 8
        if (col < 8) {
            // Trên-phải
            ctx.beginPath();
            ctx.moveTo(cx + gap, cy - gap - markLen);
            ctx.lineTo(cx + gap, cy - gap);
            ctx.lineTo(cx + gap + markLen, cy - gap);
            ctx.stroke();
            // Dưới-phải
            ctx.beginPath();
            ctx.moveTo(cx + gap, cy + gap + markLen);
            ctx.lineTo(cx + gap, cy + gap);
            ctx.lineTo(cx + gap + markLen, cy + gap);
            ctx.stroke();
        }
    }
}

// ===== VẼ QUÂN CỜ =====

/** Vẽ tất cả quân cờ trên bàn cờ */
export function drawPieces(ctx: CanvasRenderingContext2D, board: Piece[][], isFlipped: boolean, checkSide?: string | null) {
    for (let row = 0; row < 10; row++) {
        for (let col = 0; col < 9; col++) {
            const piece = board[row][col];
            if (piece && piece !== Piece.EMPTY) {
                const sideUpper = checkSide ? checkSide.toUpperCase() : null;
                const isChecked = (piece === Piece.rG && sideUpper === 'RED') || 
                                  (piece === Piece.bG && sideUpper === 'BLACK');
                drawSinglePiece(ctx, piece, row, col, isFlipped, isChecked);
            }
        }
    }
}

/** Vẽ 1 quân cờ tại giao điểm (row, col) */
function drawSinglePiece(ctx: CanvasRenderingContext2D, piece: Piece, row: number, col: number, isFlipped: boolean, isChecked: boolean = false) {
    const info = PIECE_MAP[piece];
    if (!info) return;

    const { px, py } = toPixel(row, col, isFlipped);

    ctx.save();

    // --- Hiệu ứng Chiếu Tướng (Glow Đỏ Gắt) ---
    if (isChecked) {
        ctx.beginPath();
        ctx.arc(px, py, PIECE_RADIUS + 12, 0, Math.PI * 2);
        // Nền đỏ đậm
        ctx.fillStyle = 'rgba(255, 0, 0, 0.7)';
        ctx.shadowColor = '#ff0000';
        ctx.shadowBlur = 30; // Phát sáng mạnh
        ctx.fill();
        
        // Viền đỏ tươi bao quanh
        ctx.strokeStyle = '#ff0000';
        ctx.lineWidth = 4;
        ctx.stroke();
        
        ctx.shadowBlur = 0; // Reset shadow
    }

    // --- Bóng đổ ---
    ctx.beginPath();
    ctx.arc(px + 2, py + 2, PIECE_RADIUS, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(0, 0, 0, 0.15)';
    ctx.fill();

    // --- Nền quân cờ ---
    ctx.beginPath();
    ctx.arc(px, py, PIECE_RADIUS, 0, Math.PI * 2);
    ctx.fillStyle = info.fillColor;
    ctx.fill();

    // --- Viền ngoài ---
    ctx.strokeStyle = info.color;
    ctx.lineWidth = 2.5;
    ctx.stroke();

    // --- Viền trong (trang trí) ---
    ctx.beginPath();
    ctx.arc(px, py, PIECE_RADIUS - 5, 0, Math.PI * 2);
    ctx.strokeStyle = info.color;
    ctx.lineWidth = 1;
    ctx.stroke();

    // --- Chữ Hán ---
    ctx.font = `bold ${PIECE_RADIUS}px "Noto Serif SC", "SimSun", serif`;
    ctx.fillStyle = info.color;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(info.char, px, py + 1);

    ctx.restore();
}

// ===== VẼ HIGHLIGHT =====

/** Vẽ viền sáng quanh quân đang được chọn */
export function drawSelection(ctx: CanvasRenderingContext2D, row: number, col: number, isFlipped: boolean) {
    const { px, py } = toPixel(row, col, isFlipped);

    ctx.save();
    ctx.beginPath();
    ctx.arc(px, py, PIECE_RADIUS + 3, 0, Math.PI * 2);
    ctx.strokeStyle = '#f1c40f';
    ctx.lineWidth = 3;
    ctx.shadowColor = '#f1c40f';
    ctx.shadowBlur = 10;
    ctx.stroke();
    ctx.restore();
}

/** Vẽ các chấm xanh tại các nước đi hợp lệ */
export function drawLegalMoves(ctx: CanvasRenderingContext2D, moves: Position[], board: Piece[][], isFlipped: boolean) {
    ctx.save();

    for (const move of moves) {
        const { px, py } = toPixel(move.x, move.y, isFlipped);
        const targetPiece = board[move.x][move.y];

        if (targetPiece && targetPiece !== Piece.EMPTY) {
            // Ô có quân đối phương → vẽ vòng tròn đỏ nhạt (có thể ăn)
            ctx.beginPath();
            ctx.arc(px, py, PIECE_RADIUS + 3, 0, Math.PI * 2);
            ctx.strokeStyle = 'rgba(231, 76, 60, 0.7)';
            ctx.lineWidth = 3;
            ctx.stroke();
        } else {
            // Ô trống → vẽ chấm xanh lá
            ctx.beginPath();
            ctx.arc(px, py, 8, 0, Math.PI * 2);
            ctx.fillStyle = 'rgba(46, 204, 113, 0.7)';
            ctx.fill();
        }
    }

    ctx.restore();
}

/** Vẽ highlight nước đi vừa thực hiện (fromX,fromY → toX,toY) */
export function drawLastMove(
    ctx: CanvasRenderingContext2D,
    fromRow: number, fromCol: number,
    toRow: number, toCol: number,
    isFlipped: boolean
) {
    ctx.save();
    ctx.globalAlpha = 0.35;

    // Ô xuất phát
    const from = toPixel(fromRow, fromCol, isFlipped);
    ctx.fillStyle = '#3498db';
    ctx.fillRect(from.px - CELL_SIZE / 2, from.py - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);

    // Ô đích
    const to = toPixel(toRow, toCol, isFlipped);
    ctx.fillStyle = '#2ecc71';
    ctx.fillRect(to.px - CELL_SIZE / 2, to.py - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);

    ctx.restore();
}
