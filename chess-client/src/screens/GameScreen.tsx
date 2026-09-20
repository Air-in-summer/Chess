import React, { useState, useEffect } from 'react';
import { Side } from '../types/protocol';
import type { GamePayload } from '../types/protocol';
import { ChessBoard } from '../components/ChessBoard';
import { PlayerInfo } from '../components/PlayerInfo';
import { GameOverModal } from '../components/GameOverModal';
import { GameSettings } from '../components/GameSettings';
import { MoveHistory } from '../components/MoveHistory';
import type { MoveRecord, HistoryStep } from '../hooks/useWebSocket';
import './GameScreen.css';

interface Props {
    gameState: GamePayload | null;
    mySide: Side | null;
    mySessionId: string | null;
    gameResultModal: GamePayload | null;
    lastMove: { fromX: number; fromY: number; toX: number; toY: number } | null;
    // Danh sách biên bản nước đi dạng Text (ví dụ: "M8.7", "P2.5") dùng để in ra bảng hiển thị lịch sử ở giao diện.
    moveHistory: MoveRecord[];
    // Mảng lưu trữ trực tiếp "ảnh chụp" toàn bộ mảng bàn cờ (Piece[][]) ở từng bước. 
    // Chuyên phục vụ cho tính năng Tua lại ván đấu (Replay Mode) vẽ lại lên Canvas.
    historySteps: HistoryStep[];
    onMove: (fromX: number, fromY: number, toX: number, toY: number) => void;
    leave: () => void;
    surrender: () => void;
}

export const GameScreen: React.FC<Props> = ({ 
    gameState, mySide, mySessionId, gameResultModal, lastMove, moveHistory, historySteps, onMove, leave, surrender 
}) => {
    const [hideModal, setHideModal] = useState(false);
    const [replayIndex, setReplayIndex] = useState<number>(-1);

    // Bắt sự kiện phím mũi tên trái phải khi đang ẩn Modal (chế độ xem lại)
    useEffect(() => {
        if (gameResultModal && hideModal && historySteps.length > 0) {
            const handleKeyDown = (e: KeyboardEvent) => {
                if (e.key === 'ArrowLeft') {
                    setReplayIndex(prev => Math.max(0, prev - 1));
                } else if (e.key === 'ArrowRight') {
                    setReplayIndex(prev => Math.min(historySteps.length - 1, prev + 1));
                }
            };
            window.addEventListener('keydown', handleKeyDown);
            return () => window.removeEventListener('keydown', handleKeyDown);
        }
    }, [gameResultModal, hideModal, historySteps.length]);

    if (!gameState || !mySide) return <div style={{ color: 'white', textAlign: 'center', marginTop: '50px' }}>Đang tải bàn cờ...</div>;

    const isGameOver = !!gameResultModal;
    const isRedTurn = gameState.redTurn ?? true;
    const isMyTurn = !isGameOver && ((mySide === Side.RED && isRedTurn) || (mySide === Side.BLACK && !isRedTurn));
    const opponentSide = mySide === Side.RED ? Side.BLACK : Side.RED;

    // Tính toán trạng thái hiển thị của bàn cờ dựa vào replayIndex
    const isReplayMode = isGameOver && hideModal && replayIndex >= 0 && replayIndex < historySteps.length;
    const displayBoard = isReplayMode ? historySteps[replayIndex].board : (gameState.board || []);
    const displayLastMove = isReplayMode ? historySteps[replayIndex].lastMove : lastMove;
    // Khi tua lại thì ta tạm thời tắt checkSide
    const displayCheckSide = isReplayMode ? null : gameState.checkSide;
    
    return (
        <div className="game-container">
            {/* Nút thoát nổi khi đang xem lại bàn cờ */}
            {isGameOver && hideModal && (
                <div style={{ position: 'fixed', top: '20px', left: '20px', zIndex: 100 }}>
                    <button 
                        className="btn primary" 
                        style={{ boxShadow: '0 4px 6px rgba(0,0,0,0.3)' }}
                        onClick={leave}
                    >
                        Thoát Về Sảnh
                    </button>
                </div>
            )}

            {/* Cột Trái: Thông tin người chơi (Sidebar) */}
            <div className="sidebar">
                {/* Top: Đối thủ */}
                <PlayerInfo 
                    side={opponentSide}
                    timeMillis={opponentSide === Side.RED ? (gameState.redTime || 0) : (gameState.blackTime || 0)}
                    isTurn={!isGameOver && (opponentSide === Side.RED ? isRedTurn : !isRedTurn)}
                    isMe={false}
                />

                <div className="sidebar-middle" style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '10px', marginBottom: '10px' }}>
                    <MoveHistory history={moveHistory} />
                    
                    {/* Bảng điều khiển tua lại */}
                    {isGameOver && hideModal && historySteps.length > 0 && (
                        <div style={{ background: 'rgba(0,0,0,0.4)', padding: '10px', borderRadius: '8px', color: 'white', display: 'flex', justifyContent: 'center', gap: '10px', marginTop: 'auto' }}>
                            <button 
                                onClick={() => setReplayIndex(prev => Math.max(0, prev - 1))}
                                disabled={replayIndex <= 0}
                                style={{ padding: '6px 16px', cursor: 'pointer', borderRadius: '4px', border: 'none', background: replayIndex <= 0 ? '#555' : '#3498db', color: 'white', fontWeight: 'bold' }}
                            >
                                ⬅️
                            </button>
                            <span style={{ lineHeight: '30px', minWidth: '40px', textAlign: 'center', fontWeight: 'bold' }}>
                                {replayIndex}/{historySteps.length - 1}
                            </span>
                            <button 
                                onClick={() => setReplayIndex(prev => Math.min(historySteps.length - 1, prev + 1))}
                                disabled={replayIndex >= historySteps.length - 1}
                                style={{ padding: '6px 16px', cursor: 'pointer', borderRadius: '4px', border: 'none', background: replayIndex >= historySteps.length - 1 ? '#555' : '#3498db', color: 'white', fontWeight: 'bold' }}
                            >
                                ➡️
                            </button>
                        </div>
                    )}
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '10px' }}>
                    <GameSettings onSurrender={isGameOver ? leave : surrender} isGameOver={isGameOver} />
                </div>

                {/* Bottom: Tôi */}
                <PlayerInfo 
                    side={mySide}
                    timeMillis={mySide === Side.RED ? (gameState.redTime || 0) : (gameState.blackTime || 0)}
                    isTurn={isMyTurn}
                    isMe={true}
                />
            </div>

            {/* Cột Phải: Bàn cờ */}
            <div className="board-section">
                <ChessBoard 
                    board={displayBoard}
                    mySide={mySide}
                    isMyTurn={isMyTurn && !isReplayMode}
                    onMove={onMove}
                    lastMove={displayLastMove}
                    checkSide={displayCheckSide}
                />
            </div>

            {/* Modal Game Over */}
            {gameResultModal && !hideModal && (
                <GameOverModal 
                    gameResult={gameResultModal} 
                    mySessionId={mySessionId} 
                    onClose={leave}
                    onHide={() => {
                        setHideModal(true);
                        setReplayIndex(historySteps.length - 1); // Bắt đầu ở nước đi cuối cùng
                    }}
                />
            )}
        </div>
    );
};
