package com.nutrisync;

import com.nutrisync.domain.AppUser;
import com.nutrisync.repository.AppUserRepository;
import com.nutrisync.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {
    @Autowired UserService userService;
    @Autowired AppUserRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void registrationPersistsNormalizedEmailAndHashedPassword() {
        String rawPassword = "CorrectHorseBattery1";
        AppUser user = userService.registerClient("Test Person", "New.User@example.test", rawPassword);
        assertThat(user.getEmail()).isEqualTo("new.user@example.test");
        assertThat(user.getPassword()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, user.getPassword())).isTrue();
    }

    @Test
    void duplicateEmailIsRejected() {
        userService.registerClient("Test Person", "repeat@example.test", "CorrectHorseBattery1");
        ResponseStatusException exception = catchThrowableOfType(
            () -> userService.registerClient("Second Person", "REPEAT@example.test", "CorrectHorseBattery2"), ResponseStatusException.class);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
