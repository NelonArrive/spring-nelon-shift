package nelon.arrive.nelonshift.services;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.entity.RefreshToken;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.exception.AlreadyExistsException;
import nelon.arrive.nelonshift.exception.ResourceNotFoundException;
import nelon.arrive.nelonshift.exception.TokenRefreshException;
import nelon.arrive.nelonshift.repository.UserRepository;
import nelon.arrive.nelonshift.request.LoginRequest;
import nelon.arrive.nelonshift.request.SignupRequest;
import nelon.arrive.nelonshift.response.AuthResponse;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.security.jwt.JwtUtils;
import nelon.arrive.nelonshift.security.user.CustomUserDetails;
import nelon.arrive.nelonshift.security.utils.CookieUtil;
import nelon.arrive.nelonshift.services.interfaces.IAuthService;
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
	private final CookieUtil cookieUtil;
	
	/**
	 * POST /api/auth/login - Вход в систему
	 * Процесс:
	 * 1. Проверяем email и пароль
	 * 2. Генерируем Access Token (15 минут)
	 * 3. Генерируем Refresh Token (30 дней)
	 * 4. Записываем оба токена в HTTP-only cookies
	 * 5. Возвращаем только информацию о пользователе (БЕЗ токенов!)
	 * <p>
	 * Токены автоматически отправляются браузером в каждом запросе
	 */
	@Override
	public AuthResponse login(LoginRequest loginRequest, HttpServletResponse response) {
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

		cookieUtil.setAccessTokenCookie(response, accessToken);
		cookieUtil.setRefreshTokenCookie(response, refreshToken);

		log.info("Login successful for user: {}", userDetails.getEmail());

		return new AuthResponse(
			userDetails.getId(),
			userDetails.getEmail(),
			userDetails.getName()
		);
	}
	
	/**
	 * POST /api/auth/refresh - Обновление Access Token
	 * Процесс (Token Rotation):
	 * 1. Читаем Refresh Token из cookie (автоматически)
	 * 2. Проверяем токен в Redis
	 * 3. Генерируем новый Access Token
	 * 4. Генерируем новый Refresh Token (Token Rotation!)
	 * 5. Обновляем оба токена в cookies
	 */
	@PostMapping("/refresh")
	@Override
	public MessageResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
		log.info("Token refresh request");
		
		// 1. Читаем Refresh Token из cookie
		String refreshToken = cookieUtil.getRefreshTokenFromCookie(request)
			.orElseThrow(() -> new TokenRefreshException("Refresh token not found in cookies"));
		
		// 2. Проверяем и получаем информацию о токене из Redis
		RefreshToken refreshTokenEntity = refreshTokenService.verifyExpiration(refreshToken);
		
		// 3. Загружаем пользователя
		User user = userRepository.findById(refreshTokenEntity.getUserId())
			.orElseThrow(() -> new RuntimeException("User not found"));
		
		// 4. Генерируем новый Access Token
		String newAccessToken = jwtUtils.generateAccessToken(
			user.getId(),
			user.getEmail(),
			user.getName()
		);
		
		// 5. Token Rotation - создаём новый Refresh Token, удаляем старый
		String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
		
		// 7. Обновляем оба токена в cookies
		cookieUtil.setAccessTokenCookie(response, newAccessToken);
		cookieUtil.setRefreshTokenCookie(response, newRefreshToken);
		
		log.info("Token refresh successful for user: {}", user.getEmail());
		
		return new MessageResponse("Token refreshed successfully");
	}
	
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
	 * POST /api/auth/logout - Выход (удаление токенов)
	 * 1. Читаем Refresh Token из cookie
	 * 2. Удаляем Refresh Token из Redis
	 * 3. Удаляем оба токена из cookies
	 * Access Token всё ещё будет работать до истечения (макс 15 минут)
	 */
	@Override
	public MessageResponse logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		// Читаем Refresh Token из cookie (если есть)
		cookieUtil.getRefreshTokenFromCookie(request).ifPresent(refreshTokenService::deleteByToken);
		
		// Удаляем оба токена из cookies
		cookieUtil.deleteAllTokenCookies(response);
		
		log.info("Logout successful");
		
		return new MessageResponse("Logged out successfully");
	}
	
//	public MessageResponse logout(TokenRefreshRequest request) {
//		String refreshToken = request.getRefreshToken();
//
//		refreshTokenService.deleteByToken(refreshToken);
//
//		log.info("Logout successful, refresh token deleted");
//
		
		/**
		 * GET /api/auth/me - Получить информацию о текущем пользователе
		 * Требует Access Token в cookie (автоматически отправляется)
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

