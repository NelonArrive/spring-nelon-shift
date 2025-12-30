package nelon.arrive.nelonshift.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import nelon.arrive.nelonshift.security.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtils {
	
	@Value("${jwt.secret}")
	private String jwtSecret;
	
	@Value("${jwt.expiration}")
	private int expirationTime;
	
	public String generateTokenForUser(Authentication authentication) {
		CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
		
		Instant now = Instant.now();
		
		return Jwts.builder()
			.subject(userPrincipal.getEmail())
			.claim("id", userPrincipal.getId())
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusMillis(expirationTime)))
			.signWith(key())
			.compact();
	}
	
	private SecretKey key() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}
	
	public String getUsernameFromToken(String token) {
		return Jwts.parser()
			.verifyWith(key())
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getSubject();
	}
	
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(key())
				.build()
				.parseSignedClaims(token)
				.getPayload();
			return true;
		} catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException |
		         IllegalArgumentException e) {
			throw new JwtException(e.getMessage());
		}
	}
	
}
