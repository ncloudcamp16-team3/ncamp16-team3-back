package tf.tailfriend.global.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
    private final OAuth2SuccessHandler successHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            JwtAuthenticationEntryPoint jwtAuthEntryPoint,
            OAuth2SuccessHandler successHandler
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.successHandler = successHandler;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable()) // 폼 로그인 제거
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 제거

                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/static/**").permitAll()  // 정적 자원 경로는 인증 없이 접근 가능
                        // OAuth2 관련
                        .requestMatchers("/api/oauth2/authorization/**").permitAll()
                                .requestMatchers("/static/**").permitAll()
                                .requestMatchers("/api/login/oauth2/code/**").permitAll()

                        // 관리자 API - 로그인, 인증 체크, 로그아웃은 누구나 접근 가능
                        .requestMatchers("/api/admin/login", "/api/admin/auth/validate", "/api/admin/logout").permitAll()
                        .requestMatchers("/api/admin/register").permitAll()

                        // 나머지 관리자 API는 ADMIN 권한 필요
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 일반 API
//                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/api/**").hasRole("USER")

                        // 정적 페이지
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/admin").permitAll()
                        // 나머지 API 권한 설정


                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/api/oauth2/authorization") // ✅ 여기서 경로 커스터마이징
                                .authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository())
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/api/login/oauth2/code/**") // ✅ 여기를 꼭 추가해야 custom redirect-uri 작동함!
                        )
                        .successHandler(successHandler) // OAuth2 로그인 성공 후 핸들러 설정
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace(); // 에러 로그 출력
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"" + exception.getMessage() + "\"}");
                        })
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint)); // 인증 실패시 처리

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("http://tailfriends.kro.kr");
        configuration.addAllowedOrigin("https://tailfriends.kro.kr");
        configuration.addAllowedOrigin("https://tailfriends.kro.kr:8080");
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
