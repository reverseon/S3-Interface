package rev.util.s3interfacesb.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;
@RestController
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, String>> error(NoHandlerFoundException e) {
        Map<String, String> resp = Map.of(
                "message", "Services not found"
        );
        return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> error(HttpRequestMethodNotSupportedException e) {
        Map<String, String> resp = Map.of(
                "message", "Resource not found"
        );
        return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> error(Exception e) {
        Map<String, String> resp = Map.of(
                "message", "Something went wrong"
        );
        return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @RequestMapping(value = "/error", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> error(@RequestBody ErrorResponse errorResponse) {
        Map<String, String> resp = Map.of(
                "message", "Something went wrong"
        );
        return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
