package nelon.arrive.nelonshift.controller;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.UserDto;
import nelon.arrive.nelonshift.request.UpdateUserRequest;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.services.interfaces.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
	
	private final IUserService userService;
	
	@GetMapping("/all")
	public ResponseEntity<List<UserDto>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}
	
	@PutMapping("/{userId}")
	public ResponseEntity<UserDto> updateUser(
		@RequestBody UpdateUserRequest request,
		@PathVariable UUID userId
	) {
		return ResponseEntity.ok(userService.updateUser(request, userId));
	}
	
	@DeleteMapping("/{userId}")
	public ResponseEntity<MessageResponse> deleteUser(@PathVariable UUID userId) {
		return ResponseEntity.ok(userService.deleteUser(userId));
	}
	
}
