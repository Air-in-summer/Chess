import React, { useEffect, useRef } from 'react';
import type { MoveRecord } from '../hooks/useWebSocket';
import './MoveHistory.css';

interface Props {
    history: MoveRecord[];
}

export const MoveHistory: React.FC<Props> = ({ history }) => {
    const tableRef = useRef<HTMLDivElement>(null);

    // Tự động cuộn xuống dưới cùng khi có nước đi mới
    useEffect(() => {
        if (tableRef.current) {
            tableRef.current.scrollTop = tableRef.current.scrollHeight;
        }
    }, [history]);

    return (
        <div className="move-history-container">
            <div className="history-header">
                <div className="col-stt">STT</div>
                <div className="col-red">Đỏ</div>
                <div className="col-black">Đen</div>
            </div>
            
            <div className="history-body" ref={tableRef}>
                {history.map((record, index) => (
                    <div className="history-row" key={index}>
                        <div className="col-stt">{index + 1}</div>
                        <div className="col-red">{record.red}</div>
                        <div className="col-black">{record.black}</div>
                    </div>
                ))}
                {history.length === 0 && (
                    <div className="history-empty">Chưa có nước đi</div>
                )}
            </div>
        </div>
    );
};
