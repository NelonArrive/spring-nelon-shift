package nelon.arrive.nelonshift.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.UserDto;
import nelon.arrive.nelonshift.mappers.UserMapper;
import nelon.arrive.nelonshift.request.LoginRequest;
import nelon.arrive.nelonshift.request.SignupRequest;
import nelon.arrive.nelonshift.response.AuthResponse;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.services.interfaces.IAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
	
	private final IAuthService authService;
	private final UserMapper userMapper;
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
		@Valid @RequestBody LoginRequest loginRequest,
		HttpServletResponse response
	) {
		AuthResponse authResponse = authService.login(loginRequest, response);
		return ResponseEntity.ok(authResponse);
	}
	
	@PostMapping("/signup")
	public ResponseEntity<MessageResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
		return ResponseEntity.ok(authService.signup(signupRequest));
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<MessageResponse> refresh(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		return ResponseEntity.ok(authService.refreshToken(request, response));
	}
	
	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		return ResponseEntity.ok(authService.logout(request, response));
	}
	
	@GetMapping("/me")
	public ResponseEntity<UserDto> currentUser() {
		UserDto userDto = userMapper.toDto(authService.getCurrentUser());
		return ResponseEntity.ok(userDto);
	}
}
