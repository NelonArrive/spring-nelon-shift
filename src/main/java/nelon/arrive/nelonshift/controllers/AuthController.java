package nelon.arrive.nelonshift.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.auth.LoginRequest;
import nelon.arrive.nelonshift.repositories.RoleRepository;
import nelon.arrive.nelonshift.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	// private final JwtUtils jwtUtils;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(
		@Valid @RequestBody LoginRequest loginRequest
	) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
				loginRequest.getEmail(),
				loginRequest.getPassword()
			)
		);
		
		
	}
}
