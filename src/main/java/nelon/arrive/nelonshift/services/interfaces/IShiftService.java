package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.dto.ShiftDto;
import nelon.arrive.nelonshift.entity.Shift;

import java.util.List;

public interface IShiftService {
	public List<ShiftDto> getShiftsByProjectId(Long projectId);
	
	ShiftDto createShift(Long projectId, Shift shift);
	
	ShiftDto updateShift(Long id, Shift shiftDetails);
	
	void deleteShift(Long id);
}
