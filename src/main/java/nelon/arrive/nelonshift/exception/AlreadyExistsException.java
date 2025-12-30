package nelon.arrive.nelonshift.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends ApiException {
	public AlreadyExistsException(String message) {
		super(HttpStatus.CONFLICT, message);
	}
}