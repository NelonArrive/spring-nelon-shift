package nelon.arrive.nelonshift.repository;

import nelon.arrive.nelonshift.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	
	@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.projects")
	List<User> findAllWithProjects();
	
	boolean existsByEmail(String email);
	
	Optional<User> findByEmail(String email);
}
