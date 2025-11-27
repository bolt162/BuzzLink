package com.buzzlink;

import com.buzzlink.entity.Channel;
import com.buzzlink.entity.User;
import com.buzzlink.repository.ChannelRepository;
import com.buzzlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main Spring Boot application class for BuzzLink
 */
@SpringBootApplication
@RequiredArgsConstructor
public class BuzzLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuzzLinkApplication.class, args);
    }

    /**
     * Initialize sample data on startup (for demo purposes)
     * Creates default channels and sample users
     */
    @Bean
    public CommandLineRunner initData(ChannelRepository channelRepository, UserRepository userRepository) {
        return args -> {
            // Create default channels if they don't exist
            if (channelRepository.findByName("general").isEmpty()) {
                Channel general = new Channel();
                general.setName("general");
                general.setDescription("General discussion");
                channelRepository.save(general);
            }

            if (channelRepository.findByName("random").isEmpty()) {
                Channel random = new Channel();
                random.setName("random");
                random.setDescription("Random conversations");
                channelRepository.save(random);
            }

            if (channelRepository.findByName("engineering").isEmpty()) {
                Channel engineering = new Channel();
                engineering.setName("engineering");
                engineering.setDescription("Engineering team channel");
                channelRepository.save(engineering);
            }

            System.out.println("✓ BuzzLink backend started successfully!");
            System.out.println("✓ Default channels initialized: general, random, engineering");
            System.out.println("✓ H2 Console (dev mode): http://localhost:8080/h2-console");
            System.out.println("✓ Prometheus metrics: http://localhost:8080/actuator/prometheus");
        };
    }
}
