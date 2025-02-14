package com.mp.karental.configuration;

import com.mp.karental.constant.ERole;
import com.mp.karental.entity.Role;
import com.mp.karental.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor()
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    RoleRepository roleRepository;

    @Bean
    ApplicationRunner init(RoleRepository roleRepository) {
        return args -> {
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
