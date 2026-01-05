package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.request.LoginRequest;
import nelon.arrive.nelonshift.request.SignupRequest;
import nelon.arrive.nelonshift.request.TokenRefreshRequest;
import nelon.arrive.nelonshift.response.JwtResponse;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.response.TokenRefreshResponse;

public interface IAuthService {
	JwtResponse login(LoginRequest request);
	
	TokenRefreshResponse refreshToken(TokenRefreshRequest request);
	
	MessageResponse signup(SignupRequest request);
	
	MessageResponse logout(TokenRefreshRequest request);
	
	User getCurrentUser();
}
