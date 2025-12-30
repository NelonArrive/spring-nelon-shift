package nelon.arrive.nelonshift.repository;

import nelon.arrive.nelonshift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
	List<Shift> findByProjectId(Long projectId);
	
	boolean existsByProjectIdAndDate(Long projectId, LocalDate date);
}