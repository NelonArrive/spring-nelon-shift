package nelon.arrive.nelonshift.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShiftDto {
	private Long id;
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private Integer hours;
	private BigDecimal basePay;
	private Integer overtimeHours;
	private BigDecimal overtimePay;
	private BigDecimal perDiem;
	private BigDecimal compensation;
}