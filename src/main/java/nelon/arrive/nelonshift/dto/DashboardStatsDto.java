package nelon.arrive.nelonshift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
	
	private Integer totalActiveProjects;
	private Integer totalCompletedProjects;
	private Integer totalShifts;
	private Integer totalHours;
	private BigDecimal totalEarnings;
	
	private BigDecimal currentMonthEarnings;
	private Integer currentMonthShifts;
	private Integer currentMonthHours;
	
	private List<TopProjectDto> topProjects;
}
