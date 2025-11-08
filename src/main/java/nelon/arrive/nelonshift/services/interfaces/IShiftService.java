package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.dto.ShiftDTO;
import nelon.arrive.nelonshift.entities.Shift;

import java.util.List;

public interface IShiftService {
	public List<ShiftDTO> getShiftsByProjectId(Long projectId);
	
	ShiftDTO createShift(Long projectId, Shift shift);
	
	ShiftDTO updateShift(Long id, Shift shiftDetails);
	
	void deleteShift(Long id);
}
