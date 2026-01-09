package nelon.arrive.nelonshift.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shifts")
public class Shift {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;
	
	@Column(nullable = false)
	private LocalDate date;
	
	@Column(name = "start_time")
	private LocalTime startTime;
	
	@Column(name = "end_time")
	private LocalTime endTime;
	
	@Column(nullable = false)
	@Min(value = 0, message = "Hours must be at least 0")
	@Max(value = 24, message = "Hours cannot exceed 24")
	private Integer hours;
	
	@Column(name = "base_pay", precision = 10, scale = 2)
	@DecimalMin(value = "0.0", inclusive = true, message = "Base pay must be positive")
	private BigDecimal basePay;
	
	@Column(name = "overtime_hours")
	@Min(value = 0, message = "Overtime hours must be at least 0")
	private Integer overtimeHours;
	
	@Column(name = "overtime_pay", precision = 10, scale = 2)
	@DecimalMin(value = "0.0", inclusive = true, message = "Overtime pay must be positive")
	private BigDecimal overtimePay;
	
	@Column(name = "per_diem", precision = 10, scale = 2)
	@DecimalMin(value = "0.0", inclusive = true, message = "Per diem must be positive")
	private BigDecimal perDiem;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	
	public BigDecimal getTotalPay() {
		BigDecimal total = BigDecimal.ZERO;
		if (basePay != null) total = total.add(basePay);
		if (overtimePay != null) total = total.add(overtimePay);
		if (perDiem != null) total = total.add(perDiem);
		return total;
	}
}
