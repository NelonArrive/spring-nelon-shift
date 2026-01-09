package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.dto.ProjectDto;
import nelon.arrive.nelonshift.dto.ProjectStatsDto;
import nelon.arrive.nelonshift.enums.ProjectStatus;
import nelon.arrive.nelonshift.request.CreateProjectRequest;
import nelon.arrive.nelonshift.request.UpdateProjectRequest;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.response.PageResponse;

import java.time.LocalDate;

public interface IProjectService {
	PageResponse<ProjectDto> getProjects(
		String name,
		ProjectStatus status,
		int page,
		int size,
		String sortBy,
		String sortDirection
	);
	
	ProjectDto getProjectById(Long id);
	
	ProjectDto createProject(CreateProjectRequest project);
	
	ProjectDto updateProject(Long id, UpdateProjectRequest projectDetails);
	
	MessageResponse deleteProject(Long id);
	
	ProjectStatsDto getProjectStats(Long id);
}
