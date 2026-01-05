package nelon.arrive.nelonshift.services;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.UserDto;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.exception.ResourceNotFoundException;
import nelon.arrive.nelonshift.mappers.UserMapper;
import nelon.arrive.nelonshift.repository.UserRepository;
import nelon.arrive.nelonshift.request.UpdateUserRequest;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.services.interfaces.IUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService {
	
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	
	@Override
	@Transactional(readOnly = true)
	public List<UserDto> getAllUsers() {
		List<User> users = userRepository.findAll();
		return userMapper.toDtoList(users);
	}
	
	@Override
	@Transactional(readOnly = true)
	public UserDto getUserById(UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User not found!"));
		return userMapper.toDto(user);
	}
	
	@Override
	public UserDto updateUser(UpdateUserRequest request, UUID userId) {
		User user = userRepository.findById(userId).map(exisingUser -> {
			exisingUser.setName(request.getName());
			return userRepository.save(exisingUser);
		}).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
		return userMapper.toDto(user);
	}
	
	@Override
	public MessageResponse deleteUser(UUID userId) {
		userRepository.findById(userId).ifPresentOrElse(userRepository::delete, () -> {
			throw new ResourceNotFoundException("User not found!");
		});
		
		return new MessageResponse("Delete user successfully");
	}
}
