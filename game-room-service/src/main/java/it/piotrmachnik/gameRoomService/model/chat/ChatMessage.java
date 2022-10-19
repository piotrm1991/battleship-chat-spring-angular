package it.piotrmachnik.gameRoomService.model.chat;

import it.piotrmachnik.gameRoomService.model.MessageType;
import lombok.Builder;
import lombok.Getter;

@Builder
public class ChatMessage {
    @Getter
    private MessageType type;
    @Getter
    private String content;
    @Getter
    private String sender;
    @Getter
    private String receiver;
    @Getter
    private String gameRoomId;
    @Getter
    private String time;
}
