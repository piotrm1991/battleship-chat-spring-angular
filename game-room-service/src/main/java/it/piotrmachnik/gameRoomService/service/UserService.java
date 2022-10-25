package it.piotrmachnik.gameRoomService.service;

import it.piotrmachnik.gameRoomService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public String getPlayerName(String playerId) {
        if (this.userRepository.findById(playerId).isPresent()) {
            return this.userRepository.findById(playerId).get().getUsername();
        } else {
            return "";
        }
    }

    public String getUserName(String id) {
        return this.userRepository.findById(id).get().getUsername();
    }
}
