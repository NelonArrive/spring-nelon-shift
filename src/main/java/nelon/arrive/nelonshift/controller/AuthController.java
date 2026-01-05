package nelon.arrive.nelonshift.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.request.LoginRequest;
import nelon.arrive.nelonshift.request.SignupRequest;
import nelon.arrive.nelonshift.request.TokenRefreshRequest;
import nelon.arrive.nelonshift.response.JwtResponse;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.response.TokenRefreshResponse;
import nelon.arrive.nelonshift.services.interfaces.IAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
	
	private final IAuthService authService;
	
	@PostMapping("/login")
	public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
		JwtResponse jwtResponse = authService.login(loginRequest);
		return ResponseEntity.ok(jwtResponse);
	}
	
	@PostMapping("/signup")
	public ResponseEntity<MessageResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
		return ResponseEntity.ok(authService.signup(signupRequest));
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
		return ResponseEntity.ok(authService.refreshToken(request));
	}
	
	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout(@Valid @RequestBody TokenRefreshRequest request) {
		return ResponseEntity.ok(authService.logout(request));
	}
	
	@GetMapping("/me")
	public ResponseEntity<JwtResponse> currentUser() {
		User user = authService.getCurrentUser();
		
		JwtResponse jwtResponse = new JwtResponse(
			null,
			null,
			null,
			user.getId(),
			user.getEmail(),
			user.getName()
		);
		
		return ResponseEntity.ok(jwtResponse);
	}
}
