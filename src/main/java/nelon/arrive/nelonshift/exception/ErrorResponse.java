package nelon.arrive.nelonshift.exception;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ErrorResponse {
	private int status;
	private String message;
	private LocalDateTime timestamp;
	private Map<String, String> errors;
	
	public ErrorResponse(int status, String message, LocalDateTime timestamp) {
		this.status = status;
		this.message = message;
		this.timestamp = timestamp;
	}
	
	public ErrorResponse(int status, String message, LocalDateTime timestamp, Map<String, String> errors) {
		this.status = status;
		this.message = message;
		this.timestamp = timestamp;
		this.errors = errors;
	}
}