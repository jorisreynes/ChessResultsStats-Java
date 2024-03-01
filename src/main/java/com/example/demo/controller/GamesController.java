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
    public List<Game> getGames(@RequestParam String username){

        // 1. We check the date of the last game in the database
        String lastGameMonth = gamesService.getLastGameMonth(username);

        // 2. We call the API for 12 months before now
        gamesService.getGamesFromChessCom(username, lastGameMonth, 3);

        return gamesService.getGames(username);
    }

    @RequestMapping(method = RequestMethod.POST, value="/games")
    public void addGame(@RequestBody Game game)
    {
        gamesService.saveGameInDatabase(game);
    }
}