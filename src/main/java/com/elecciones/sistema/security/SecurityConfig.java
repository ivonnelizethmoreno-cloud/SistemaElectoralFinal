package com.elecciones.sistema.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final DBUserDetailsService dbUserDetailsService;

    // ==========================
    // 🔑 ENCODER DE CONTRASEÑAS
    // ==========================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==========================
    // 👤 USUARIOS DE MEMORIA (ADMIN + DEMO VOTANTE)
    // ==========================
    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder encoder) {
        var admin = User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        var votante = User.withUsername("1175500265")
                .password(encoder.encode("1175500265")) // 👈 igual usuario y contraseña
                .roles("VOTANTE")
                .build();

        return new InMemoryUserDetailsManager(admin, votante);
    }

    // ==========================
    // ⚙️ AUTHENTICATION MANAGER
    // ==========================
    @Bean
    public AuthenticationManager authenticationManager(
            PasswordEncoder encoder,
            InMemoryUserDetailsManager inMemoryManager
    ) {
        DaoAuthenticationProvider dbProvider = new DaoAuthenticationProvider();
        dbProvider.setUserDetailsService(dbUserDetailsService);
        dbProvider.setPasswordEncoder(encoder);

        DaoAuthenticationProvider memoryProvider = new DaoAuthenticationProvider();
        memoryProvider.setUserDetailsService(inMemoryManager);
        memoryProvider.setPasswordEncoder(encoder);

        return new ProviderManager(List.of(dbProvider, memoryProvider));
    }

    // ==========================
    // 🔒 CONFIGURACIÓN HTTP
    // ==========================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
        http
                .authenticationManager(authManager)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/img/**", "/js/**",
                                "/login", "/error", "/home", "/volver-inicio").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/votante/**", "/votar", "/emitir-voto", "/gracias", "/certificado/**")
                        .hasAnyRole("VOTANTE", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // ✅ Redirección según rol
                        .successHandler((request, response, authentication) -> {
                            var roles = authentication.getAuthorities();
                            if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
                                response.sendRedirect("/admin");
                            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_VOTANTE"))) {
                                response.sendRedirect("/votante"); // 👈 va directo a votar.html
                            } else {
                                response.sendRedirect("/home");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}