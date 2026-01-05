package nelon.arrive.nelonshift.services;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.entity.RefreshToken;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.exception.AlreadyExistsException;
import nelon.arrive.nelonshift.exception.ResourceNotFoundException;
import nelon.arrive.nelonshift.repository.UserRepository;
import nelon.arrive.nelonshift.request.LoginRequest;
import nelon.arrive.nelonshift.request.SignupRequest;
import nelon.arrive.nelonshift.request.TokenRefreshRequest;
import nelon.arrive.nelonshift.response.JwtResponse;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.response.TokenRefreshResponse;
import nelon.arrive.nelonshift.security.jwt.JwtUtils;
import nelon.arrive.nelonshift.security.user.CustomUserDetails;
import nelon.arrive.nelonshift.services.interfaces.IAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
	
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;
	private final RefreshTokenService refreshTokenService;
	
	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpirationMs;
	
	/**
	 * POST /api/auth/login - Вход в систему
	 * Процесс:
	 * 1. Проверяем email и пароль
	 * 2. Генерируем Access Token (15 минут)
	 * 3. Генерируем Refresh Token (30 дней) и сохраняем в Redis
	 * 4. Возвращаем оба токена

	 * Клиент должен:
	 * - Хранить Access Token в памяти (не в localStorage!)
	 * - Хранить Refresh Token в httpOnly cookie (или secure storage)
	 */
	@Override
	public JwtResponse login(LoginRequest loginRequest) {
		log.info("Login attempt for email: {}", loginRequest.getEmail());
		
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
				loginRequest.getEmail(),
				loginRequest.getPassword()
			)
		);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		String accessToken = jwtUtils.generateAccessToken(authentication);
		
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		String refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
		
		log.info("Login successful for user: {}", userDetails.getEmail());
		
		return new JwtResponse(
			accessToken,
			refreshToken,
			accessTokenExpirationMs / 1000,
			userDetails.getId(),
			userDetails.getEmail(),
			userDetails.getName()
		);
	}
	
	/**
	 * POST /api/auth/refresh - Обновление Access Token
	 * Процесс (Token Rotation):
	 * 1. Проверяем Refresh Token в Redis
	 * 2. Генерируем новый Access Token
	 * 3. Генерируем новый Refresh Token (Token Rotation!)
	 * 4. Удаляем старый Refresh Token из Redis
	 * 5. Возвращаем оба новых токена
	 */
	@PostMapping("/refresh")
	@Override
	public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
		String requestRefreshToken = request.getRefreshToken();
		
		log.info("Token refresh request");
		
		// 1. Проверяем и получаем информацию о токене из Redis
		RefreshToken refreshToken = refreshTokenService.verifyExpiration(requestRefreshToken);
		
		// 2. Загружаем пользователя
		User user = userRepository.findById(refreshToken.getUserId())
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		
		// 4. Генерируем новый Access Token
		String newAccessToken = jwtUtils.generateAccessToken(
			user.getId(),
			user.getEmail(),
			user.getName()
		);
		
		// 5. Token Rotation - создаём новый Refresh Token, удаляем старый
		String newRefreshToken = refreshTokenService.rotateRefreshToken(requestRefreshToken);
		
		log.info("Token refresh successful for user: {}", user.getEmail());
		
		return new TokenRefreshResponse(
			newAccessToken,
			newRefreshToken,
			accessTokenExpirationMs / 1000
		);
	}
	
	/**
	 * POST /api/auth/signup - Регистрация нового пользователя
	 */
	@Override
	public MessageResponse signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new AlreadyExistsException("Email already in use");
		}
		
		User user = User.builder()
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.name(request.getName())
			.build();
		
		userRepository.save(user);
		
		log.info("User registered successfully: {}", user.getEmail());
		
		return new MessageResponse("User registered successfully");
	}
	
	/**
	 * POST /api/auth/logout - Выход (удаление Refresh Token)
	 * Удаляет текущий Refresh Token из Redis
	 * Access Token всё ещё будет работать до истечения срока действия (15 минут)
	 */
	@Override
	public MessageResponse logout(TokenRefreshRequest request) {
		String refreshToken = request.getRefreshToken();
		
		refreshTokenService.deleteByToken(refreshToken);
		
		log.info("Logout successful, refresh token deleted");
		
		return new MessageResponse("Logged out successfully");
	}
	
	/**
	 * GET /api/auth/me - Получить информацию о текущем пользователе
	 * Требует Access Token в заголовке Authorization
	 */
	@Override
	public User getCurrentUser() {
		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null ||
			!authentication.isAuthenticated() ||
			authentication.getPrincipal().equals("anonymousUser")) {
			throw new JwtException("User is not authenticated");
		}
		
		CustomUserDetails userDetails =
			(CustomUserDetails) authentication.getPrincipal();
		
		return userRepository.findById(userDetails.getId())
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
	
}

