package nelon.arrive.nelonshift.service;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.exception.ResourceNotFoundException;
import nelon.arrive.nelonshift.repository.ProjectRepository;
import nelon.arrive.nelonshift.repository.ShiftRepository;
import nelon.arrive.nelonshift.service.interfaces.IShiftService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftService implements IShiftService {
	private final ShiftRepository shiftRepository;
	private final ProjectRepository projectRepository;
	
	@Override
	public Shift getByProject(Long projectId) {
		return shiftRepository.findByProjectId(projectId);
	}
	
	@Override
	public Shift create(Long projectId, Shift shift) {
		var project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		
		shift.setProject(project);
		return shiftRepository.save(shift);
	}
	
	@Override
	public Shift update(Long projectId, Shift shift) {
		return null;
	}
	
	@Override
	public void delete(Long id) {
		shiftRepository.findById(id).ifPresentOrElse(shiftRepository::delete,
			() -> {
				throw new ResourceNotFoundException("Shift not found!");
			});
	}
}
