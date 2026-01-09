package nelon.arrive.nelonshift.security.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {
	
	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpirationMs;
	
	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpirationMs;
	
	@Value("${cookie.domain:localhost}")
	private String cookieDomain;
	
	@Value("${cookie.secure:false}")
	private boolean cookieSecure;
	
	public static final String ACCESS_TOKEN_COOKIE = "accessToken";
	public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
	
	/**
	 * Создать Access Token cookie
	 *
	 * Параметры:
	 * - HttpOnly: true (JS не может прочитать)
	 * - Secure: true в prod (только HTTPS)
	 * - SameSite: Strict (защита от CSRF)
	 * - Path: / (доступен везде)
	 * - MaxAge: 15 минут
	 */
	public void setAccessTokenCookie(HttpServletResponse response, String token) {
		Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, token);
		cookie.setHttpOnly(true); // JavaScript НЕ может прочитать
		cookie.setSecure(cookieSecure); // Только HTTPS в prod
		cookie.setPath("/"); // Доступен для всех путей
		cookie.setMaxAge((int) (accessTokenExpirationMs / 1000)); // 15 минут
		cookie.setDomain(cookieDomain); // localhost или твой домен
		// SameSite=Strict добавляется через заголовок ниже
		
		response.addCookie(cookie);
		
		// Добавляем SameSite через заголовок (Spring Boot не поддерживает напрямую)
		response.addHeader("Set-Cookie",
			String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; %sSameSite=Strict",
				ACCESS_TOKEN_COOKIE,
				token,
				(int) (accessTokenExpirationMs / 1000),
				cookieSecure ? "Secure; " : ""
			)
		);
	}
	
	/**
	 * Создать Refresh Token cookie
	 *
	 * Параметры такие же, но живёт 30 дней
	 */
	public void setRefreshTokenCookie(HttpServletResponse response, String token) {
		Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, token);
		cookie.setHttpOnly(true);
		cookie.setSecure(cookieSecure);
		cookie.setPath("/");
		cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000)); // 30 дней
		cookie.setDomain(cookieDomain);
		
		response.addCookie(cookie);
		
		response.addHeader("Set-Cookie",
			String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; %sSameSite=Strict",
				REFRESH_TOKEN_COOKIE,
				token,
				(int) (refreshTokenExpirationMs / 1000),
				cookieSecure ? "Secure; " : ""
			)
		);
	}
	
	public Optional<String> getAccessTokenFromCookie(HttpServletRequest request) {
		return getCookieValue(request, ACCESS_TOKEN_COOKIE);
	}
	
	public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
		return getCookieValue(request, REFRESH_TOKEN_COOKIE);
	}
	
	public void deleteAccessTokenCookie(HttpServletResponse response) {
		deleteCookie(response, ACCESS_TOKEN_COOKIE);
	}
	
	public void deleteRefreshTokenCookie(HttpServletResponse response) {
		deleteCookie(response, REFRESH_TOKEN_COOKIE);
	}
	
	public void deleteAllTokenCookies(HttpServletResponse response) {
		deleteAccessTokenCookie(response);
		deleteRefreshTokenCookie(response);
	}
	
	private Optional<String> getCookieValue(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		
		if (cookies == null) {
			return Optional.empty();
		}
		
		return Arrays.stream(cookies)
			.filter(cookie -> name.equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst();
	}
	
	private void deleteCookie(HttpServletResponse response, String name) {
		Cookie cookie = new Cookie(name, null);
		cookie.setHttpOnly(true);
		cookie.setSecure(cookieSecure);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		cookie.setDomain(cookieDomain);
		
		response.addCookie(cookie);
	}
}
