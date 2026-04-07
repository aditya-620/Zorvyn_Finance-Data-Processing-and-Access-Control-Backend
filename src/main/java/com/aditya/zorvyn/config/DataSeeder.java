package com.aditya.zorvyn.config;

import com.aditya.zorvyn.model.Role;
import com.aditya.zorvyn.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds initial data on application startup.
 * Creates a default admin user if no users exist in the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) {
        try {
            if (userService.getAllUsers().isEmpty()) {
                userService.createUser("admin", "admin@zorvyn.com", "admin123", Role.ADMIN);
                log.info("Default admin user created — username: admin, password: admin123");

                userService.createUser("analyst", "analyst@zorvyn.com", "analyst123", Role.ANALYST);
                log.info("Default analyst user created — username: analyst, password: analyst123");

                userService.createUser("viewer", "viewer@zorvyn.com", "viewer123", Role.VIEWER);
                log.info("Default viewer user created — username: viewer, password: viewer123");
            } else {
                log.info("Users already exist, skipping data seeding");
            }
        } catch (Exception e) {
            log.warn("Data seeding skipped or failed: {}", e.getMessage());
        }
    }
}
