package tf.tailfriend.global.config;//package tf.tailfriend.global.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
//@Configuration
//public class Config {
//
//    @Bean
//    public CorsFilter corsFilter() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//
//        // 허용할 출처 설정 (리액트 개발 서버 주소)
//        config.addAllowedOrigin("http://localhost:5173");
//
//        // 필요한 경우 실제 배포 환경 URL도 추가
//        // config.addAllowedOrigin("https://your-production-domain.com");
//
//        // 허용할 HTTP 메서드 설정
//        config.addAllowedMethod("*");
//
//        // 허용할 헤더 설정
//        config.addAllowedHeader("*");
//
//        // 인증 정보 허용 (쿠키, 인증 헤더 등)
//        config.setAllowCredentials(true);
//
//        // 캐시 시간 설정 (초 단위)
//        config.setMaxAge(3600L);
//
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }
//}
