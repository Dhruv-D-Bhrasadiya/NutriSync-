package com.nutrisync.config;

import com.nutrisync.repository.AppUserRepository;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository users) {
        return username -> users.findByEmailIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password."));
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/v1/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/slots").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/slots").hasAnyRole("NUTRITIONIST", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/bookings").hasRole("CLIENT")
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/slots", "/register", "/login", "/css/**", "/actuator/health", "/error").permitAll()
                .requestMatchers("/staff/**").hasAnyRole("NUTRITIONIST", "ADMIN")
                .requestMatchers("/bookings/**").authenticated()
                .anyRequest().permitAll())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/slots", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/slots?logout").permitAll());
        return http.build();
    }
}
