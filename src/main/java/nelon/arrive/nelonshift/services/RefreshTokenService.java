package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.entity.RefreshToken;
import nelon.arrive.nelonshift.exception.TokenRefreshException;
import nelon.arrive.nelonshift.repository.RefreshTokenRepository;
import nelon.arrive.nelonshift.security.jwt.JwtUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
			.userId(userId)
			.token(tokenValue)
			.ttl(expirationMs / 1000)
			.build();
		
		refreshTokenRepository.save(refreshToken);
		
		log.info("Created refresh token for user: {}, expires at: {}", userId, expiresAt);
		
		return tokenValue;
	}
	
	public RefreshToken verifyExpiration(String token) {
		return refreshTokenRepository.findById(token)
			.orElseThrow(() ->
				new TokenRefreshException("Refresh token not found or expired"));
	}
	
	public String rotateRefreshToken(String oldToken) {
		RefreshToken oldRefreshToken = verifyExpiration(oldToken);
		
		refreshTokenRepository.delete(oldRefreshToken);
		
		return createRefreshToken(oldRefreshToken.getUserId());
	}
	
	public void deleteByToken(String token) {
		refreshTokenRepository.findByToken(token)
			.ifPresent(refreshToken -> {
				refreshTokenRepository.delete(refreshToken);
				log.info("Deleted refresh token for user: {}", refreshToken.getUserId());
			});
	}
}
