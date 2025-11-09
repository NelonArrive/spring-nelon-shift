package nelon.arrive.nelonshift.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
class JwtResponse {
	private String token;
	private String type = "Bearer";
	private UUID id;
	private String email;
	private String name;
	private Set<String> roles;
	
	public JwtResponse(String token, UUID id, String email, String name, Set<String> roles) {
		this.token = token;
		this.id = id;
		this.email = email;
		this.name = name;
		this.roles = roles;
	}
}