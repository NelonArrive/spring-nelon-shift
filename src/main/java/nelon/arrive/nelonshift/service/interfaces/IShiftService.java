package nelon.arrive.nelonshift.service.interfaces;

import nelon.arrive.nelonshift.entity.Shift;

public interface IShiftService {
	Shift getByProject(Long projectId);
	
	Shift create(Long projectId, Shift shift);
	
	Shift update(Long projectId, Shift shift);
	
	void delete(Long id);
}
