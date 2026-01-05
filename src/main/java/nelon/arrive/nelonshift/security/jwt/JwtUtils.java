package nelon.arrive.nelonshift.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.security.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {
	
	@Value("${jwt.secret}")
	private String jwtSecret;
	
	@Value("${jwt.access-token-expiration}")
	private int accessTokenExpirationMs;
	
	@Getter
	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpirationMs;
	
	public String generateAccessToken(Authentication authentication) {
		CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
		
		return Jwts.builder()
			.subject(userPrincipal.getId().toString())
			.claim("email", userPrincipal.getEmail())
			.claim("name", userPrincipal.getName())
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
			.signWith(key())
			.compact();
	}
	
	public String generateAccessToken(UUID userId, String email, String name) {
		return Jwts.builder()
			.subject(userId.toString())
			.claim("email", email)
			.claim("name", name)
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
			.signWith(key())
			.compact();
	}
	
	public String generateRefreshToken() {
		return UUID.randomUUID().toString();
	}
	
	private SecretKey key() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}
	
	public UUID getUserIdFromJwtToken(String token) {
		String userId = Jwts.parser()
			.verifyWith(key())
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getSubject();
		
		return UUID.fromString(userId);
	}
	
	public String getEmailFromJwtToken(String token) {
		
		Claims claims = Jwts.parser()
			.verifyWith(key())
			.build()
			.parseSignedClaims(token)
			.getPayload();
		
		return claims.get("email", String.class);
	}
	
	public Claims getAllClaimsFromToken(String token) {
		return Jwts.parser()
			.verifyWith(key())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
	
	public boolean validateAccessToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(key())
				.build()
				.parseSignedClaims(token)
				.getPayload();
			
			return true;
		} catch (MalformedJwtException e) {
			log.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty: {}", e.getMessage());
		} catch (Exception e) {
			log.error("JWT validation error: {}", e.getMessage());
		}
		
		return false;
	}
	
}
