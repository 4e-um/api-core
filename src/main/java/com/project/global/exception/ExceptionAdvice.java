package com.project.global.exception;

import com.project.global.exception.code.domain.BaseErrorCode;
import com.project.global.exception.code.domain.GlobalErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<Object> handleBaseException(BaseException e, HttpServletRequest request) {
    BaseErrorCode code = e.getCode();
    log.error("[BaseException] {} - {}", code.name(), code.getMessage());

    ProblemDetail problem = ProblemDetail.forStatus(code.getHttpStatus());
    problem.setTitle(code.name());
    problem.setDetail(code.getMessage());
    problem.setProperty("code", code.getCustomCode());

    return ResponseEntity.status(code.getHttpStatus()).body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleUnhandledException(Exception e, WebRequest request) {
    log.error("[Exception] Unhandled", e);

    GlobalErrorCode code = GlobalErrorCode.INTERNAL_SERVER_ERROR;

    ProblemDetail problem = ProblemDetail.forStatus(code.getHttpStatus());
    problem.setTitle(code.name());
    problem.setDetail(code.getMessage());
    problem.setProperty("code", code.getCustomCode());

    return ResponseEntity.status(code.getHttpStatus()).body(problem);
  }
}
