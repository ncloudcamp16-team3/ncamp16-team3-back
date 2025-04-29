package tf.tailfriend.global.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Integer userId;
    private String snsAccountId;
    private Integer snsTypeId;
    private Boolean isNewUser;

    // 권한이 필요한 경우 확장 가능
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_USER 권한을 부여 (기본적으로 모든 사용자에게 ROLE_USER)
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // 비밀번호 사용 안하므로 빈 문자열
    @Override
    public String getPassword() {
        return "";
    }

    // 이메일을 username으로 사용
    @Override
    public String getUsername() {
        return snsAccountId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
