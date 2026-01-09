package nelon.arrive.nelonshift.repository;

import nelon.arrive.nelonshift.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
	
	Optional<RefreshToken> findByToken(String token);
	
	void deleteByToken(String token);
}
