package nelon.arrive.nelonshift.services;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.exception.AlreadyExistsException;
import nelon.arrive.nelonshift.exception.ResourceNotFoundException;
import nelon.arrive.nelonshift.repository.UserRepository;
import nelon.arrive.nelonshift.request.CreateUserRequest;
import nelon.arrive.nelonshift.request.UpdateUserRequest;
import nelon.arrive.nelonshift.services.interfaces.IUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public User getUserById(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User not found!"));
	}
	
	@Override
	public User createUser(CreateUserRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new AlreadyExistsException("Email already exists");
		}
		
		User user = new User();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setName(request.getName());
		
		return userRepository.save(user);
	}
	
	@Override
	public User updateUser(UpdateUserRequest request, UUID userId) {
		return userRepository.findById(userId).map(exisingUser -> {
			exisingUser.setName(request.getName());
			return userRepository.save(exisingUser);
		}).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
	}
	
	@Override
	public void deleteUser(UUID userId) {
		userRepository.findById(userId).ifPresentOrElse(userRepository::delete, () -> {
			throw new ResourceNotFoundException("User not found!");
		});
	}
	
	@Override
	public User getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null ||
			!authentication.isAuthenticated() ||
			authentication.getPrincipal().equals("anonymousUser")) {
			throw new JwtException("User is not authenticated");
		}
		
		String email = authentication.getName();
		
		return userRepository.findByEmail(email)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
