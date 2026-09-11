import React from 'react';
import { GameResult, Side } from '../types/protocol';
import type { GamePayload } from '../types/protocol';
import './GameOverModal.css';

interface Props {
    gameResult: GamePayload;
    mySessionId: string | null;
    onClose: () => void;
}

export const GameOverModal: React.FC<Props> = ({ gameResult, mySessionId, onClose }) => {
    let title = "Hòa Cờ";
    let message = gameResult.reason || "Trận đấu kết thúc với kết quả hòa.";
    let statusClass = "draw";

    if (gameResult.winner === mySessionId) {
        title = "Bạn Đã Thắng!";
        statusClass = "win";
    } else if (gameResult.loser === mySessionId) {
        title = "Bạn Đã Thua!";
        statusClass = "lose";
    } else if (gameResult.result === GameResult.RED_WIN) {
        // Fallback in case winner/loser is not set but redPlayerId is
        if (gameResult.redPlayerId === mySessionId) {
            title = "Bạn Đã Thắng!";
            statusClass = "win";
        } else {
            title = "Bạn Đã Thua!";
            statusClass = "lose";
        }
    } else if (gameResult.result === GameResult.BLACK_WIN) {
        // Fallback
        if (gameResult.blackPlayerId === mySessionId) {
            title = "Bạn Đã Thắng!";
            statusClass = "win";
        } else {
            title = "Bạn Đã Thua!";
            statusClass = "lose";
        }
    }

    return (
        <div className="modal-overlay">
            <div className={`modal-content ${statusClass}`}>
                <h1 className="modal-title">{title}</h1>
                <p className="modal-reason">{message}</p>
                <div className="modal-actions">
                    <button className="btn primary" onClick={onClose}>
                        Xác Nhận & Thoát
                    </button>
                </div>
            </div>
        </div>
    );
};
