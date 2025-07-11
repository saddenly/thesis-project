package com.rustem.eduthesis.infrastructure.service;

import com.rustem.eduthesis.infrastructure.entity.RoleEntity;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.RoleNotFoundException;
import com.rustem.eduthesis.infrastructure.repository.RoleRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import com.rustem.eduthesis.infrastructure.security.oauth2.OAuth2UserInfo;
import com.rustem.eduthesis.infrastructure.security.oauth2.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String providerName = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerName, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty())
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");


        UserEntity userEntity = userRepo.findByEmail(userInfo.getEmail()).orElse(null);

        if (userEntity != null)
            updateExistingUser(userEntity, userInfo, providerName);
        else
            userEntity = registerNewUser(userInfo, providerName);

        userRepo.save(userEntity);

        // Return the OAuth2User with the right attributes and authorities
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        userEntity.getRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()))
        );

        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                userInfo.getAttributeKey()
        );
    }

    private void updateExistingUser(
            UserEntity userEntity,
            OAuth2UserInfo userInfo,
            String providerName) {

        userEntity.setProvider(providerName);
        userEntity.setProviderId(userInfo.getId());
        userEntity.setFirstName(userInfo.getFirstName());
        userEntity.setLastName(userInfo.getLastName());
        userEntity.setUpdatedAt(LocalDateTime.now());
    }

    private UserEntity registerNewUser(OAuth2UserInfo userInfo, String providerName) {
        UserEntity userEntity = new UserEntity();

        userEntity.setProvider(providerName);
        userEntity.setProviderId(userInfo.getId());
        userEntity.setEmail(userInfo.getEmail());
        userEntity.setFirstName(userInfo.getFirstName());
        userEntity.setLastName(userInfo.getLastName());
        userEntity.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Generate random encoded password
        userEntity.setEnabled(true);
        userEntity.setLocked(false);
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());

        // Assign a default STUDENT role to new users
        RoleEntity roleEntity = roleRepo.findByName("STUDENT")
                .orElseThrow(() -> new RoleNotFoundException("Default STUDENT role not found"));
        userEntity.setRoles(Set.of(roleEntity));

        return userEntity;
    }
}
