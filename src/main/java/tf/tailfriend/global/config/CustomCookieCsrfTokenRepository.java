package tf.tailfriend.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class CustomCookieCsrfTokenRepository implements CsrfTokenRepository {

    private final String headerName = "X-XSRF-TOKEN";
    private final String parameterName = "_csrf";
    private final String cookieName = "XSRF-TOKEN";

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        String token = createNewTokenValue();
        return new DefaultCsrfToken(headerName, parameterName, token);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            response.addHeader("Set-Cookie", cookieName + "=; Max-Age=0; Path=/; SameSite=None; Secure");
            return;
        }

        response.addHeader("Set-Cookie", String.format(
                "%s=%s; Path=/; SameSite=None; Secure",
                cookieName, token.getToken()
        ));
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        String token = null;

        // 쿠키에서 직접 가져옴
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if (cookieName.equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (StringUtils.hasText(token)) {
            return new DefaultCsrfToken(headerName, parameterName, token);
        }

        return null;
    }

    private String createNewTokenValue() {
        return UUID.randomUUID().toString();
    }
}
