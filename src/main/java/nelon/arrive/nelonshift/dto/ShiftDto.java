package nelon.arrive.nelonshift.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftDto {
	private Long id;
	private Long projectId;
	private String projectName;
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private Integer hours;
	private BigDecimal basePay;
	private Integer overtimeHours;
	private BigDecimal overtimePay;
	private BigDecimal perDiem;
	private BigDecimal totalPay;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
