package nelon.arrive.nelonshift.controllers;


import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.ShiftDTO;
import nelon.arrive.nelonshift.entities.Shift;
import nelon.arrive.nelonshift.services.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/shifts")
@RequiredArgsConstructor
public class ShiftController {
	private final ShiftService shiftService;
	
	@GetMapping
	public ResponseEntity<List<ShiftDTO>> getShifts(
		@RequestParam() Long projectId
	) {
		List<ShiftDTO> response = shiftService.getShiftsByProjectId(projectId);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping
	public ResponseEntity<ShiftDTO> createShift(
		@RequestParam Long projectId,
		@RequestBody Shift shift
	) {
		ShiftDTO updatedShift = shiftService.createShift(projectId, shift);
		return ResponseEntity.status(HttpStatus.CREATED).body(updatedShift);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ShiftDTO> updateShift(
		@PathVariable Long id,
		@RequestBody Shift shift
	) {
		ShiftDTO updatedShift = shiftService.updateShift(id, shift);
		return ResponseEntity.ok(updatedShift);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
		shiftService.deleteShift(id);
		return ResponseEntity.noContent().build();
	}
}
