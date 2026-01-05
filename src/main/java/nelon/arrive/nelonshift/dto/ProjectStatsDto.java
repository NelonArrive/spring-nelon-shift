package nelon.arrive.nelonshift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStatsDto {
	
	private String period;
	private Integer daysWorked;
	private Integer shiftCount;
	private Integer totalHours;
	
	private BigDecimal totalEarnings;
	private BigDecimal totalBasePay;
	private BigDecimal totalOvertimePay;
	private BigDecimal totalPerDiem;
	
	private Integer basePayPercentage;
	private Integer overtimePayPercentage;
	private Integer perDiemPercentage;
	
	private BigDecimal hourlyRate;
	
	private Integer targetShiftCount;
	
	public static ProjectStatsDto empty(String period) {
		return ProjectStatsDto.builder()
			.period(period != null ? period : "—")
			.daysWorked(0)
			.shiftCount(0)
			.totalHours(0)
			.totalEarnings(BigDecimal.ZERO)
			.totalBasePay(BigDecimal.ZERO)
			.basePayPercentage(0)
			.totalOvertimePay(BigDecimal.ZERO)
			.overtimePayPercentage(0)
			.totalPerDiem(BigDecimal.ZERO)
			.perDiemPercentage(0)
			.hourlyRate(BigDecimal.ZERO)
			.targetShiftCount(null)
			.build();
	}
	
	/**
	 * Рассчитать производные значения для финансовой статистики:
	 * - Процент каждого типа оплаты (base, overtime, per diem)
	 * - Средняя ставка за час
	 */
	public void calculateDerivedValues() {
		calculatePaymentPercentages();
		calculateHourlyRate();
	}
	
	// Рассчитывает проценты для basePay, overtimePay и perDiem
	private void calculatePaymentPercentages() {
		if (totalEarnings != null && totalEarnings.compareTo(BigDecimal.ZERO) > 0) {
			basePayPercentage = calculatePercentage(totalBasePay, totalEarnings);
			overtimePayPercentage = calculatePercentage(totalOvertimePay, totalEarnings);
			perDiemPercentage = calculatePercentage(totalPerDiem, totalEarnings);
		} else {
			basePayPercentage = 0;
			overtimePayPercentage = 0;
			perDiemPercentage = 0;
		}
	}
	
	// Рассчитывает среднюю ставку за час
	private void calculateHourlyRate() {
		if (totalHours != null && totalHours > 0 && totalEarnings != null) {
			hourlyRate = totalEarnings
				.divide(BigDecimal.valueOf(totalHours), 2, RoundingMode.HALF_UP);
		} else {
			hourlyRate = BigDecimal.ZERO;
		}
	}
	
	// Вычисление процента
	private Integer calculatePercentage(BigDecimal part, BigDecimal total) {
		if (part == null || total == null || total.compareTo(BigDecimal.ZERO) == 0) {
			return 0;
		}
		return part.multiply(BigDecimal.valueOf(100))
			.divide(total, 0, RoundingMode.HALF_UP)
			.intValue();
	}
}
