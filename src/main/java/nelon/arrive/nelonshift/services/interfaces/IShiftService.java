package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.dto.ShiftDto;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.request.CreateShiftRequest;
import nelon.arrive.nelonshift.request.UpdateShiftRequest;
import nelon.arrive.nelonshift.response.MessageResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IShiftService {
	List<ShiftDto> getShiftsByProjectId(Long projectId);
	
	ShiftDto createShift(Long projectId, CreateShiftRequest shift);
	
	ShiftDto updateShift(Long id, UpdateShiftRequest shiftDetails);
	
	MessageResponse deleteShift(Long id);
	
	void validateShiftCreate(CreateShiftRequest request);
	
	void validateShiftUpdate(UpdateShiftRequest request);
	
	void validateShiftDateAgainstProject(LocalDate shiftDate, Project project);
	
	long calculateHoursBetween(LocalTime start, LocalTime end);
}
