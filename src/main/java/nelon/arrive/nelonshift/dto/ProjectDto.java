package nelon.arrive.nelonshift.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nelon.arrive.nelonshift.entity.Project;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
	private Long id;
	private String name;
	private String status;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Integer shiftsCount;
	
	public ProjectDto(Project project) {
		this.id = project.getId();
		this.name = project.getName();
		this.status = project.getStatus().name();
		this.startDate = project.getStartDate();
		this.endDate = project.getEndDate();
		this.createdAt = project.getCreatedAt();
		this.updatedAt = project.getUpdatedAt();
		this.shiftsCount = project.getShifts() != null ? project.getShifts().size() : 0;
	}
}
