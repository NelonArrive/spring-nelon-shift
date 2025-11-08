package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.dto.PageResponse;
import nelon.arrive.nelonshift.dto.ProjectDTO;
import nelon.arrive.nelonshift.entities.Project;
import nelon.arrive.nelonshift.entities.Project.ProjectStatus;
import nelon.arrive.nelonshift.exceptions.AlreadyExistsException;
import nelon.arrive.nelonshift.exceptions.BadRequestException;
import nelon.arrive.nelonshift.exceptions.BusinessLogicException;
import nelon.arrive.nelonshift.exceptions.ResourceNotFoundException;
import nelon.arrive.nelonshift.repositories.ProjectRepository;
import nelon.arrive.nelonshift.services.interfaces.IProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService implements IProjectService {
	private final ProjectRepository projectRepository;
	
	private static final int MIN_PAGE_SIZE = 1;
	private static final int MAX_PAGE_SIZE = 100;
	private static final int MIN_NAME_LENGTH = 2;
	private static final int MAX_NAME_LENGTH = 100;
	private static final List<String> VALID_SORT_FIELDS = Arrays.asList(
		"id", "name", "status", "startDate", "endDate", "createdAt", "updatedAt"
	);
	
	@Transactional(readOnly = true)
	public PageResponse<ProjectDTO> getProjects(
		String name,
		ProjectStatus status,
		LocalDate startDate,
		LocalDate endDate,
		int page,
		int size,
		String sortBy,
		String sortDirection
	) {
		validatePagination(page, size);
		validateSorting(sortBy, sortDirection);
		
		if (name != null && name.trim().length() > MAX_NAME_LENGTH) {
			throw new BadRequestException("Search name is too long (max " + MAX_NAME_LENGTH + " characters)");
		}
		
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			throw new BadRequestException("Start date cannot be after end date");
		}
		
		if (startDate != null && startDate.isBefore(LocalDate.now().minusYears(10))) {
			throw new BadRequestException("Start date cannot be more than 10 years in the past");
		}
		
		if (endDate != null && endDate.isAfter(LocalDate.now().plusYears(10))) {
			throw new BadRequestException("End date cannot be more than 10 years in the future");
		}
		
		Sort sort = sortDirection.equalsIgnoreCase("desc")
			? Sort.by(sortBy).descending()
			: Sort.by(sortBy).ascending();
		
		Pageable pageable = PageRequest.of(page, size, sort);
		
		Page<Project> projectPage = projectRepository.findByFilters(
			name, status, startDate, endDate, pageable
		);
		
		Page<ProjectDTO> dtoPage = projectPage.map(ProjectDTO::new);
		return new PageResponse<>(dtoPage);
	}
	
	@Transactional(readOnly = true)
	public ProjectDTO getProjectById(Long id) {
		Project project = projectRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
		return new ProjectDTO(project);
	}
	
	@Transactional
	public ProjectDTO createProject(Project project) {
		if (project == null) {
			throw new BadRequestException("Project data is required");
		}
		
		validateProjectName(project.getName());
		
		if (projectRepository.existsByName(project.getName().trim())) {
			throw new AlreadyExistsException("Project with name '" + project.getName() + "' already exists");
		}
		
		if (project.getStatus() == null) {
			throw new BadRequestException("Project status is required");
		}
		
		validateProjectDates(project.getStartDate(), project.getEndDate());
		
		if (project.getStatus() == ProjectStatus.COMPLETED) {
			throw new BusinessLogicException("Cannot create a project with COMPLETED status");
		}
		
		if (project.getStartDate() != null && project.getStartDate().isBefore(LocalDate.now())) {
			log.warn("Creating project with start date in the past: {}", project.getStartDate());
		}
		
		Project savedProject = projectRepository.save(project);
		log.info("Created project with id: {} and name: '{}'", savedProject.getId(), savedProject.getName());
		
		return new ProjectDTO(savedProject);
	}
	
	@Transactional
	public ProjectDTO updateProject(Long id, Project projectDetails) {
		validateProjectId(id);
		
		if (projectDetails == null) {
			throw new BadRequestException("Project update data is required");
		}
		
		Project project = projectRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
		
		// Сохраняем старые значения для логирования
		String oldName = project.getName();
		ProjectStatus oldStatus = project.getStatus();
		
		if (projectDetails.getName() != null) {
			validateProjectName(projectDetails.getName());
			
			// Проверка уникальности имени (если меняется)
			if (!project.getName().equals(projectDetails.getName().trim())) {
				if (projectRepository.existsByName(projectDetails.getName().trim())) {
					throw new AlreadyExistsException("Project with name '" + projectDetails.getName() + "' already exists");
				}
				project.setName(projectDetails.getName().trim());
			}
		}
		
		if (projectDetails.getStatus() != null) {
			validateStatusTransition(
				project.getStatus(),
				projectDetails.getStatus(),
				project
			);
			project.setStatus(projectDetails.getStatus());
		}
		
		LocalDate newStartDate = projectDetails.getStartDate() != null ?
			projectDetails.getStartDate() : project.getStartDate();
		LocalDate newEndDate = projectDetails.getEndDate() != null ?
			projectDetails.getEndDate() : project.getEndDate();
		
		validateProjectDates(newStartDate, newEndDate);
		
		if (projectDetails.getStartDate() != null || projectDetails.getEndDate() != null) {
			validateDateChangeWithShifts(project, newStartDate, newEndDate);
		}
		
		if (projectDetails.getStartDate() != null) {
			project.setStartDate(projectDetails.getStartDate());
		}
		if (projectDetails.getEndDate() != null) {
			project.setEndDate(projectDetails.getEndDate());
		}
		
		Project updatedProject = projectRepository.save(project);
		log.info(
			"Updated project id: {}. Name: '{}' -> '{}', Status: {} -> {}",
			id, oldName, updatedProject.getName(), oldStatus,
			updatedProject.getStatus()
		);
		
		return new ProjectDTO(updatedProject);
	}
	
	@Transactional
	public void deleteProject(Long id) {
		Project project = projectRepository.findById(id)
			.orElseThrow(
				() -> new ResourceNotFoundException("Project not found with id: " + id));
		
		if (!project.getShifts().isEmpty()) {
			throw new BusinessLogicException(
				"Cannot delete project with " + project.getShifts().size() + " existing shifts. Delete shifts first."
			);
		}
		
		if (project.getStatus() == ProjectStatus.COMPLETED) {
			throw new BusinessLogicException("Cannot delete completed project. Change status first.");
		}
		
		projectRepository.deleteById(id);
		log.info("Deleted project with id: {} and name: '{}'", id, project.getName());
	}
	
	// ===== ПРИВАТНЫЕ МЕТОДЫ ВАЛИДАЦИИ =====
	
	private void validateProjectId(Long id) {
		if (id == null || id <= 0) {
			throw new BadRequestException("Project ID must be a positive number");
		}
	}
	
	private void validateProjectName(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new BadRequestException("Project name cannot be empty");
		}
		
		String trimmedName = name.trim();
		if (trimmedName.length() < MIN_NAME_LENGTH) {
			throw new BadRequestException("Project name must be at least " + MIN_NAME_LENGTH + " characters");
		}
		
		if (trimmedName.length() > MAX_NAME_LENGTH) {
			throw new BadRequestException("Project name cannot exceed " + MAX_NAME_LENGTH + " characters");
		}
		
		if (trimmedName.matches(".*[<>\"'].*")) {
			throw new BadRequestException("Project name contains invalid characters");
		}
	}
	
	private void validateProjectDates(LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null) {
			if (startDate.isAfter(endDate)) {
				throw new BadRequestException("Start date cannot be after end date");
			}
			
			if (startDate.plusYears(5).isBefore(endDate)) {
				throw new BadRequestException("Project duration cannot exceed 5 years");
			}
		}
		
		if (startDate != null && startDate.isBefore(LocalDate.now().minusYears(10))) {
			throw new BadRequestException("Start date cannot be more than 10 years in the past");
		}
		
		if (endDate != null && endDate.isAfter(LocalDate.now().plusYears(10))) {
			throw new BadRequestException("End date cannot be more than 10 years in the future");
		}
	}
	
	private void validatePagination(int page, int size) {
		if (page < 0) {
			throw new BadRequestException("Page number cannot be negative");
		}
		
		if (size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
			throw new BadRequestException("Page size must be between " + MIN_PAGE_SIZE + " and " + MAX_PAGE_SIZE);
		}
	}
	
	private void validateSorting(String sortBy, String sortDirection) {
		if (sortBy == null || sortBy.trim().isEmpty()) {
			throw new BadRequestException("Sort field cannot be empty");
		}
		
		if (!VALID_SORT_FIELDS.contains(sortBy)) {
			throw new BadRequestException("Invalid sort field: " + sortBy +
				". Valid fields: " + String.join(", ", VALID_SORT_FIELDS));
		}
		
		if (sortDirection == null || sortDirection.trim().isEmpty()) {
			throw new BadRequestException("Sort direction cannot be empty");
		}
		
		if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
			throw new BadRequestException("Sort direction must be 'asc' or 'desc'");
		}
	}
	
	private void validateStatusTransition(ProjectStatus currentStatus, ProjectStatus newStatus, Project project) {
		if (currentStatus == ProjectStatus.CANCELLED && newStatus != ProjectStatus.ACTIVE) {
			throw new BusinessLogicException("Cancelled project can only be changed to ACTIVE status");
		}
		
		if (currentStatus == ProjectStatus.COMPLETED) {
			throw new BusinessLogicException("Cannot change status of completed project");
		}
		
		if (newStatus == ProjectStatus.COMPLETED && project.getShifts().isEmpty()) {
			throw new BusinessLogicException("Cannot complete project without any shifts");
		}
		
		if (newStatus == ProjectStatus.COMPLETED && currentStatus != ProjectStatus.ACTIVE) {
			throw new BusinessLogicException("Only ACTIVE projects can be marked as COMPLETED");
		}
	}
	
	private void validateDateChangeWithShifts(Project project, LocalDate newStartDate, LocalDate newEndDate) {
		if (project.getShifts().isEmpty()) {
			return;
		}
		
		boolean hasShiftsOutsideRange = project.getShifts().stream()
			.anyMatch(shift -> {
				LocalDate shiftDate = shift.getDate();
				boolean outsideStart = newStartDate != null && shiftDate.isBefore(newStartDate);
				boolean outsideEnd = newEndDate != null && shiftDate.isAfter(newEndDate);
				return outsideStart || outsideEnd;
			});
		
		if (hasShiftsOutsideRange) {
			throw new BusinessLogicException(
				"Cannot change project dates: some shifts fall outside the new date range. " +
					"Update or delete those shifts first."
			);
		}
	}
}
