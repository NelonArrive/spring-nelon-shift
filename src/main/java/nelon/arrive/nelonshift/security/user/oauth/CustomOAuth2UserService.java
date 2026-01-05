package nelon.arrive.nelonshift.security.user.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		OAuth2User oauth2User = super.loadUser(userRequest);
		
		String provider = userRequest.getClientRegistration().getRegistrationId();
		String email = oauth2User.getAttribute("email");
		String name = oauth2User.getAttribute("name");
		
		if (email == null) {
			throw new OAuth2AuthenticationException("Email не найден из OAuth2 provider");
		}
		
		log.info("OAuth2 login: provider={}, email={}", provider, email);
		
		User user = userRepository.findByEmail(email)
			.orElseGet(() -> registerNewOAuth2User(email, name, provider));
		
		return new CustomOAuth2User(oauth2User, user);
	}
	
	// Регистрация нового пользователя через OAuth2
	private User registerNewOAuth2User(String email, String name, String provider) {
		log.info("Register new OAuth2 user: email={}, provider={}", email, provider);
		
		User user = User.builder()
			.email(email)
			.name(name != null ? name : email.split("@")[0])
			.password(passwordEncoder.encode(UUID.randomUUID().toString()))
			.build();
		
		return userRepository.save(user);
	}
}
