package nelon.arrive.nelonshift.repository;

import nelon.arrive.nelonshift.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
	
	Optional<RefreshToken> findByToken(String token);
	
	List<RefreshToken> findByUserId(UUID userId);
	
	void deleteByUserId(UUID userId);
	
	void deleteByToken(String token);
}
