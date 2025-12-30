package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.dto.ShiftDto;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.exception.AlreadyExistsException;
import nelon.arrive.nelonshift.exception.BadRequestException;
import nelon.arrive.nelonshift.exception.ResourceNotFoundException;
import nelon.arrive.nelonshift.exception.ValidationException;
import nelon.arrive.nelonshift.repository.ProjectRepository;
import nelon.arrive.nelonshift.repository.ShiftRepository;
import nelon.arrive.nelonshift.services.interfaces.IShiftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftService implements IShiftService {
	private final ShiftRepository shiftRepository;
	private final ProjectRepository projectRepository;
	
	@Transactional(readOnly = true)
	public List<ShiftDto> getShiftsByProjectId(Long projectId) {
		if (projectId == null || projectId <= 0) {
			throw new ValidationException("Project ID must be a positive number");
		}
		
		if (!projectRepository.existsById(projectId)) {
			throw new ResourceNotFoundException("Project not found with id: " + projectId);
		}
		
		List<Shift> shifts = shiftRepository.findByProjectId(projectId);
		
		if (shifts.isEmpty()) {
			log.info("No shifts found for project with id: {}", projectId);
			return List.of();
		}
		
		return shifts.stream()
			.map(ShiftDto::new)
			.collect(Collectors.toList());
	}
	
	@Transactional
	public ShiftDto createShift(Long projectId, Shift shift) {
		if (projectId == null || projectId <= 0) {
			throw new ValidationException("Project ID must be a positive number");
		}
		
		validateShift(shift);
		
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
		
		if (project.getStatus() == Project.ProjectStatus.CANCELLED) {
			throw new BadRequestException("Cannot create shift for cancelled project");
		}
		
		validateShiftDateAgainstProject(shift.getDate(), project);
		
		if (shiftRepository.existsByProjectIdAndDate(projectId, shift.getDate())) {
			throw new AlreadyExistsException("Shift already exists for this project on date: " + shift.getDate());
		}
		
		shift.setProject(project);
		Shift savedShift = shiftRepository.save(shift);
		log.info("Created shift with id: {} for project: {}", savedShift.getId(), projectId);
		
		return new ShiftDto(savedShift);
	}
	
	public ShiftDto updateShift(Long id, Shift shiftDetails) {
		if (id == null || id <= 0) {
			throw new ValidationException("Shift ID must be a positive number");
		}
		
		validateShift(shiftDetails);
		
		Shift shift = shiftRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
		
		if (shift.getProject().getStatus() == Project.ProjectStatus.COMPLETED) {
			throw new BadRequestException("Cannot update shift for completed project");
		}
		
		if (!shift.getDate().equals(shiftDetails.getDate())) {
			if (shiftRepository.existsByProjectIdAndDate(shift.getProject().getId(), shiftDetails.getDate())) {
				throw new AlreadyExistsException("Shift already exists for this project on date: " + shiftDetails.getDate());
			}
			
			validateShiftDateAgainstProject(shiftDetails.getDate(), shift.getProject());
		}
		
		shift.setDate(shiftDetails.getDate());
		shift.setStartTime(shiftDetails.getStartTime());
		shift.setEndTime(shiftDetails.getEndTime());
		shift.setHours(shiftDetails.getHours());
		shift.setBasePay(shiftDetails.getBasePay());
		shift.setOvertimeHours(shiftDetails.getOvertimeHours());
		shift.setOvertimePay(shiftDetails.getOvertimePay());
		shift.setPerDiem(shiftDetails.getPerDiem());
		
		Shift updatedShift = shiftRepository.save(shift);
		log.info("Updated shift with id: {}", id);
		
		return new ShiftDto(updatedShift);
	}
	
	public void deleteShift(Long id) {
		if (id == null || id <= 0) {
			throw new ValidationException("Shift ID must be a positive number");
		}
		
		Shift shift = shiftRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
		
		if (shift.getProject().getStatus() == Project.ProjectStatus.COMPLETED) {
			throw new BadRequestException("Cannot delete shift from completed project");
		}
		
		shiftRepository.deleteById(id);
		log.info("Deleted shift with id: {}", id);
	}
	
	private void validateShift(Shift shift) {
		if (shift == null) {
			throw new ValidationException("Shift data is required");
		}
		
		// Валидация даты
		if (shift.getDate() == null) {
			throw new ValidationException("Shift date is required");
		}
		
		if (shift.getDate().isAfter(LocalDate.now().plusYears(1))) {
			throw new ValidationException("Shift date cannot be more than 1 year in the future");
		}
		
		// Валидация времени
		if (shift.getStartTime() != null && shift.getEndTime() != null) {
			if (shift.getEndTime().isBefore(shift.getStartTime())) {
				throw new ValidationException("End time must be after start time");
			}
		}
		
		// Валидация часов
		if (shift.getHours() == null) {
			throw new ValidationException("Hours is required");
		}
		
		if (shift.getHours() < 0 || shift.getHours() > 24) {
			throw new ValidationException("Hours must be between 0 and 24");
		}
		
		// Валидация базовой зарплаты
		if (shift.getBasePay() == null) {
			throw new ValidationException("Base pay is required");
		}
		
		if (shift.getBasePay().compareTo(BigDecimal.ZERO) < 0) {
			throw new ValidationException("Base pay must be positive");
		}
		
		// Валидация сверхурочных
		if (shift.getOvertimeHours() != null) {
			if (shift.getOvertimeHours() < 0) {
				throw new ValidationException("Overtime hours must be positive");
			}
			
			if (shift.getOvertimeHours() > 0 && (shift.getOvertimePay() == null || shift.getOvertimePay().compareTo(BigDecimal.ZERO) <= 0)) {
				throw new ValidationException("Overtime pay is required when overtime hours are specified");
			}
		}
		
		// Валидация суточных
		if (shift.getPerDiem() != null && shift.getPerDiem().compareTo(BigDecimal.ZERO) < 0) {
			throw new ValidationException("Per diem must be positive");
		}
		
		// Бизнес-логика: проверяем соответствие часов времени
		if (shift.getStartTime() != null && shift.getEndTime() != null) {
			long calculatedHours = calculateHoursBetween(shift.getStartTime(), shift.getEndTime());
			if (Math.abs(calculatedHours - shift.getHours()) > 1) {
				log.warn("Hours mismatch: declared {} but calculated {}", shift.getHours(), calculatedHours);
			}
		}
	}
	
	private void validateShiftDateAgainstProject(LocalDate shiftDate, Project project) {
		if (project.getStartDate() != null && shiftDate.isBefore(project.getStartDate())) {
			throw new BadRequestException("Shift date cannot be before project start date");
		}
		
		if (project.getEndDate() != null && shiftDate.isAfter(project.getEndDate())) {
			throw new BadRequestException("Shift date cannot be after project end date");
		}
	}
	
	private long calculateHoursBetween(LocalTime start, LocalTime end) {
		return java.time.Duration.between(start, end).toHours();
	}
}
