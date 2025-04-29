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
        serializer.setDomainName("tailfriends.kro.kr"); // 또는 ".tailfriends.kro.kr" → 둘 다 실험해보기
        serializer.setCookiePath("/");
        serializer.setSameSite("Lax"); // 또는 "None" (프론트 상황에 따라)
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(true); // HTTPS만 쓸 거면 true
        return serializer;
    }
}