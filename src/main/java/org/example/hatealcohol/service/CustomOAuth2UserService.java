package org.example.hatealcohol.service;

import org.example.hatealcohol.dto.CustomOAuth2User;
import org.example.hatealcohol.dto.GoogleResponse;
import org.example.hatealcohol.dto.KakaoResponse;
import org.example.hatealcohol.dto.NaverResponse;
import org.example.hatealcohol.dto.OAuth2Response;
import org.example.hatealcohol.dto.UserDTO;
import org.example.hatealcohol.entity.User;
import org.example.hatealcohol.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        User foundUser = userRepository.findByUsername(username);
        if (foundUser == null) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(oAuth2Response.getEmail());
            user.setName(oAuth2Response.getName());
            user.setRole("ROLE_USER");
            userRepository.save(user);
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole("ROLE_USER");
            return new CustomOAuth2User(userDTO);
        } else {

            foundUser.setEmail(oAuth2Response.getEmail());
            foundUser.setName(oAuth2Response.getName());
            userRepository.save(foundUser);
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(foundUser.getUsername());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(foundUser.getRole());

            return new CustomOAuth2User(userDTO);
        }
    }
}
