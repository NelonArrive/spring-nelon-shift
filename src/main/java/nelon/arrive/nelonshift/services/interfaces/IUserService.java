package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.dto.UserDto;
import nelon.arrive.nelonshift.request.UpdateUserRequest;
import nelon.arrive.nelonshift.response.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface IUserService {
	List<UserDto> getAllUsers();
	
	UserDto getUserById(UUID userId);
	
	UserDto updateUser(UpdateUserRequest request, UUID userId);
	
	MessageResponse deleteUser(UUID userId);
	
}
