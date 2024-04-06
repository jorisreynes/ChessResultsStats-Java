package com.example.demo.controller;

import com.example.demo.model.Game;
import com.example.demo.service.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins="*")
@RestController
public class GamesController {

    @Value("${test}")
    private String testValue;

    @Autowired
    private GamesService gamesService;

    @RequestMapping("/games")
    public List<Game> getGames(@RequestParam String username){

        // 1. We check the date of the last game in the database
        String lastGameMonth = gamesService.getLastGameMonth(username);

        // 2. We get the data from the chess.com API
        List<String> dataList = gamesService.getGamesFromChessCom(username, lastGameMonth, 3);

        // 3. We create a list of games with the data
        List<Game> currentGamesList = gamesService.createFormattedGamesList(dataList, username);

        // 4. We save the list in database
        gamesService.saveGameInDatabase(currentGamesList);

        // 5. We return all games for this user to the front
        return gamesService.getGames(username);
    }

    @RequestMapping(method = RequestMethod.POST, value="/games")
    public void addGame(@RequestBody List<Game> gameList)
    {
        gamesService.saveGameInDatabase(gameList);
    }

    @RequestMapping("/test")
    public String getTestValue(){
        return "La valeur de la variable 'test' est : " + testValue;
    }
}