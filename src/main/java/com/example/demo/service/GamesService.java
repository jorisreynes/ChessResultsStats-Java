package com.example.demo.service;

import com.example.demo.model.Game;
import com.example.demo.repository.GamesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GamesService {

    @Autowired
    private GamesRepository gameRepository;


    public List<Game> getGames(){
        List<Game> games = new ArrayList<>();
        gameRepository.findAll().forEach(game -> {
            games.add(game);
        });
        return games;
    }

    public void addGame(Game game){

        gameRepository.save(game);
    }

}
