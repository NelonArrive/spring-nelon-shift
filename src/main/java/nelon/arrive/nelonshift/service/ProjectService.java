package nelon.arrive.nelonshift.service;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Project.ProjectStatus;
import nelon.arrive.nelonshift.repository.ProjectRepository;
import nelon.arrive.nelonshift.service.interfaces.IProjectService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {
	private final ProjectRepository projectRepository;
	
	public List<Project> getProjects(
		String name,
		ProjectStatus status,
		LocalDate startDate,
		LocalDate endDate,
		int page,
		int size,
		String sortBy,
		String sortDirection
	) {
		return List.of();
	}
	
	public Project getProjectById(Long id) {
		return null;
	}
	
	public Project createProject(Project project) {
		return null;
	}
	
	public Project updateProject(Long id, Project projectDetails) {
		return null;
	}
	
	public void deleteProject(Long id) {
	
	}
}
