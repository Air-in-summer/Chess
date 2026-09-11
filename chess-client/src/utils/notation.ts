import { Piece } from '../types/protocol';

export function generateNotation(board: Piece[][], fromX: number, fromY: number, toX: number, toY: number, piece: Piece): string {
    const isRed = piece.startsWith('r');
    
    // 1. Ký hiệu quân cờ
    let char = '';
    const type = piece.charAt(1);
    if (type === 'G') char = 'Tg';
    else if (type === 'A') char = 'S';
    else if (type === 'B') char = 'T';
    else if (type === 'K') char = 'M';
    else if (type === 'R') char = 'X';
    else if (type === 'C') char = 'P';
    else if (type === 'P') char = 'B';

    // 2. Kiểm tra Trùng cột (Tiền/Hậu)
    let prefix = '';
    const samePieces: {row: number, col: number}[] = [];
    for (let r = 0; r < 10; r++) {
        if (board[r][fromY] === piece) {
            samePieces.push({row: r, col: fromY});
        }
    }
    
    // Nếu có đúng 2 quân trùng cột (Xe, Pháo, Mã)
    if (samePieces.length === 2 && type !== 'G' && type !== 'A' && type !== 'B') {
        samePieces.sort((a, b) => a.row - b.row);
        const [top, bottom] = samePieces;
        if (isRed) {
            // Đỏ ở dưới đáy (row = 9), nên row nhỏ hơn (top) là Tiền
            if (fromX === top.row) prefix = '+';
            else prefix = '-';
        } else {
            // Đen ở trên đỉnh (row = 0), nên row lớn hơn (bottom) là Tiền
            if (fromX === bottom.row) prefix = '+';
            else prefix = '-';
        }
    }
    // (Bỏ qua xử lý 3-5 Tốt trùng cột vì hiếm gặp và phức tạp)

    // 3. Cột xuất phát
    const startCol = isRed ? (9 - fromY) : (fromY + 1);

    // 4. Hành động
    let action = '';
    if (toX === fromX) {
        action = '=';
    } else {
        if (isRed) {
            action = (toX < fromX) ? '.' : '/'; // Đỏ tiến lên (giảm row)
        } else {
            action = (toX > fromX) ? '.' : '/'; // Đen tiến xuống (tăng row)
        }
    }

    // 5. Đích đến
    let dest = '';
    const isDiagonal = (type === 'A' || type === 'B' || type === 'K');
    if (action === '=') {
        dest = (isRed ? (9 - toY) : (toY + 1)).toString();
    } else {
        if (isDiagonal) {
            // Quân đi chéo (Sĩ, Tượng, Mã) luôn ghi cột đích
            dest = (isRed ? (9 - toY) : (toY + 1)).toString();
        } else {
            // Quân đi thẳng (Xe, Pháo, Tốt, Tướng) ghi số bước đi được
            dest = Math.abs(toX - fromX).toString();
        }
    }

    // 6. Ráp chuỗi hoàn chỉnh
    if (prefix) {
        return `${prefix}${char}${action}${dest}`; // VD: +P=5 (Tiền Pháo bình 5)
    } else {
        return `${char}${startCol}${action}${dest}`; // VD: M2.3 (Mã 2 tấn 3)
    }
}
