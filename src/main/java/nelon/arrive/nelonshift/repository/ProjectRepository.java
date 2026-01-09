package nelon.arrive.nelonshift.repository;

import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	
	boolean existsByName(String name);
	
	@Query("""
		SELECT p FROM Project p
		LEFT JOIN p.shifts s
		WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
		AND (:status IS NULL OR p.status = :status)
		GROUP BY p.id
		""")
	Page<Project> findByFilters(
		@Param("name") String name,
		@Param("status") ProjectStatus status,
		Pageable pageable
	);
}
