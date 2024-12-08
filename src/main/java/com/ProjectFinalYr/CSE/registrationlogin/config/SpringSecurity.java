package com.ProjectFinalYr.CSE.registrationlogin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SpringSecurity {

    @Autowired
    private UserDetailsService userDetailsService;

    // Bean for password encoding
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configure HTTP security
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable() // Disable CSRF temporarily for testing (to be re-enabled in production)
                .authorizeRequests((authorize) -> authorize
                        .requestMatchers("/", "/index.html/**").permitAll() // Allow index page access
                        .requestMatchers("/register/**").permitAll() // Allow registration page access
                        .requestMatchers("/login/**").permitAll() // Allow login page access
                        .requestMatchers("/op").permitAll() // Allow output page access (Python result page)
                        .requestMatchers("/run-python").permitAll() // Allow Python script execution
                        .requestMatchers("/users/**").hasRole("ADMIN") // Protect user-related pages (admin only)
                        .anyRequest().authenticated() // Require authentication for all other requests
                )
                .formLogin(form -> form
                        .loginPage("/login") // Custom login page
                        .loginProcessingUrl("/login") // Processing URL for login form
                        .defaultSuccessUrl("/users", true) // Redirect to /users after successful login
                        .permitAll() // Allow all users to access login page
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // Custom logout URL
                        .logoutSuccessUrl("/") // Redirect to the homepage after logout
                        .permitAll() // Allow all users to access logout page
                );
        return http.build();
    }

    // Configure authentication manager
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder()); // Use the custom password encoder
    }
}
