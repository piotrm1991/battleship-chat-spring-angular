package it.piotrmachnik.gameRoomService.model.gameRoom;

import it.piotrmachnik.gameRoomService.model.MessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class GameRoomResponse {
    private GameRoom gameRoom;
    private MessageType messageType;
}
