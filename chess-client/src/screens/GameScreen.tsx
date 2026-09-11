import React, { useState } from 'react';
import { Side } from '../types/protocol';
import type { GamePayload } from '../types/protocol';
import { ChessBoard } from '../components/ChessBoard';
import { PlayerInfo } from '../components/PlayerInfo';
import { GameOverModal } from '../components/GameOverModal';
import { GameSettings } from '../components/GameSettings';
import { MoveHistory } from '../components/MoveHistory';
import type { MoveRecord } from '../hooks/useWebSocket';
import './GameScreen.css';

interface Props {
    gameState: GamePayload | null;
    mySide: Side | null;
    mySessionId: string | null;
    gameResultModal: GamePayload | null;
    lastMove: { fromX: number; fromY: number; toX: number; toY: number } | null;
    moveHistory: MoveRecord[];
    onMove: (fromX: number, fromY: number, toX: number, toY: number) => void;
    leave: () => void;
}

export const GameScreen: React.FC<Props> = ({ 
    gameState, mySide, mySessionId, gameResultModal, lastMove, moveHistory, onMove, leave 
}) => {
    if (!gameState || !mySide) return <div style={{ color: 'white', textAlign: 'center', marginTop: '50px' }}>Đang tải bàn cờ...</div>;

    const isRedTurn = gameState.redTurn ?? true;
    const isMyTurn = (mySide === Side.RED && isRedTurn) || (mySide === Side.BLACK && !isRedTurn);
    const opponentSide = mySide === Side.RED ? Side.BLACK : Side.RED;
    
    return (
        <div className="game-container">
            {/* Cột Trái: Thông tin người chơi (Sidebar) */}
            <div className="sidebar">
                {/* Top: Đối thủ */}
                <PlayerInfo 
                    side={opponentSide}
                    timeMillis={opponentSide === Side.RED ? (gameState.redTime || 0) : (gameState.blackTime || 0)}
                    isTurn={opponentSide === Side.RED ? isRedTurn : !isRedTurn}
                    isMe={false}
                />

                <div className="sidebar-middle" style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '10px', marginBottom: '10px' }}>
                    <MoveHistory history={moveHistory} />
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '10px' }}>
                    <GameSettings onSurrender={leave} />
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
                    board={gameState.board || []}
                    mySide={mySide}
                    isMyTurn={isMyTurn}
                    onMove={onMove}
                    lastMove={lastMove}
                    checkSide={gameState.checkSide}
                />
            </div>

            {/* Modal Game Over */}
            {gameResultModal && (
                <GameOverModal 
                    gameResult={gameResultModal} 
                    mySessionId={mySessionId} 
                    onClose={leave} 
                />
            )}
        </div>
    );
};
