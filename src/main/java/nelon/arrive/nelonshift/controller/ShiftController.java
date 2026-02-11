package nelon.arrive.nelonshift.controller;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.ShiftDto;
import nelon.arrive.nelonshift.request.CreateShiftRequest;
import nelon.arrive.nelonshift.request.UpdateShiftRequest;
import nelon.arrive.nelonshift.response.MessageResponse;
import nelon.arrive.nelonshift.services.interfaces.IShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${api.prefix}/shifts")
@RequiredArgsConstructor
public class ShiftController {
	
	private final IShiftService shiftService;
	
	@GetMapping
	public ResponseEntity<List<ShiftDto>> getShifts(@RequestParam() Long projectId) {
		return ResponseEntity.ok(shiftService.getShiftsByProjectId(projectId));
	}
	
	@PostMapping
	public ResponseEntity<ShiftDto> createShift(
		@RequestBody CreateShiftRequest request
	) {
		return ResponseEntity.status(CREATED).body(shiftService.createShift(request));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ShiftDto> updateShift(
		@PathVariable Long id,
		@RequestBody UpdateShiftRequest request
	) {
		return ResponseEntity.ok(shiftService.updateShift(id, request));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<MessageResponse> deleteShift(@PathVariable Long id) {
		return ResponseEntity.ok(shiftService.deleteShift(id));
	}
}
