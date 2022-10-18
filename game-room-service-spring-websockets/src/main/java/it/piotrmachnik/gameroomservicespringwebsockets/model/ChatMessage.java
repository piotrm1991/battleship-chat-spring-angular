package it.piotrmachnik.gameroomservicespringwebsockets.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class ChatMessage {
    @Getter
    private MessageType type;
    @Getter
    @Setter
    private String content;
    @Getter
    private String sender;
    @Getter
    private String time;
    @Getter
    private String receiver;
}
