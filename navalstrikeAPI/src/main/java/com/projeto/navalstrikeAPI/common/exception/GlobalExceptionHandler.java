package com.projeto.navalstrikeAPI.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MatchNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleMatchNotFound(MatchNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(GameAlreadyStartedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleGameAlreadyStarted(GameAlreadyStartedException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(PlayerTurnException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handlePlayerTurn(PlayerTurnException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ShipOverlapException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleShipOverlap(ShipOverlapException ex) {
        return ex.getMessage();
    }
    @ExceptionHandler(InvalidCoordinateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidCoordinate(InvalidCoordinateException ex) {
        return ex.getMessage();
    }
    @ExceptionHandler(ShipPlacementException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleShipPlacement(ShipPlacementException ex) {
        return ex.getMessage();
    }
    @ExceptionHandler(GameFinishedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleGameFinished(GameFinishedException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAll(Exception ex) {
        return "Erro inesperado: " + ex.getMessage();
    }
}