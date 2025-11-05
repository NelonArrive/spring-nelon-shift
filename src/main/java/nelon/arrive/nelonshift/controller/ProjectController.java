package nelon.arrive.nelonshift.controller;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/projects")
@RequiredArgsConstructor
public class ProjectController {
	private final ProjectService projectService;
	
	@PostMapping
	public Project create(@RequestBody Project project) {
		return projectService.create(project);
	}
	
	@GetMapping
	public ResponseEntity<PageResponse<ProjectDTO>> getAll(
		@RequestParam(required = false) String name,
		@RequestParam(required = false) ProjectStatus status,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "id") String sortBy,
		@RequestParam(defaultValue = "asc") String sortDirection
	) {
		PageResponse<ProjectDTO> response = projectService.getAll(
			name, status, startDate, endDate, page, size, sortBy, sortDirection
		);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{id}")
	public Project getById(@PathVariable Long id) {
		return projectService.getById(id);
	}
	
}
