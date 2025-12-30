package nelon.arrive.nelonshift.repository;

import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	boolean existsByName(String name);
	
	@Query("SELECT p FROM Project p WHERE " +
		"(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
		"(:status IS NULL OR p.status = :status) AND " +
		"(:startDate IS NULL OR p.startDate >= :startDate) AND " +
		"(:endDate IS NULL OR p.endDate <= :endDate)")
	Page<Project> findByFilters(
		@Param("name") String name,
		@Param("status") ProjectStatus status,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		Pageable pageable
	);
}
