package com.euphony.streaming.exception.custom.report;

import com.euphony.streaming.exception.HttpStatusProvider;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ReportGenerationException extends RuntimeException implements HttpStatusProvider {

  private final HttpStatus httpStatus;

  // Constructor con mensaje y código de estado HTTP
  public ReportGenerationException(String message, HttpStatus httpStatus) {
    super(message);
    this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;  // Por defecto 500
  }

  // Constructor para un caso de falta de campos (por defecto 404)
  public static ReportGenerationException missingFields(String message) {
    return new ReportGenerationException(message, HttpStatus.NOT_FOUND);
  }

  // Constructor por defecto con mensaje genérico (opcional)
  public static ReportGenerationException genericError(String message) {
    return new ReportGenerationException(message, HttpStatus.BAD_REQUEST);
  }

  public ReportGenerationException(String message) {
    this(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // Constructor con mensaje y causa (por defecto 500)
  public ReportGenerationException(String message, Throwable cause) {
    super(message, cause);
    this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return this.httpStatus;
  }
}
