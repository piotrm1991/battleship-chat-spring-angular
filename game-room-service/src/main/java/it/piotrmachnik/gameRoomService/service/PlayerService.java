package it.piotrmachnik.gameRoomService.service;

import it.piotrmachnik.gameRoomService.model.gameRoom.player.Player;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerGameStatusType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerOnlineStatusType;
import it.piotrmachnik.gameRoomService.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserService userService;

    public Player getPlayerByUserId(String userId) {
        if (this.playerRepository.findById(userId).isPresent()) {
            Player player = this.playerRepository.findById(userId).get();
            player.setPlayerOnlineStatus(PlayerOnlineStatusType.ONLINE);
            return this.playerRepository.save(player);
        } else {
            return this.playerRepository.save(Player.builder()
                    .id(userId)
                    .name(this.userService.getUserName(userId))
                    .playerOnlineStatus(PlayerOnlineStatusType.ONLINE)
                    .playerGameStatus(PlayerGameStatusType.NOT_READY)
                    .build());
        }
    }

    public Player gerPlayerById(String playerId) {
        if (this.playerRepository.findById(playerId).isPresent()) {
            return this.playerRepository.findById(playerId).get();
        } else {
            return null;
        }
    }

    public Player disconnectPlayer(String playerId) {
        Player player = this.playerRepository.findById(playerId).get();
        player.setPlayerOnlineStatus(PlayerOnlineStatusType.OFFLINE);
        return this.playerRepository.save(player);
    }
}
