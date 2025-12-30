package nelon.arrive.nelonshift.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	/**
	 * Обработка наших кастомных исключений (ResourceNotFoundException, AlreadyExistsException и т.д.)
	 */
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(
		ApiException ex,
		HttpServletRequest request
	) {
		log.error("API Exception: {}", ex.getMessage(), ex);
		
		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(ex.getStatus().value())
			.error(ex.getStatus().getReasonPhrase())
			.message(ex.getMessage())
			.path(request.getRequestURI())
			.build();
		
		return ResponseEntity.status(ex.getStatus()).body(errorResponse);
	}
	
	/**
	 * Обработка ValidationException с несколькими ошибками
	 */
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
		ValidationException ex,
		HttpServletRequest request
	) {
		log.error("Validation Exception: {}", ex.getMessage());
		
		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(ex.getStatus().value())
			.error(ex.getStatus().getReasonPhrase())
			.message(ex.getMessage())
			.path(request.getRequestURI())
			.errors(ex.getErrors())
			.build();
		
		return ResponseEntity.status(ex.getStatus()).body(errorResponse);
	}
	
	/**
	 * Обработка ошибок валидации Spring (@Valid в контроллерах)
	 * Автоматически срабатывает когда @NotNull, @Size и т.д. не проходят
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
		MethodArgumentNotValidException ex,
		HttpServletRequest request
	) {
		log.error("Method Argument Not Valid: {}", ex.getMessage());
		
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		
		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.BAD_REQUEST.value())
			.error("Validation Failed")
			.message("Invalid request parameters")
			.path(request.getRequestURI())
			.errors(errors)
			.build();
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}
	
	/**
	 * Обработка всех остальных непредвиденных исключений
	 * Например: NullPointerException, IllegalArgumentException и т.д.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(
		Exception ex,
		HttpServletRequest request
	) {
		log.error("Unexpected Exception: {}", ex.getMessage(), ex);
		
		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
			.error("Internal Server Error")
			.message("An unexpected error occurred")
			.path(request.getRequestURI())
			.build();
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}