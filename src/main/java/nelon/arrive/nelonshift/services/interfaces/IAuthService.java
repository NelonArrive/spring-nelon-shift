package nelon.arrive.nelonshift.services.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.request.LoginRequest;
import nelon.arrive.nelonshift.request.SignupRequest;
import nelon.arrive.nelonshift.response.AuthResponse;
import nelon.arrive.nelonshift.response.MessageResponse;

public interface IAuthService {
	
	AuthResponse login(LoginRequest loginRequest, HttpServletResponse response);
	
	MessageResponse refreshToken(HttpServletRequest request, HttpServletResponse response);
	
	MessageResponse signup(SignupRequest request);
	
	MessageResponse logout(
		HttpServletRequest request,
		HttpServletResponse response
	);
	
	User getCurrentUser();
}
