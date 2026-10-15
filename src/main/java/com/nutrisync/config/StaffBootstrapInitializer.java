package com.nutrisync.config;

import com.nutrisync.domain.AppUser;
import com.nutrisync.domain.Role;
import com.nutrisync.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Optional, one-time bootstrap for a non-demo environment where staff accounts
 * cannot be created through public registration. Keep disabled unless all values
 * are supplied through a secret manager or protected environment variables.
 */
@Component
@ConditionalOnProperty(name = "app.bootstrap-staff.enabled", havingValue = "true")
public class StaffBootstrapInitializer implements ApplicationRunner {
    private final UserService users;
    private final String name;
    private final String email;
    private final String password;

    public StaffBootstrapInitializer(
            UserService users,
            @Value("${app.bootstrap-staff.name:}") String name,
            @Value("${app.bootstrap-staff.email:}") String email,
            @Value("${app.bootstrap-staff.password:}") String password) {
        this.users = users;
        this.name = name == null ? "" : name.trim();
        this.email = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalStateException("Bootstrap staff name must contain 2–100 characters.");
        }
        if (!email.matches("(?i)^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$")) {
            throw new IllegalStateException("A valid APP_BOOTSTRAP_STAFF_EMAIL is required when staff bootstrap is enabled.");
        }
        if (password.length() < 20 || password.length() > 72) {
            throw new IllegalStateException("Bootstrap staff password must contain 20–72 characters.");
        }

        AppUser account = users.createUserIfMissing(name, email, password, Role.NUTRITIONIST);
        if (account.getRole() != Role.NUTRITIONIST && account.getRole() != Role.ADMIN) {
            throw new IllegalStateException("The bootstrap email already belongs to a non-staff account; choose a dedicated staff email.");
        }
    }
}
