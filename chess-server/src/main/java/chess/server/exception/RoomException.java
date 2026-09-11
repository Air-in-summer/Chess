package chess.server.exception;

import chess.server.protocol.ErrorCode;
import chess.server.protocol.MessageType;

public class RoomException extends RuntimeException {

    private final MessageType messageType;
    private final ErrorCode errorCode;

    public RoomException(MessageType messageType, ErrorCode errorCode, String message) {
        super(message);
        this.messageType = messageType;
        this.errorCode = errorCode;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
