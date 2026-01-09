package nelon.arrive.nelonshift.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateShiftRequest {
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	
	@Min(value = 0, message = "Hours must be at least 0")
	@Max(value = 24, message = "Hours cannot exceed 24")
	private Integer hours;
	
	@DecimalMin(value = "0.0", message = "Base pay must be positive")
	private BigDecimal basePay;
	
	@Min(value = 0, message = "Overtime hours must be at least 0")
	private Integer overtimeHours;
	
	@DecimalMin(value = "0.0", message = "Overtime pay must be positive")
	private BigDecimal overtimePay;
	
	@DecimalMin(value = "0.0", message = "Per diem must be positive")
	private BigDecimal perDiem;
}
