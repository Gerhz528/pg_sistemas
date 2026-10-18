package dsi.plantilla.demo.config;

import dsi.plantilla.demo.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .cacheControl(cache -> cache.disable())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**", "/img/**", "/webjars/**", "/uploads/**").permitAll()
                .requestMatchers("/dashboard").authenticated()
                
                .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/clientes/**").hasAnyRole("ADMINISTRADOR", "JEFE")
                .requestMatchers("/proyectos/**").hasAnyRole("ADMINISTRADOR", "JEFE")
                .requestMatchers("/cotizaciones/**").hasAnyRole("ADMINISTRADOR", "JEFE")
                .requestMatchers("/inventario/**").hasAnyRole("ADMINISTRADOR", "JEFE", "SUPERVISOR")
                
                .requestMatchers("/trabajadores/**").hasRole("SUPERVISOR")
                .requestMatchers("/asistencias/**").hasAnyRole("SUPERVISOR", "CONTADORA", "JEFE")
                .requestMatchers("/facturas/**").hasAnyRole("ADMINISTRADOR", "SUPERVISOR", "CONTADORA", "JEFE")
                
                .requestMatchers("/planillas/**").hasRole("CONTADORA")
                .requestMatchers("/facturacion/**").hasRole("CONTADORA") 
                
                .requestMatchers("/reportes/**").hasAnyRole("ADMINISTRADOR", "JEFE", "CONTADORA")
                // HU-28 y HU-30: Seguridad para Auditoría y Logs del Sistema
                .requestMatchers("/auditoria/**").hasAnyRole("ADMINISTRADOR", "JEFE")
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }
}