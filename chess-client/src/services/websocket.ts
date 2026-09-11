import type { ClientMessage, ServerMessage } from '../types/protocol';

type MessageHandler = (message: ServerMessage) => void;
type ConnectionHandler = (connected: boolean) => void;

class WebSocketService {
    private ws: WebSocket | null = null;
    private messageHandlers: Set<MessageHandler> = new Set();
    private connectionHandlers: Set<ConnectionHandler> = new Set();
    private reconnectTimer: number | null = null;
    private isIntentionallyClosed: boolean = false;

    public connect(url: string = 'ws://localhost:8080/ws/chess') {
        if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
            return;
        }

        this.isIntentionallyClosed = false;
        this.ws = new WebSocket(url);

        this.ws.onopen = () => {
            console.log('[WebSocket] Connected');
            this.notifyConnectionChange(true);
            if (this.reconnectTimer) {
                clearTimeout(this.reconnectTimer);
                this.reconnectTimer = null;
            }
        };

        this.ws.onmessage = (event) => {
            try {
                const message: ServerMessage = JSON.parse(event.data);
                this.messageHandlers.forEach(handler => handler(message));
            } catch (err) {
                console.error('[WebSocket] Failed to parse message:', err);
            }
        };

        this.ws.onclose = () => {
            console.log('[WebSocket] Disconnected');
            this.notifyConnectionChange(false);
            this.ws = null;
            
            if (!this.isIntentionallyClosed) {
                this.scheduleReconnect(url);
            }
        };

        this.ws.onerror = (error) => {
            console.error('[WebSocket] Error:', error);
        };
    }

    private scheduleReconnect(url: string) {
        if (!this.reconnectTimer) {
            this.reconnectTimer = window.setTimeout(() => {
                this.reconnectTimer = null;
                console.log('[WebSocket] Attempting to reconnect...');
                this.connect(url);
            }, 3000);
        }
    }

    public disconnect() {
        this.isIntentionallyClosed = true;
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }
    }

    public send(message: ClientMessage) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        } else {
            console.error('[WebSocket] Cannot send message, not connected.');
        }
    }

    public onMessage(handler: MessageHandler) {
        this.messageHandlers.add(handler);
        return () => this.messageHandlers.delete(handler);
    }

    public onConnectionChange(handler: ConnectionHandler) {
        this.connectionHandlers.add(handler);
        // Call immediately with current status
        handler(this.ws !== null && this.ws.readyState === WebSocket.OPEN);
        return () => this.connectionHandlers.delete(handler);
    }

    private notifyConnectionChange(connected: boolean) {
        this.connectionHandlers.forEach(handler => handler(connected));
    }
}

export const wsService = new WebSocketService();
