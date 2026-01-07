package nelon.arrive.nelonshift.controller;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.DashboardStatsDto;
import nelon.arrive.nelonshift.services.interfaces.IDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/dashboard")
@RequiredArgsConstructor
public class DashboardController {
	
	private final IDashboardService dashboardService;
	
	@GetMapping("/stats")
	public ResponseEntity<DashboardStatsDto> getDashboardStats() {
		DashboardStatsDto stats = dashboardService.getDashboardStats();
		return ResponseEntity.ok(stats);
	}
	
}
