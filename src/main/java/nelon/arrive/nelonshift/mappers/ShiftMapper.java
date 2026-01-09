package nelon.arrive.nelonshift.mappers;

import nelon.arrive.nelonshift.dto.ShiftDto;
import nelon.arrive.nelonshift.entity.Shift;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
	
	ShiftDto toDto(Shift shift);
	
	Shift toEntity(ShiftDto shiftDto);
	
	List<ShiftDto> toDtoList(List<Shift> shifts);
}
