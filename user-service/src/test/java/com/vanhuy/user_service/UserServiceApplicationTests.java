package com.vanhuy.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "restaurant.service.url=http://localhost:8082",
    "order.service.url=http://localhost:8083",
    "spring.security.user.name=test",
    "spring.security.user.password=test"
})
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
		// This test just verifies that the Spring application context loads successfully
	}

}
