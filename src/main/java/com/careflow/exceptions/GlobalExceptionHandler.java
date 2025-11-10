package com.careflow.exceptions;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;

import javax.management.relation.RoleNotFoundException;

import com.careflow.exceptions.auth.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        log.warn(ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.warn(ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> hendleDeniedAccess(AccessDeniedException ex) {
        log.info(ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<?> handleRoleNotFound(RoleNotFoundException ex) {
        log.error("role not found", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleNotValidMethodArgument(MethodArgumentNotValidException ex) {
        log.warn(ex.getMessage());
        HashMap<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, errors);

    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwtException(ExpiredJwtException ex){
        log.error("token is expired");
        return  buildResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
    }

@ExceptionHandler(PasswordMismatchException.class)
public ResponseEntity<?> handlePasswordMissmach(PasswordMismatchException ex){
        log.error("password missmach : {}",ex.getMessage());
        return  buildResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
}
    @ExceptionHandler(InvalidTokenException.class)
    public  ResponseEntity<?> handleInvalidToken(InvalidTokenException ex){
        log.error("token is invalid {}",ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
    }

    @ExceptionHandler(TooManyResetAttemptsException.class)
    public ResponseEntity<?> handleTooManyRestAttempts(TooManyResetAttemptsException ex){
        log.error("too many reset attempts :{}",ex.getMessage());
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS,ex.getMessage());
    }


    private ResponseEntity<ApiError> buildResponse(HttpStatus status, Object message) {
        ApiError error = new ApiError(status.value(), message, LocalDateTime.now());
        return new ResponseEntity<>(error, status);
    }

}
