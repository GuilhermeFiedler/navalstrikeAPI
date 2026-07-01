package com.projeto.navalstrikeAPI.domain.board.controller;

import com.projeto.navalstrikeAPI.domain.board.service.BoardService;
import com.projeto.navalstrikeAPI.domain.ship.dto.PlaceShipRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor

public class BoardController {

    private final BoardService service;

    @PostMapping("/{id}/ships")
    public void placeShip(@PathVariable UUID id, @RequestBody PlaceShipRequest request){
        service.placeShip(id, request);
    }
}
