package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.dto.DailyEarningsDto;
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
import java.util.*;
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
			.collect(Collectors.toList());
		
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
		
		// Прошлый месяц для сравнения
		YearMonth previousMonth = currentMonth.minusMonths(1);
		LocalDate prevMonthStart = previousMonth.atDay(1);
		LocalDate prevMonthEnd = previousMonth.atEndOfMonth();
		
		BigDecimal previousMonthEarnings = allShifts.stream()
			.filter(s -> !s.getDate().isBefore(prevMonthStart) && !s.getDate().isAfter(prevMonthEnd))
			.map(Shift::getTotalPay)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		// Процент изменения
		int earningsChangePercentage = calculateChangePercentage(
			currentMonthEarnings,
			previousMonthEarnings
		);
		
		// ===== ТОП-3 ПРОЕКТОВ =====
		
		List<TopProjectDto> topProjects = allProjects.stream()
			.filter(p -> !p.getShifts().isEmpty())
			.map(this::mapToTopProject)
			.sorted((a, b) -> b.getTotalEarnings().compareTo(a.getTotalEarnings()))
			.limit(3)
			.collect(Collectors.toList());
		
		// ===== ГРАФИК ПО ДНЯМ (последние 7 дней) =====
		
		List<DailyEarningsDto> dailyEarnings = calculateDailyEarnings(allShifts);
		
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
			.earningsChangePercentage(earningsChangePercentage)
			.topProjects(topProjects)
			.dailyEarnings(dailyEarnings)
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
			.earningsChangePercentage(0)
			.topProjects(Collections.emptyList())
			.dailyEarnings(Collections.emptyList())
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
	
	/**
	 * Рассчитать заработок по дням (последние 7 дней)
	 */
	private List<DailyEarningsDto> calculateDailyEarnings(List<Shift> allShifts) {
		LocalDate today = LocalDate.now();
		LocalDate sevenDaysAgo = today.minusDays(6);
		
		// Группируем смены по датам
		Map<LocalDate, List<Shift>> shiftsByDate = allShifts.stream()
			.filter(s -> !s.getDate().isBefore(sevenDaysAgo) && !s.getDate().isAfter(today))
			.collect(Collectors.groupingBy(Shift::getDate));
		
		// Создаём запись для каждого дня (даже если смен нет)
		List<DailyEarningsDto> dailyEarnings = new ArrayList<>();
		
		for (int i = 6; i >= 0; i--) {
			LocalDate date = today.minusDays(i);
			List<Shift> shiftsForDay = shiftsByDate.getOrDefault(date, Collections.emptyList());
			
			BigDecimal earnings = shiftsForDay.stream()
				.map(Shift::getTotalPay)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			dailyEarnings.add(DailyEarningsDto.builder()
				.date(date.toString())
				.earnings(earnings)
				.shifts(shiftsForDay.size())
				.build());
		}
		
		return dailyEarnings;
	}
	
	/**
	 * Рассчитать процент изменения
	 */
	private int calculateChangePercentage(BigDecimal current, BigDecimal previous) {
		if (previous.compareTo(BigDecimal.ZERO) == 0) {
			return current.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
		}
		
		return current.subtract(previous)
			.multiply(BigDecimal.valueOf(100))
			.divide(previous, 0, RoundingMode.HALF_UP)
			.intValue();
	}
	
	
}
