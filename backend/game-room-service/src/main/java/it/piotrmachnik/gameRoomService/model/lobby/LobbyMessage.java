package it.piotrmachnik.gameRoomService.model.lobby;

import it.piotrmachnik.gameRoomService.model.MessageType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LobbyMessage {
    private MessageType type;
    private String senderId;
    private String sender;
    private String gameRoomId;
}
