package nelon.arrive.nelonshift.security.user;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
		
		return new CustomUserDetails(
			user.getId(),
			user.getEmail(),
			user.getPassword()
		);
	}
}
