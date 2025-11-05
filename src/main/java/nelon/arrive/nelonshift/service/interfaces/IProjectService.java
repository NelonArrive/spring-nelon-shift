package nelon.arrive.nelonshift.service.interfaces;

import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Project.ProjectStatus;

import java.time.LocalDate;
import java.util.List;

public interface IProjectService {
	List<Project> getProjects(
		String name,
		ProjectStatus status,
		LocalDate startDate,
		LocalDate endDate,
		int page,
		int size,
		String sortBy,
		String sortDirection
	);
	
	Project getProjectById(Long id);
	
	Project createProject(Project project);
	
	Project updateProject(Long id, Project projectDetails);
	
	void deleteProject(Long id);
}
