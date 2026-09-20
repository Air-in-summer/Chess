import React, { useEffect, useState } from 'react';
import { Side } from '../types/protocol';
import './PlayerInfo.css';

interface Props {
    side: Side;
    timeMillis: number;
    isTurn: boolean;
    isMe: boolean;
}

export const PlayerInfo: React.FC<Props> = ({ side, timeMillis, isTurn, isMe }) => {
    const [displayTime, setDisplayTime] = useState(timeMillis);

    useEffect(() => {
        setDisplayTime(timeMillis);
        
        let interval: number;
        if (isTurn && timeMillis > 0) {
            interval = window.setInterval(() => {
                setDisplayTime(prev => Math.max(0, prev - 1000));
            }, 1000);
        }
        
        return () => {
            if (interval) clearInterval(interval);
        };
    }, [timeMillis, isTurn]);

    const formatTime = (ms: number) => {
        const totalSeconds = Math.floor(ms / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    };

    const sideName = side === Side.RED ? 'Cờ Đỏ' : 'Cờ Đen';
    const sideClass = side === Side.RED ? 'red-side' : 'black-side';
    const avatarChar = side === Side.RED ? '帥' : '將';

    return (
        <div className={`player-info-container ${isTurn ? 'active-turn' : ''} ${sideClass}`}>
            <div className="player-details">
                <div className="avatar notranslate" translate="no">
                    {avatarChar}
                </div>
                <div className="info-text">
                    <div className="name">{isMe ? 'Tôi' : 'Đối thủ'}</div>
                    <div className="side-label">{sideName}</div>
                </div>
            </div>
            
            <div className="timer-box">
                <span className={`time ${displayTime < 30000 ? 'warning' : ''}`}>
                    {formatTime(displayTime)}
                </span>
                {isTurn && <div className="turn-indicator">Đang suy nghĩ...</div>}
            </div>
        </div>
    );
};
