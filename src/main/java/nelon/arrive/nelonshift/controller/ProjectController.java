package nelon.arrive.nelonshift.controller;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.ProjectDto;
import nelon.arrive.nelonshift.dto.ProjectStatsDto;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.enums.ProjectStatus;
import nelon.arrive.nelonshift.repository.ProjectRepository;
import nelon.arrive.nelonshift.request.CreateProjectRequest;
import nelon.arrive.nelonshift.request.UpdateProjectRequest;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.response.PageResponse;
import nelon.arrive.nelonshift.services.ProjectExcelService;
import nelon.arrive.nelonshift.services.interfaces.IProjectService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${api.prefix}/projects")
@RequiredArgsConstructor
public class ProjectController {
	
	private final IProjectService projectService;
	private final ProjectExcelService excelExportService;
	private final ProjectRepository projectRepository;


	@GetMapping
	public ResponseEntity<PageResponse<ProjectDto>> getProjects(
		@RequestParam(required = false) String name,
		@RequestParam(required = false) ProjectStatus status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "date") String sortBy,
		@RequestParam(defaultValue = "desc") String sortDirection
	) {
		PageResponse<ProjectDto> projectDtos = projectService.getProjects(
			name, status, page, size, sortBy, sortDirection
		);
		return ResponseEntity.ok(projectDtos);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
		return ResponseEntity.ok(projectService.getProjectById(id));
	}
	
	@PostMapping
	public ResponseEntity<ProjectDto> createProject(@RequestBody CreateProjectRequest request) {
		return ResponseEntity.status(CREATED).body(projectService.createProject(request));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ProjectDto> updateProject(
		@PathVariable Long id,
		@RequestBody UpdateProjectRequest request
	) {
		return ResponseEntity.ok(projectService.updateProject(id, request));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<MessageResponse> deleteProject(@PathVariable Long id){
		return ResponseEntity.ok(projectService.deleteProject(id));
	}
	
	@GetMapping("/{id}/stats")
	public ResponseEntity<ProjectStatsDto> getProjectStats(@PathVariable Long id) {
		ProjectStatsDto stats = projectService.getProjectStats(id);
		return ResponseEntity.ok(stats);
	}

	@GetMapping("/{id}/export/excel")
	public ResponseEntity<byte[]> exportProjectToExcel(@PathVariable Long id) {
		try {
			Project project = projectRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Проект не найден с ID: " + id));

			byte[] excelData = excelExportService.exportProjectToExcel(project);

			String fileName = "Проект_" + project.getName().replaceAll("[^a-zA-Zа-яА-Я0-9]", "_") + ".xlsx";
			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
					.replaceAll("\\+", "%20");

			// Настраиваем заголовки
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

			return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}
}
