package nelon.arrive.nelonshift.controller;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.ProjectDto;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Project.ProjectStatus;
import nelon.arrive.nelonshift.response.PageResponse;
import nelon.arrive.nelonshift.services.ProjectService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/${api.prefix}/projects")
@RequiredArgsConstructor
public class ProjectController {
	private final ProjectService projectService;
	
	@GetMapping
	public ResponseEntity<PageResponse<ProjectDto>> getProjects(
		@RequestParam(required = false) String name,
		@RequestParam(required = false) ProjectStatus status,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "id") String sortBy,
		@RequestParam(defaultValue = "asc") String sortDirection
	) {
		PageResponse<ProjectDto> response = projectService.getProjects(
			name, status, startDate, endDate, page, size, sortBy, sortDirection
		);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ProjectDto> getProjectById(
		@PathVariable Long id
	) {
		ProjectDto project = projectService.getProjectById(id);
		return ResponseEntity.ok(project);
	}
	
	@PostMapping
	public ResponseEntity<ProjectDto> createProject(
		@RequestBody Project project
	) {
		ProjectDto createdProject = projectService.createProject(project);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ProjectDto> updateProject(
		@PathVariable Long id,
		@RequestBody Project project
	) {
		ProjectDto updatedProject = projectService.updateProject(id, project);
		return ResponseEntity.ok(updatedProject);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProject(
		@PathVariable Long id
	){
		projectService.deleteProject(id);
		return ResponseEntity.noContent().build();
	}
}
