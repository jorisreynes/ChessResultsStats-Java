package com.example.demo.controller;

import com.example.demo.model.Game;
import com.example.demo.service.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins="*")
@RestController
public class GamesController {

    @Autowired
    private GamesService gamesService;

    @RequestMapping("/games")
    public List<Game> getGames(@RequestParam String username){

        // 1. We check the date of the last game in the database
        LocalDateTime lastGameDateAndTime = gamesService.getLastGameDateAndTime(username);

        // 2. We get the data from the chess.com API, each string in the list is a month of data returned by the API
        List<String> dataList = gamesService.getGamesFromChessCom(username, lastGameDateAndTime, 3);

        // 3. We create a list of recent games with the data
        List<Game> currentGamesList = gamesService.createFormattedGamesList(dataList, username, lastGameDateAndTime);

        // 4. We save the list in database
        gamesService.saveGameInDatabase(currentGamesList);

        // 5. We return all games for this user to the frontend
        return gamesService.getGames(username);
    }

    @RequestMapping(method = RequestMethod.POST, value="/games")
    public void addGame(@RequestBody List<Game> gameList)
    {
        gamesService.saveGameInDatabase(gameList);
    }
}