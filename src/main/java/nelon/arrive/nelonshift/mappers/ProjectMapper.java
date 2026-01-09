package nelon.arrive.nelonshift.mappers;

import nelon.arrive.nelonshift.dto.ProjectDto;
import nelon.arrive.nelonshift.entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
	
	ProjectDto toDto(Project project);
	
	Project toEntity(ProjectDto projectDto);
	
}
