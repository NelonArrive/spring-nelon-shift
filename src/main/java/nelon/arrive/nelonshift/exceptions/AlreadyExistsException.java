package nelon.arrive.nelonshift.exceptions;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends ApiException {
	public AlreadyExistsException(String message) {
		super(HttpStatus.CONFLICT, message);
	}
}