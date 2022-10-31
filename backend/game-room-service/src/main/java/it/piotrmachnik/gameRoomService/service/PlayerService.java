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

    public PlayerService(final PlayerRepository playerRepository, final UserService userService) {
        this.playerRepository = playerRepository;
        this.userService = userService;
    }

    public PlayerService() {
    }

    public Player setPlayerOnlineOrInitNewPlayer(String userId) {
        if (this.playerRepository.findById(userId).isPresent()) {
            Player player = this.playerRepository.findById(userId).get();
            player.setPlayerOnlineStatus(PlayerOnlineStatusType.ONLINE);
            return player;
        } else {
            return Player.builder()
                    .id(userId)
                    .name(this.userService.getUserName(userId))
                    .playerOnlineStatus(PlayerOnlineStatusType.ONLINE)
                    .playerGameStatus(PlayerGameStatusType.NOT_READY)
                    .build();
        }
    }

    public Player savePlayer(Player player) {
        return this.playerRepository.save(player);
    }

    public Player getPlayerById(String playerId) {
        return this.playerRepository.findById(playerId).get();
    }

    public void deletePlayer(Player player) {
        this.playerRepository.delete(player);
    }

    public Player setPlayerOffline(String playerId) {
        Player player = this.playerRepository.findById(playerId).get();
        player.setPlayerOnlineStatus(PlayerOnlineStatusType.OFFLINE);
        return player;
    }

    public Player setGameRoomIdForPlayer(String currentPlayerId, String gameRoomId) {
        Player player = this.playerRepository.findById(currentPlayerId).get();
        player.setGameRoomId(gameRoomId);
        return player;
    }
}
