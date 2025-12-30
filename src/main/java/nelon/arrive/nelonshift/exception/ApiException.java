package nelon.arrive.nelonshift.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {
	
	private final HttpStatus status;
	private final String message;
	
	protected ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
		this.message = message;
	}
	
	protected ApiException(HttpStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
		this.message = message;
	}
}