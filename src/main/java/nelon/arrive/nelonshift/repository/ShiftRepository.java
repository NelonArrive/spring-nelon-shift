package nelon.arrive.nelonshift.repository;

import nelon.arrive.nelonshift.entity.Shift;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.LocalDate;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
	Page<Shift> findByProjectId(Long projectId, Pageable pageable);
	
	Page<Shift> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
	
	@Query("SELECT s FROM Shift s WHERE " +
		"(:projectId IS NULL OR s.project.id = :projectId) AND " +
		"(:startDate IS NULL OR s.date >= :startDate) AND " +
		"(:endDate IS NULL OR s.date <= :endDate) AND " +
		"(:minHours IS NULL OR s.hours >= :minHours)")
	Page<Shift> findByFilters(
		@Param("projectId") Long projectId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("minHours") Integer minHours,
		Pageable pageable
	);
}
