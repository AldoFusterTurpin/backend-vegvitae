package com.vegvitae.vegvitae;

import com.vegvitae.vegvitae.model.User;
import com.vegvitae.vegvitae.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class LoadDatabase {

	@Bean
	CommandLineRunner initDatabase(UserRepository repository) {
		return args -> {
			log.info("Preloading " + repository.save(new User("Uncle Bob Martin", "123abc", "I am vegan :)", null)));
			log.info("Preloading " + repository.save(new User("Martin Fowler", "my_awesome_password", "I love Guacamole from Mercadona", null)));
		};
	}
}
