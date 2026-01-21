package com.examplo.pagoamigos.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import java.io.IOException;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    @Value("${app.form-action-origin:https://pagoamigos.pimentel.cloud}")
    private String formActionOrigin;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuración de autorización
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/error/**", "/css/**", "/js/**", "/images/**", "/logout", "/favicon.ico").permitAll()
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
                .logoutSuccessHandler(customLogoutSuccessHandler())
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )
            // Protección de sesiones
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            // CSRF con cookies para evitar crear sesión en formularios
            .csrf(csrf -> {
                CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
                CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                requestHandler.setCsrfRequestAttributeName("_csrf");
                
                csrf.csrfTokenRepository(tokenRepository)
                    .csrfTokenRequestHandler(requestHandler);
            })
            // Headers de seguridad
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "img-src 'self' data: https://lh3.googleusercontent.com https://grainy-gradients.vercel.app; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        // Allow the application's deployed origin for form submissions (from properties)
                        "form-action 'self' " + formActionOrigin + ";")
                )
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.disable()) // XSS Protection obsoleto en navegadores modernos con CSP
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000) // 1 año
                )
                .referrerPolicy(referrer -> referrer
                    .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=()")
                )
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
    public LogoutSuccessHandler customLogoutSuccessHandler() {
        return new LogoutSuccessHandler() {
            @Override
            public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException {
                // Generar nuevo token CSRF después del logout
                CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
                CsrfToken newToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", UUID.randomUUID().toString());
                tokenRepository.saveToken(newToken, request, response);
                
                // Redirigir al login
                response.sendRedirect("/login?logout");
            }
        };
    }

    @Bean    
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Aumentado a 12 rounds para mayor seguridad
    }
}
