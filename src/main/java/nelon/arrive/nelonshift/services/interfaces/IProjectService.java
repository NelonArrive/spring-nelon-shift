package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.dto.PageResponse;
import nelon.arrive.nelonshift.dto.ProjectDTO;
import nelon.arrive.nelonshift.entities.Project;
import nelon.arrive.nelonshift.entities.Project.ProjectStatus;

import java.time.LocalDate;

public interface IProjectService {
	PageResponse<ProjectDTO> getProjects(
		String name,
		ProjectStatus status,
		LocalDate startDate,
		LocalDate endDate,
		int page,
		int size,
		String sortBy,
		String sortDirection
	);
	
	ProjectDTO getProjectById(Long id);
	
	ProjectDTO createProject(Project project);
	
	ProjectDTO updateProject(Long id, Project projectDetails);
	
	void deleteProject(Long id);
}
