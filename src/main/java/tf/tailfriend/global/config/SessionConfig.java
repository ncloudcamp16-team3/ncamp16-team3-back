package tf.tailfriend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSION");
        serializer.setDomainName("tailfriends.kro.kr"); // 반드시 일치하는 도메인
        serializer.setCookiePath("/");
        serializer.setUseSecureCookie(true); // HTTPS 사용 중이므로 true 권장
        serializer.setSameSite("Lax"); // SameSite 설정 (CSRF 보호)
        return serializer;
    }
}
