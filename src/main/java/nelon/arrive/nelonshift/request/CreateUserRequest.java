package nelon.arrive.nelonshift.request;

import lombok.Data;

@Data
public class CreateUserRequest {
	private String name;
	private String email;
	private String password;
}
