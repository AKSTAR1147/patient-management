package ink.starblog.patientservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationExceptions(MethodArgumentNotValidException ex){

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }


    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String,String>> patientNotFountExceptionHandler(PatientNotFoundException ex){
        log.warn("The Patinet does not exists {}", ex.getMessage());
        Map<String,String> errors = new HashMap<>();
        errors.put("Message","Patinet with provided Id does not found");
        return ResponseEntity.badRequest().body(errors);
    }
}
