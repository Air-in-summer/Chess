import React from 'react';
import { useWebSocket } from './hooks/useWebSocket';
import { HomeScreen } from './screens/HomeScreen';
import { LobbyScreen } from './screens/LobbyScreen';
import { GameScreen } from './screens/GameScreen';
import './App.css';

function App() {
    const { 
        connected, 
        currentScreen, 
        errorMsg, 
        createRoom, 
        joinRoom,
        roomState,
        gameState,
        gameResultModal,
        lastMove,
        mySessionId,
        mySide,
        ready,
        unready,
        leave,
        sendMove
    } = useWebSocket();

    return (
        <div className="app-container">
            {currentScreen === 'HOME' && (
                <HomeScreen 
                    createRoom={createRoom} 
                    joinRoom={joinRoom} 
                    connected={connected} 
                    errorMsg={errorMsg} 
                />
            )}
            
            {currentScreen === 'LOBBY' && (
                <LobbyScreen 
                    roomState={roomState}
                    mySessionId={mySessionId}
                    ready={ready}
                    unready={unready}
                    leave={leave}
                />
            )}
            
            {currentScreen === 'GAME' && (
                <GameScreen 
                    gameState={gameState}
                    mySide={mySide}
                    mySessionId={mySessionId}
                    gameResultModal={gameResultModal}
                    lastMove={lastMove}
                    onMove={sendMove}
                    leave={leave}
                />
            )}
        </div>
    );
}

export default App;
