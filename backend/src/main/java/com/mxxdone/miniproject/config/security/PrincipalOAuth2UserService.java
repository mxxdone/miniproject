package com.mxxdone.miniproject.config.security;

import com.mxxdone.miniproject.config.oauth2.GoogleUserInfo;
import com.mxxdone.miniproject.config.oauth2.OAuth2UserInfo;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrincipalOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Google로부터 받은 사용자 정보 객체
        OAuth2UserInfo oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());

        String provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String username = provider + "_" + providerId;
        String nickname = oAuth2UserInfo.getName(); // 화면 표시용

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) //최초 소셜 로그인 시 임의의 비밀번호 생성
                    .nickname(nickname)
                    .email(email)
                    .role(Role.USER)
                    .provider(provider)
                    .build();
            userRepository.save(user);
        }
        // 일반 회원(user), OAuth2 로그인을 통해 받아온 프로필 정보
        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }
}
