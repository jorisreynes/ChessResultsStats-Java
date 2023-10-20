package com.example.demo.controller;

import com.example.demo.model.Game;
import com.example.demo.service.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins="*")
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