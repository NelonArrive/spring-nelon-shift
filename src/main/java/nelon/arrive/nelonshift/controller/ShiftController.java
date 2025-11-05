package nelon.arrive.nelonshift.controller;


import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.service.ShiftService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/projects/{projectId}/shifts")
@RequiredArgsConstructor
public class ShiftController {
	private final ShiftService shiftService;
	
	
}
