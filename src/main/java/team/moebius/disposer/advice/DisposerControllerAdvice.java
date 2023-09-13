package team.moebius.disposer.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.moebius.disposer.dto.ErrorMessage;
import team.moebius.disposer.exception.NotFoundTokenException;
import team.moebius.disposer.exception.RecipientException;
import team.moebius.disposer.exception.TokenException;

@RestControllerAdvice
public class DisposerControllerAdvice {

    @ExceptionHandler({RecipientException.class, TokenException.class})
    public ResponseEntity<ErrorMessage> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler({NotFoundTokenException.class})
    public ResponseEntity<ErrorMessage> handleNotFoundTokenException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }

}
