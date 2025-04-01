package com.mp.karental.configuration;

import com.mp.karental.constant.ERole;
import com.mp.karental.entity.Role;
import com.mp.karental.logging.LoggingConfig;
import com.mp.karental.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class contains init configuration for the application and would be run fist.
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Configuration
@RequiredArgsConstructor()
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    LoggingConfig loggingConfig;
    RoleRepository roleRepository;

    /**
     * Initializes some default data for the application.
     * <p>
     * This method checks if the default roles, such as CAR_OWNER and CUSTOMER,
     * already exist in the database. If they do not exist, they will be created and saved to the database.
     * </p>
     **
     * @return An ApplicationRunner that executes when the application starts.
     *
     * @author DieuTTH4
     *
     * @version 1.1
     */
    @Bean
    ApplicationRunner init() {
        return args -> {
            //set up logging for the application
            loggingConfig.setupLogging();

            //create 2 roles Customer and car owner
            if (roleRepository.findByName(ERole.CAR_OWNER).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(ERole.CAR_OWNER)
                        .build());
                log.info("Create role Car Owner");
            }
            if (roleRepository.findByName(ERole.CUSTOMER).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(ERole.CUSTOMER)
                        .build());
                log.info("Create role Customer");
            }
        };
    }
}
