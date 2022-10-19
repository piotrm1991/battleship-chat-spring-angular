package it.piotrmachnik.gameRoomService.repository;

import it.piotrmachnik.gameRoomService.model.user.ERole;
import it.piotrmachnik.gameRoomService.model.user.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
  Optional<Role> findByName(ERole name);
}
