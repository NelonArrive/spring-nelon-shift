package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.entity.RefreshToken;
import nelon.arrive.nelonshift.exception.TokenRefreshException;
import nelon.arrive.nelonshift.repository.RefreshTokenRepository;
import nelon.arrive.nelonshift.security.jwt.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {
	
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtUtils jwtUtils;
	
	public String createRefreshToken(UUID userId) {
		String tokenValue = jwtUtils.generateRefreshToken();
		
		long expirationMs = jwtUtils.getRefreshTokenExpirationMs();
		Instant expiresAt = Instant.now().plusMillis(expirationMs);
		
		RefreshToken refreshToken = RefreshToken.builder()
			.id(UUID.randomUUID().toString())
			.userId(userId)
			.token(tokenValue)
			.createdAt(Instant.now())
			.ttl(expirationMs / 1000)
			.build();
		
		refreshTokenRepository.save(refreshToken);
		
		log.info("Created refresh token for user: {}, expires at: {}", userId, expiresAt);
		
		return tokenValue;
	}
	
	public RefreshToken findByToken(String token) {
		return refreshTokenRepository.findByToken(token)
			.orElseThrow(() -> new TokenRefreshException("Refresh token not found or expired"));
	}
	
	public RefreshToken verifyExpiration(String token) {
		RefreshToken refreshToken = findByToken(token);
		
		if (refreshToken.isExpired()) {
			refreshTokenRepository.delete(refreshToken);
			
			log.warn("Refresh token expired: {}", token);
			throw new TokenRefreshException("Refresh token was expired. Please login again.");
		}
		
		return refreshToken;
	}
	
	@Transactional
	public String rotateRefreshToken(String oldToken) {
		RefreshToken oldRefreshToken = verifyExpiration(oldToken);
		
		UUID userId = oldRefreshToken.getUserId();
		
		refreshTokenRepository.delete(oldRefreshToken);
		log.info("Deleted old refresh token for user: {}", userId);
		
		String newToken = createRefreshToken(userId);
		log.info("Created new refresh token for user: {} (rotation)", userId);
		
		return newToken;
	}
	
	public void deleteByToken(String token) {
		refreshTokenRepository.findByToken(token)
			.ifPresent(refreshToken -> {
				refreshTokenRepository.delete(refreshToken);
				log.info("Deleted refresh token for user: {}", refreshToken.getUserId());
			});
	}
	
	public void deleteAllUserTokens(UUID userId) {
		List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
		refreshTokenRepository.deleteAll(tokens);
		
		log.info("Deleted all refresh tokens for user: {} (count: {})", userId, tokens.size());
	}
}
