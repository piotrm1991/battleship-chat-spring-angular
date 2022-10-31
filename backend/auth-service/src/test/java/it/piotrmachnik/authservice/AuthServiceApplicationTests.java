package it.piotrmachnik.authservice;

import it.piotrmachnik.authservice.controllers.AuthController;
import it.piotrmachnik.authservice.controllers.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class AuthServiceApplicationTests {

	@Autowired
	AuthController authController;

	@Autowired
	TestController testController;

	@Test
	void contextLoads() {
		assertThat(authController).isNotNull();
		assertThat(testController).isNotNull();
	}
}
