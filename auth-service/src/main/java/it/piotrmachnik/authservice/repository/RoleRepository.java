package it.piotrmachnik.authservice.repository;

import it.piotrmachnik.authservice.models.ERole;
import it.piotrmachnik.authservice.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
  Optional<Role> findByName(ERole name);
}
