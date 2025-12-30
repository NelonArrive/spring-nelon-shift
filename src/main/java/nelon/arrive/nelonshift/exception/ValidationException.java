package nelon.arrive.nelonshift.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ValidationException extends ApiException {
	public ValidationException(String message) {
		super(HttpStatus.UNPROCESSABLE_ENTITY, message);
	}
	
	public Map<String, String> getErrors() {
		return Map.of();
	}
}