package nelon.arrive.nelonshift.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectDto {
	private Long id;
	private String name;
	private String status;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
