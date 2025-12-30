package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.request.CreateUserRequest;
import nelon.arrive.nelonshift.request.UpdateUserRequest;

import java.util.UUID;

public interface IUserService {
	User getUserById(UUID userId);
	
	User createUser(CreateUserRequest request);
	
	User updateUser(UpdateUserRequest request, UUID userId);
	
	void deleteUser(UUID userId);
	
	User getAuthenticatedUser();
}
