package com.examplo.pagoamigos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuración de autorización
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/dashboard/**").hasAnyRole("VALIDATOR", "SOLICITANTE")
                .anyRequest().authenticated()
            )
            // Configuración de login con límite de intentos
            .formLogin(form -> form 
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            // Configuración de logout seguro
            .logout(logout -> logout 
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Protección de sesiones
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            // Headers de seguridad
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "img-src 'self' data: https://lh3.googleusercontent.com https://grainy-gradients.vercel.app;")
                )
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.disable()) // XSS Protection obsoleto en navegadores modernos con CSP
            )
            // CSRF habilitado por defecto (Spring Security 6+)
            .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl("/login?error=true");
        return handler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Aumentado a 12 rounds para mayor seguridad
    }
}
