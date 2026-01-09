package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.dto.DashboardStatsDto;
import nelon.arrive.nelonshift.dto.TopProjectDto;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.enums.ProjectStatus;
import nelon.arrive.nelonshift.services.interfaces.IDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService implements IDashboardService {
	
	private final AuthService authService;
	
	@Override
	@Transactional(readOnly = true)
	public DashboardStatsDto getDashboardStats() {
		User currentUser = authService.getCurrentUser();
		
		List<Project> allProjects = currentUser.getProjects();
		
		if (allProjects.isEmpty()) {
			return createEmptyStats();
		}
		
		int totalActiveProjects = (int) allProjects.stream()
			.filter(p -> p.getStatus() == ProjectStatus.ACTIVE)
			.count();
		
		int totalCompletedProjects = (int) allProjects.stream()
			.filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
			.count();
		
		List<Shift> allShifts = allProjects.stream()
			.flatMap(p -> p.getShifts().stream())
			.toList();
		
		int totalShifts = allShifts.size();
		
		int totalHours = allShifts.stream()
			.map(Shift::getHours)
			.filter(Objects::nonNull)
			.reduce(0, Integer::sum);
		
		BigDecimal totalEarnings = allShifts.stream()
			.map(Shift::getTotalPay)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		// ===== СТАТИСТИКА ЗА ТЕКУЩИЙ МЕСЯЦ =====
		
		YearMonth currentMonth = YearMonth.now();
		LocalDate monthStart = currentMonth.atDay(1);
		LocalDate monthEnd = currentMonth.atEndOfMonth();
		
		List<Shift> currentMonthShifts = allShifts.stream()
			.filter(s -> !s.getDate().isBefore(monthStart) && !s.getDate().isAfter(monthEnd))
			.toList();
		
		BigDecimal currentMonthEarnings = currentMonthShifts.stream()
			.map(Shift::getTotalPay)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		int currentMonthShiftCount = currentMonthShifts.size();
		
		int currentMonthHoursCount = currentMonthShifts.stream()
			.map(Shift::getHours)
			.filter(Objects::nonNull)
			.reduce(0, Integer::sum);
		
		// ===== ТОП-3 ПРОЕКТОВ =====
		
		List<TopProjectDto> topProjects = allProjects.stream()
			.filter(p -> !p.getShifts().isEmpty())
			.map(this::mapToTopProject)
			.sorted((a, b) -> b.getTotalEarnings().compareTo(a.getTotalEarnings()))
			.limit(3)
			.collect(Collectors.toList());
		
		// ===== СОБИРАЕМ DTO =====
		
		DashboardStatsDto stats = DashboardStatsDto.builder()
			.totalActiveProjects(totalActiveProjects)
			.totalCompletedProjects(totalCompletedProjects)
			.totalShifts(totalShifts)
			.totalHours(totalHours)
			.totalEarnings(totalEarnings)
			.currentMonthEarnings(currentMonthEarnings)
			.currentMonthShifts(currentMonthShiftCount)
			.currentMonthHours(currentMonthHoursCount)
			.topProjects(topProjects)
			.build();
		
		log.info("Dashboard stats calculated: {} projects, {} shifts, {} total",
			allProjects.size(), totalShifts, totalEarnings);
		
		return stats;
	}
	
	/**
	 * Создать пустую статистику (когда нет проектов)
	 */
	private DashboardStatsDto createEmptyStats() {
		return DashboardStatsDto.builder()
			.totalActiveProjects(0)
			.totalCompletedProjects(0)
			.totalShifts(0)
			.totalHours(0)
			.totalEarnings(BigDecimal.ZERO)
			.currentMonthEarnings(BigDecimal.ZERO)
			.currentMonthShifts(0)
			.currentMonthHours(0)
			.topProjects(Collections.emptyList())
			.build();
	}
	
	/**
	 * Преобразовать проект в TopProjectDto
	 */
	private TopProjectDto mapToTopProject(Project project) {
		List<Shift> shifts = project.getShifts();
		
		BigDecimal totalEarnings = shifts.stream()
			.map(Shift::getTotalPay)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		int totalHours = shifts.stream()
			.map(Shift::getHours)
			.filter(Objects::nonNull)
			.reduce(0, Integer::sum);
		
		BigDecimal hourlyRate = totalHours > 0
			? totalEarnings.divide(BigDecimal.valueOf(totalHours), 2, RoundingMode.HALF_UP)
			: BigDecimal.ZERO;
		
		return TopProjectDto.builder()
			.id(project.getId())
			.name(project.getName())
			.totalEarnings(totalEarnings)
			.shiftCount(shifts.size())
			.hourlyRate(hourlyRate)
			.build();
	}
	
}
