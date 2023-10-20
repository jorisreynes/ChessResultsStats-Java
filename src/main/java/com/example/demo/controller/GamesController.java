package com.example.demo.controller;

import com.example.demo.model.Game;
import com.example.demo.service.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GamesController {

    @Autowired
    private GamesService gamesService;

    @RequestMapping("/games")
    public List<Game> getGames(){

        return gamesService.getGames();
    }

    @RequestMapping(method = RequestMethod.POST, value="/games")
    public void addGame(@RequestBody Game game){
        gamesService.addGame(game);
    }


}