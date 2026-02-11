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
import nelon.arrive.nelonshift.mappers.ShiftMapper;
import nelon.arrive.nelonshift.repository.ProjectRepository;
import nelon.arrive.nelonshift.repository.ShiftRepository;
import nelon.arrive.nelonshift.request.CreateShiftRequest;
import nelon.arrive.nelonshift.request.UpdateShiftRequest;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.services.interfaces.IShiftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftService implements IShiftService {
	
	private final ShiftRepository shiftRepository;
	private final ProjectRepository projectRepository;
	private final ShiftMapper shiftMapper;
	
	@Override
	@Transactional(readOnly = true)
	public List<ShiftDto> getShiftsByProjectId(Long projectId) {
		if (!projectRepository.existsById(projectId)) {
			throw new ResourceNotFoundException("Project not found");
		}
		List<Shift> shifts = shiftRepository.findByProjectId(projectId);
		return shiftMapper.toDtoList(shifts);
	}
	
	@Override
	public ShiftDto createShift(CreateShiftRequest request) {
		validateShiftCreate(request);
		
		Project project = projectRepository.findById(request.getProjectId())
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		
		validateShiftDateAgainstProject(request.getDate(), project);
		
		if (shiftRepository.existsByProjectIdAndDate(request.getProjectId(), request.getDate())) {
			throw new AlreadyExistsException("Shift already exists for this project on date: " + request.getDate());
		}
		
		Shift shift = new Shift();
		shift.setDate(request.getDate());
		shift.setStartTime(request.getStartTime());
		shift.setEndTime(request.getEndTime());
		shift.setHours(request.getHours());
		shift.setBasePay(request.getBasePay());
		shift.setOvertimeHours(request.getOvertimeHours());
		shift.setOvertimePay(request.getOvertimePay());
		shift.setPerDiem(request.getPerDiem());
		shift.setCompensation(request.getCompensation());
		shift.setProject(project);

		Shift savedShift = shiftRepository.save(shift);
		log.info("Created shift with id: {} for project: {}", savedShift.getId(), request.getProjectId());

		return shiftMapper.toDto(savedShift);
	}
	
	@Override
	public ShiftDto updateShift(Long id, UpdateShiftRequest shiftDetails) {
		validateShiftUpdate(shiftDetails);
		
		Shift shift = shiftRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
		
		if (!shift.getDate().equals(shiftDetails.getDate())) {
			if (shiftRepository.existsByProjectIdAndDate(shift.getProject().getId(), shiftDetails.getDate())) {
				throw new AlreadyExistsException("Shift already exists for this project on date: " + shiftDetails.getDate());
			}
			validateShiftDateAgainstProject(shiftDetails.getDate(), shift.getProject());
			shift.setDate(shiftDetails.getDate());
		}
		
		shift.setStartTime(shiftDetails.getStartTime());
		shift.setEndTime(shiftDetails.getEndTime());
		shift.setHours(shiftDetails.getHours());
		shift.setBasePay(shiftDetails.getBasePay());
		shift.setOvertimeHours(shiftDetails.getOvertimeHours());
		shift.setOvertimePay(shiftDetails.getOvertimePay());
		shift.setPerDiem(shiftDetails.getPerDiem());
		shift.setCompensation(shiftDetails.getCompensation());

		Shift updatedShift = shiftRepository.save(shift);
		log.info("Updated shift with id: {}", id);
		
		return shiftMapper.toDto(updatedShift);
	}
	
	@Override
	public MessageResponse deleteShift(Long id) {
		if (!shiftRepository.existsById(id)) {
			throw new ResourceNotFoundException("Shift not found");
		}
		
		shiftRepository.deleteById(id);
		log.info("Deleted shift with id: {}", id);
		
		return new MessageResponse("Delete shift successfully");
	}
	
	@Override
	public void validateShiftCreate(CreateShiftRequest request) {
		// Валидация времени
		if (request.getStartTime() != null && request.getEndTime() != null) {
			if (request.getEndTime().isBefore(request.getStartTime())) {
				throw new ValidationException("End time must be after start time");
			}
		}
		
		// Обязательные поля
		if (request.getHours() == null) {
			throw new ValidationException("Hours is required");
		}
		
		if (request.getHours() < 0 || request.getHours() > 24) {
			throw new ValidationException("Hours must be between 0 and 24");
		}
		
		if (request.getBasePay() == null) {
			throw new ValidationException("Base pay is required");
		}
		
		if (request.getBasePay().compareTo(BigDecimal.ZERO) < 0) {
			throw new ValidationException("Base pay must be positive");
		}
		
		// Валидация сверхурочных
		if (request.getOvertimeHours() != null) {
			if (request.getOvertimeHours() < 0) {
				throw new ValidationException("Overtime hours must be positive");
			}
			
			if (request.getOvertimeHours() > 0 &&
				(request.getOvertimePay() == null || request.getOvertimePay().compareTo(BigDecimal.ZERO) <= 0)) {
				throw new ValidationException("Overtime pay is required when overtime hours are specified");
			}
		}
	}
	
	@Override
	public void validateShiftUpdate(UpdateShiftRequest request) {
		// Валидация времени (только если оба поля присутствуют)
		if (request.getStartTime() != null && request.getEndTime() != null) {
			if (request.getEndTime().isBefore(request.getStartTime())) {
				throw new ValidationException("End time must be after start time");
			}
		}
		
		// Валидация часов (только если присутствует)
		if (request.getHours() != null && (request.getHours() < 0 || request.getHours() > 24)) {
			throw new ValidationException("Hours must be between 0 and 24");
		}
		
		// Валидация базовой зарплаты (только если присутствует)
		if (request.getBasePay() != null && request.getBasePay().compareTo(BigDecimal.ZERO) < 0) {
			throw new ValidationException("Base pay must be positive");
		}
		
		// Валидация сверхурочных
		if (request.getOvertimeHours() != null) {
			if (request.getOvertimeHours() < 0) {
				throw new ValidationException("Overtime hours must be positive");
			}
			
			if (request.getOvertimeHours() > 0 &&
				(request.getOvertimePay() == null || request.getOvertimePay().compareTo(BigDecimal.ZERO) <= 0)) {
				throw new ValidationException("Overtime pay is required when overtime hours are specified");
			}
		}
		
		// Валидация per diem (только если присутствует)
		if (request.getPerDiem() != null && request.getPerDiem().compareTo(BigDecimal.ZERO) < 0) {
			throw new ValidationException("Per diem must be positive");
		}
	}
	
	@Override
	public void validateShiftDateAgainstProject(LocalDate shiftDate, Project project) {
		if (project.getStartDate() != null && shiftDate.isBefore(project.getStartDate())) {
			throw new BadRequestException("Shift date cannot be before project start date");
		}
		
		if (project.getEndDate() != null && shiftDate.isAfter(project.getEndDate())) {
			throw new BadRequestException("Shift date cannot be after project end date");
		}
	}
}
