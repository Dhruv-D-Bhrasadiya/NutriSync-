package com.nutrisync.config;

import com.nutrisync.domain.*;
import com.nutrisync.repository.AvailabilitySlotRepository;
import com.nutrisync.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.*;

@Component
@Profile("demo")
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataInitializer {
    private final UserService users;
    private final AvailabilitySlotRepository slots;
    private final ZoneId zone;

    public DemoDataInitializer(UserService users, AvailabilitySlotRepository slots,
                               org.springframework.core.env.Environment environment) {
        this.users = users;
        this.slots = slots;
        this.zone = ZoneId.of(environment.getProperty("app.time-zone", "Asia/Kolkata"));
    }

    @PostConstruct
    public void seedDemoData() {
        AppUser nutritionist = users.createUserIfMissing("Demo Nutritionist", "nutritionist@nutrisync.local", "NutriSyncDemo!2026", Role.NUTRITIONIST);
        users.createUserIfMissing("Demo Client", "client@nutrisync.local", "NutriSyncDemo!2026", Role.CLIENT);
        users.createUserIfMissing("Demo Administrator", "admin@nutrisync.local", "NutriSyncAdmin!2026", Role.ADMIN);
        if (slots.count() > 0) return;
        LocalDate firstDay = LocalDate.now(zone).plusDays(2);
        for (int day = 0; day < 30; day++) {
            LocalDate date = firstDay.plusDays(day);
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            for (int hour = 10; hour <= 16; hour += 2) {
                Instant start = ZonedDateTime.of(date, LocalTime.of(hour, 0), zone).toInstant();
                Instant end = start.plus(Duration.ofMinutes(45));
                slots.save(new AvailabilitySlot(nutritionist, start, end));
            }
        }
    }
}
