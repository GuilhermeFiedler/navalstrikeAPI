package com.projeto.navalstrikeAPI.domain.board.controller;

import com.projeto.navalstrikeAPI.domain.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor

public class BoardController {

    private final BoardService service;

    @PostMapping("/{id}/ships")
    public void placeShip(){}
}
