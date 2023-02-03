package com.UoU.app.security;

import com.UoU.core.Fluent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configures API security, including authentication, JWT handling, and CORS.
 */
@ConditionalOnWebApplication
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // enable @PreAuthorize for classes & methods
class SecurityConfig {

  @Bean
  @SuppressWarnings("unchecked") // for removeConfigurer()
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      HttpAccessExpressions httpAccessExpressions,
      AuthenticationEntryPoint authenticationEntryPoint) throws Exception {

    // Configure which resources require authentication, which is everything but a few exceptions.
    // For authorization (in addition to authentication), use @Authorize.XYZ on classes/methods.
    // See https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/el-access.html
    http
        .authorizeRequests()
        .mvcMatchers(HttpMethod.GET, "/favicon.ico").permitAll()
        .mvcMatchers(HttpMethod.GET, "/docs/**").permitAll()
        .mvcMatchers(HttpMethod.GET, "/css/**").permitAll()
        .mvcMatchers(
            HttpMethod.GET,
            "/actuator/health",
            "/actuator/info",
            "/actuator/health/liveness",
            "/actuator/health/readiness").access(httpAccessExpressions.health())
        .mvcMatchers(HttpMethod.GET, "/actuator/**").access(httpAccessExpressions.actuator())
        .mvcMatchers(HttpMethod.GET, "/oauth/**").permitAll()
        .mvcMatchers(HttpMethod.GET, "/v1/oauth/**").permitAll()
        .mvcMatchers("/v1/auth/connect/**").permitAll()
        .mvcMatchers("/v1/inbound-webhooks/**").permitAll() // DO-MAYBE: use hasIpAddress()?
        .mvcMatchers("/v1/config/**").permitAll() // public config values
        .anyRequest().authenticated();

    // Configure cors.
    http.cors(corsConfigurer -> corsConfigurer.configurationSource(Fluent
        .of(new UrlBasedCorsConfigurationSource())
        .also(source -> source.registerCorsConfiguration("/**", Fluent
            .of(new CorsConfiguration())
                .also(config -> config.addAllowedOrigin("*"))
                .also(config -> config.addAllowedHeader("*"))
                .also(config -> config.addAllowedMethod("*"))
            .get()))
        .get()));

    // Enable JWT auth for our resources.
    // Our JwtDecoder and JwtValidator beans will be used automatically based on @Service scanning.
    http.oauth2ResourceServer(config -> config
        .jwt().and()
        .authenticationEntryPoint(authenticationEntryPoint));

    // Disable a bunch of stuff we don't need.
    http.csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .formLogin().disable()
        .httpBasic().disable()
        .logout().disable();

    // Disable Dao configurer because it tries to get a UserDetailService and do unneeded stuff.
    // This configurer wasn't present in the old WebSecurityConfigurerAdapter, but when ported to
    // the new SecurityFilterChain in new spring-security versions, it gets added by default.
    http.getSharedObject(AuthenticationManagerBuilder.class)
        .removeConfigurer(DaoAuthenticationConfigurer.class);

    return http.build();
  }
}
