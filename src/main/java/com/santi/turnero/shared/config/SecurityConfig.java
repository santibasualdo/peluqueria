package com.santi.turnero.shared.config;

import com.santi.turnero.peluquero.domain.Peluquero;
import com.santi.turnero.peluquero.repository.PeluqueroRepository;
import com.santi.turnero.shared.security.AuthenticatedPeluqueroUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    private static final String FIRST_ACCESS_PATH = "/primer-acceso";
    private static final String FIRST_ACCESS_API_PATH = "/api/v1/peluqueros/me/password";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/login/**", FIRST_ACCESS_PATH, FIRST_ACCESS_PATH + "/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/webhooks/whatsapp").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/webhooks/whatsapp").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureUrl("/login?error")
                        .successHandler((request, response, authentication) -> {
                            if (requiresPasswordChange(authentication)) {
                                response.sendRedirect(FIRST_ACCESS_PATH);
                                return;
                            }

                            response.sendRedirect("/turnos");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/webhooks/whatsapp", "/login")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                );

        http.addFilterAfter(csrfCookieFilter(), CsrfFilter.class);
        http.addFilterAfter(forcePasswordChangeFilter(), CsrfFilter.class);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PeluqueroRepository peluqueroRepository
    ) {
        return username -> {
            String normalizedUsername = normalize(username);
            if (normalizedUsername == null) {
                throw new UsernameNotFoundException("Usuario invalido.");
            }

            Peluquero peluquero = peluqueroRepository.findByUsuarioIgnoreCaseAndActivoTrue(normalizedUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("No existe un peluquero activo para ese usuario."));

            return buildUserDetails(peluquero);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OncePerRequestFilter csrfCookieFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {
                CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                if (csrfToken != null) {
                    csrfToken.getToken();
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public OncePerRequestFilter forcePasswordChangeFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {
                Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

                if (requiresPasswordChange(authentication) && !isAllowedDuringFirstAccess(request.getRequestURI())) {
                    response.sendRedirect(FIRST_ACCESS_PATH);
                    return;
                }

                filterChain.doFilter(request, response);
            }
        };
    }

    private UserDetails buildUserDetails(Peluquero peluquero) {
        return new AuthenticatedPeluqueroUser(
                peluquero.getId(),
                peluquero.getUsuario(),
                peluquero.getPasswordHash(),
                peluquero.isActivo(),
                peluquero.isRequiereCambioPassword(),
                AuthorityUtils.createAuthorityList("ROLE_PELUQUERO")
        );
    }

    private boolean requiresPasswordChange(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        return principal instanceof AuthenticatedPeluqueroUser user && user.requiereCambioPassword();
    }

    private boolean isAllowedDuringFirstAccess(String requestUri) {
        return requestUri == null
                || requestUri.startsWith(FIRST_ACCESS_PATH)
                || requestUri.equals(FIRST_ACCESS_API_PATH)
                || requestUri.startsWith("/logout")
                || requestUri.startsWith("/error")
                || requestUri.startsWith("/api/v1/webhooks/whatsapp");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return null;
        }

        String digitsOnly = trimmed.replaceAll("\\D", "");
        return digitsOnly.isBlank() ? trimmed : digitsOnly;
    }
}
