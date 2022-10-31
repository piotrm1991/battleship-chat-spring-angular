package it.piotrmachnik.authservice;

import it.piotrmachnik.authservice.models.ERole;
import it.piotrmachnik.authservice.models.Role;
import it.piotrmachnik.authservice.models.User;
import it.piotrmachnik.authservice.repository.RoleRepository;
import it.piotrmachnik.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class AuthServiceApplication implements CommandLineRunner {

	@Autowired
	private RoleRepository roleRepository;

	//for testing
	@Autowired
	private UserRepository userRepository;
	@Autowired
	PasswordEncoder encoder;

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (!this.roleRepository.findByName(ERole.ROLE_USER).isPresent()) {
			Role role = new Role(ERole.ROLE_USER);
			this.roleRepository.save(role);
		}

		//for testing
		User user = new User("test", encoder.encode("test123"));
		Set<Role> roles = new HashSet<>();
		Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		roles.add(userRole);
		user.setRoles(roles);
		userRepository.save(user);

		user = new User("piotr", encoder.encode("test123"));
		roles = new HashSet<>();
		userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		roles.add(userRole);
		user.setRoles(roles);
		userRepository.save(user);
	}
}
