package it.piotrmachnik.gameRoomService.model.chat;

import it.piotrmachnik.gameRoomService.model.MessageType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatMessage {
    private MessageType type;
    private String content;
    private String senderId;
    private String sender;
    private String receiver;
    private String gameRoomId;
    private String time;
}
