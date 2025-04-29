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
        serializer.setDomainName("tailfriends.kro.kr"); // . 제거 주의!
        serializer.setUseSecureCookie(true); // HTTPS면 true
        serializer.setSameSite("Lax");
        return serializer;
    }

}
