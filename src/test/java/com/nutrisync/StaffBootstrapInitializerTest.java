package com.nutrisync;

import com.nutrisync.config.StaffBootstrapInitializer;
import com.nutrisync.domain.AppUser;
import com.nutrisync.domain.Role;
import com.nutrisync.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class StaffBootstrapInitializerTest {
    @Test
    void createsNutritionistAccountWithValidatedCredentials() throws Exception {
        UserService users = mock(UserService.class);
        AppUser nutritionist = new AppUser("Demo Nutritionist", "nutritionist@example.test", "hashed", Role.NUTRITIONIST);
        when(users.createUserIfMissing(
            "Demo Nutritionist",
            "nutritionist@example.test",
            "A-Unique-Strong-Password-2026",
            Role.NUTRITIONIST
        )).thenReturn(nutritionist);

        StaffBootstrapInitializer initializer = new StaffBootstrapInitializer(
            users,
            " Demo Nutritionist ",
            "Nutritionist@Example.Test",
            "A-Unique-Strong-Password-2026"
        );
        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(users).createUserIfMissing(
            "Demo Nutritionist",
            "nutritionist@example.test",
            "A-Unique-Strong-Password-2026",
            Role.NUTRITIONIST
        );
    }

    @Test
    void rejectsWeakBootstrapPasswordBeforeCreatingAnyAccount() {
        UserService users = mock(UserService.class);
        StaffBootstrapInitializer initializer = new StaffBootstrapInitializer(
            users, "Nutritionist", "nutritionist@example.test", "too-short"
        );

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments(new String[0])))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("20–72 characters");
        verifyNoInteractions(users);
    }

    @Test
    void rejectsEmailWithoutDomain() {
        UserService users = mock(UserService.class);
        StaffBootstrapInitializer initializer = new StaffBootstrapInitializer(
            users, "Nutritionist", "not-an-email", "A-Unique-Strong-Password-2026"
        );

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments(new String[0])))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("valid APP_BOOTSTRAP_STAFF_EMAIL");
        verifyNoInteractions(users);
    }
}
