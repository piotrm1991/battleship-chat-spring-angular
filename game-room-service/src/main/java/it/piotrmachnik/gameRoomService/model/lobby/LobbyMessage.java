package it.piotrmachnik.gameRoomService.model.lobby;

import it.piotrmachnik.gameRoomService.model.MessageType;
import lombok.Builder;
import lombok.Getter;

@Builder
public class LobbyMessage {
    @Getter
    private MessageType type;
    @Getter
    private String senderId;
    @Getter
    private String sender;
    @Getter
    private String gameRoomId;
}
