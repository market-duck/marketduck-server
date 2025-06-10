package com.devgang.marketduck.config;

import com.devgang.marketduck.auth.filter.JwtAuthorizationFilter;
import com.devgang.marketduck.auth.handler.UserAccessDeniedHandler;
import com.devgang.marketduck.auth.handler.UserAuthenticationEntryPoint;
import com.devgang.marketduck.auth.jwt.JwtTokenizer;
import com.devgang.marketduck.auth.util.CustomAuthorityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecConfig {

        private final JwtTokenizer jwtTokenizer;

        private final CustomAuthorityUtils authorityUtils;

        @Bean
        PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authenticationConfiguration) throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                JwtAuthorizationFilter jwtVerificationFilter = new JwtAuthorizationFilter(jwtTokenizer, authorityUtils);

                http
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .cors(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                                                .requestMatchers(new AntPathRequestMatcher("/open-api/**")).permitAll()
                                                .requestMatchers(new AntPathRequestMatcher("/chat-test/**")).permitAll()
                                                .requestMatchers(new AntPathRequestMatcher("/ws-chat/**")).permitAll()
                                                .requestMatchers(new AntPathRequestMatcher("/api/**"))
                                                .hasAnyRole("USER")
                                                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
                                .csrf(AbstractHttpConfigurer::disable)
                                .headers((headers) -> headers
                                                .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                                .formLogin(AbstractHttpConfigurer::disable)
                                .logout(AbstractHttpConfigurer::disable)
                                .addFilterBefore(jwtVerificationFilter, UsernamePasswordAuthenticationFilter.class)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(new UserAuthenticationEntryPoint())
                                                .accessDeniedHandler(new UserAccessDeniedHandler()));

                return http.build();
        }

}