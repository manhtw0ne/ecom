package com.manh.ecom_be.exceptions;

import com.manh.ecom_be.responses.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseObject> handleGeneralException(Exception exception) {
        return ResponseEntity.internalServerError().body(
                ResponseObject.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message(exception.getMessage())
                        .build()
        );
    }
    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleResourceNotFoundException(DataNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseObject.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .message(exception.getMessage())
                        .build()
        );
    }

//    @ExceptionHandler(PermissionDenyException.class)
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    public ResponseEntity<ResponseObject> handlePermissionDeny(PermissionDenyException ex) {
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
//                ResponseObject.builder()
//                        .status(HttpStatus.FORBIDDEN)
//                        .message(ex.getMessage())
//                        .build()
//        );
//    }
//
//    @ExceptionHandler(InvalidParamException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<ResponseObject> handleInvalidParam(InvalidParamException ex) {
//        return ResponseEntity.badRequest().body(
//                ResponseObject.builder().status(HttpStatus.BAD_REQUEST).message(ex.getMessage()).build()
//        );
//    }
//
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ResponseEntity<ResponseObject> handleGeneral(Exception ex) {
//        return ResponseEntity.internalServerError().body(
//                ResponseObject.builder()
//                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .message(ex.getMessage())
//                        .build()
//        );
//    }
//
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ResponseEntity<ResponseObject> handleException(Exception ex) {
//        return ResponseEntity.internalServerError().body(
//                ResponseObject.builder()
//                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .message(ex.getMessage())
//                        .build()
//        );
//    }
//
//
//    @ExceptionHandler(DataNotFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ResponseEntity<ResponseObject> handleDataNotFound(DataNotFoundException e){
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                ResponseObject.builder()
//                        .status(HttpStatus.NOT_FOUND)
//                        .message(e.getMessage())
//                        .build()
//        );
//    }
//
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ResponseEntity<ResponseObject> handleGeneralException(Exception e){
//        return ResponseEntity.internalServerError().body(
//                ResponseObject.builder()
//                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .message(e.getMessage())
//                        .build()
//        );
//    }
//
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public ResponseEntity<ResponseObject> handleDataIntegrityViolation(
//            DataIntegrityViolationException e){
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(
//                ResponseObject.builder()
//                        .status(HttpStatus.CONFLICT)
//                        .message("Data integrity violation: " + e.getMessage())
//                        .build()
//        );
//    }
//
//    @ExceptionHandler(ExpiredTokenException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ResponseEntity<ResponseObject> handleExpiredToken(ExpiredTokenException e) {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                ResponseObject.builder()
//                        .status(HttpStatus.UNAUTHORIZED)
//                        .message(e.getMessage())
//                        .build()
//        );
//    }
//
//    @ExceptionHandler(ExpiredTokenException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ResponseEntity<ResponseObject> handleExpiredToken(ExpiredTokenException e) {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                .status(HttpStatus.UNAUTHORIZED)
//                .message(e.getMessage())
//                .build()
//        );
//    }
}
