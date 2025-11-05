package nelon.arrive.nelonshift.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nelon.arrive.nelonshift.entity.Shift;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftDTO {
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
	
	public ShiftDTO(Shift shift) {
		this.id = shift.getId();
		this.projectId = shift.getProject().getId();
		this.projectName = shift.getProject().getName();
		this.date = shift.getDate();
		this.startTime = shift.getStartTime();
		this.endTime = shift.getEndTime();
		this.hours = shift.getHours();
		this.basePay = shift.getBasePay();
		this.overtimeHours = shift.getOvertimeHours();
		this.overtimePay = shift.getOvertimePay();
		this.perDiem = shift.getPerDiem();
		this.totalPay = shift.getTotalPay();
		this.createdAt = shift.getCreatedAt();
		this.updatedAt = shift.getUpdatedAt();
	}
}
