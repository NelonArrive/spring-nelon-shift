package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.dto.ProjectDto;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Project.ProjectStatus;
import nelon.arrive.nelonshift.response.PageResponse;

import java.time.LocalDate;

public interface IProjectService {
	PageResponse<ProjectDto> getProjects(
		String name,
		ProjectStatus status,
		LocalDate startDate,
		LocalDate endDate,
		int page,
		int size,
		String sortBy,
		String sortDirection
	);
	
	ProjectDto getProjectById(Long id);
	
	ProjectDto createProject(Project project);
	
	ProjectDto updateProject(Long id, Project projectDetails);
	
	void deleteProject(Long id);
}
