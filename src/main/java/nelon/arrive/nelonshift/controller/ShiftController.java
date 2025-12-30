package nelon.arrive.nelonshift.controller;


import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.ShiftDto;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.services.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/${api.prefix}/shifts")
@RequiredArgsConstructor
public class ShiftController {
	private final ShiftService shiftService;
	
	@GetMapping
	public ResponseEntity<List<ShiftDto>> getShifts(
		@RequestParam() Long projectId
	) {
		List<ShiftDto> response = shiftService.getShiftsByProjectId(projectId);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping
	public ResponseEntity<ShiftDto> createShift(
		@RequestParam Long projectId,
		@RequestBody Shift shift
	) {
		ShiftDto updatedShift = shiftService.createShift(projectId, shift);
		return ResponseEntity.status(HttpStatus.CREATED).body(updatedShift);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ShiftDto> updateShift(
		@PathVariable Long id,
		@RequestBody Shift shift
	) {
		ShiftDto updatedShift = shiftService.updateShift(id, shift);
		return ResponseEntity.ok(updatedShift);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
		shiftService.deleteShift(id);
		return ResponseEntity.noContent().build();
	}
}
