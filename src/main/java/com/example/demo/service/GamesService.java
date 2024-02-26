package com.example.demo.service;

import com.example.demo.model.Game;
import com.example.demo.repository.GamesRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import org.springframework.web.client.RestTemplate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class GamesService {

    @Autowired
    private GamesRepository gameRepository;

    public String getLastGameMonth() {
        List<Game> games = new ArrayList<>();

        //gameRepository.findAll().forEach(game -> games.add(game));
        gameRepository.findAll().forEach(games::add);

        if (games.isEmpty()) {
            return "Aucune partie trouvée";
        }
        DateTimeFormatter sourceFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("yyyy/MM");

        Game lastGame = games.stream()
                .max(Comparator.comparing(game -> LocalDate.parse(game.getDate(), sourceFormatter)))
                .orElse(null);

        if (lastGame == null) {
            return "Aucune partie trouvée";
        }
        LocalDate lastGameDate = LocalDate.parse(lastGame.getDate(), sourceFormatter);
        return lastGameDate.format(targetFormatter);
    }

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



    public void getGamesFromChessCom(String username, String lastGameMonth) {

        YearMonth startMonth;

        // Si quelque chose en BDD, on définit la date de début au mois en cours
        if (!lastGameMonth.equals("Aucune partie trouvée")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
            startMonth = YearMonth.parse(lastGameMonth, formatter).plusMonths(1);
            // Sinon, on définit la date de début il y a 2 ans
        } else {
            startMonth = YearMonth.now().minusYears(2).withMonth(1);
        }

        YearMonth currentMonth = YearMonth.now();
        RestTemplate restTemplate = new RestTemplate();

        // boucle sur les endpoints
        for (YearMonth month = startMonth; month.isBefore(currentMonth.plusMonths(1)); month = month.plusMonths(1)) {
            String url = String.format("https://api.chess.com/pub/player/%s/games/%d/%02d", username, month.getYear(), month.getMonthValue());
            try {
                // Appel à l'API
                String response = restTemplate.getForObject(url, String.class);

                // Create a list of games from the uploaded file
                List<Game> currentGamesList = createGamesList(response);

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    // Create a list of Game objects
    public static List<Game> createGamesList(String reponse) {

        JSONObject obj = new JSONObject(reponse);
        JSONArray games = obj.getJSONArray("games");

        List<Game> gamesToReturn = new ArrayList<>();

        for (int i = 0; i < games.length(); i++) {
            JSONObject game = games.getJSONObject(i);
            String url = game.getString("url");
            String pgnData = game.getString("pgn");
            JSONObject white = game.getJSONObject("white");
            JSONObject black = game.getJSONObject("black");

            int whiteRating = white.getInt("rating");
            int blackRating = black.getInt("rating");
            String whiteUsername = white.getString("username");
            String blackUsername = black.getString("username");





            try (BufferedReader reader = new BufferedReader(new StringReader(pgnData))) {
                String line;
                Game currentGame = null;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("[Event ")) {
                        if (currentGame != null) {
                            gamesToReturn.add(currentGame);
                        }
                        currentGame = new Game();
                    }

                    if (line.startsWith("[")) {
                        String key = line.substring(1, line.indexOf(' '));
                        String value = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));

                        switch (key) {
                            case "Event":
                                currentGame.setEvent(value);
                                break;
                            case "Site":
                                currentGame.setSite(value);
                                break;
                            case "Date":
                                currentGame.setDate(value);
                                break;
                            case "Round":
                                currentGame.setRound(value);
                                break;
                            case "White":
                                currentGame.setWhite(value);
                                break;
                            case "Black":
                                currentGame.setBlack(value);
                                break;
                            case "Result":
                                currentGame.setResult(value);
                                break;
                            case "WhiteElo":
                                currentGame.setWhiteelo(Integer.parseInt(value));
                                break;
                            case "BlackElo":
                                currentGame.setBlackelo(Integer.parseInt(value));
                                break;
                            case "TimeControl":
                                currentGame.setTimecontrol(value);
                                break;
                            case "EndTime":
                                currentGame.setEndtime(value);
                                break;
                            case "Termination":
                                currentGame.setTermination(value);
                                break;
                            // Ajoutez d'autres cas si nécessaire
                            default:
                                break;
                        }
                    } else if (!line.trim().isEmpty()) {
                        if (currentGame.getMoves() == null) {
                            currentGame.setMoves("");
                        }
                        currentGame.setMoves(currentGame.getMoves() + line + " ");
                    }
                    // Cette ligne devrait être à l'intérieur de la boucle while, juste avant sa fin
                    if (currentGame != null) {
                        currentGame.setDateandendtime(currentGame.getDate() + " " + currentGame.getEndtime());
                    }
                }

                if (currentGame != null) {
                    gamesToReturn.add(currentGame);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }




        }
        return gamesToReturn;



    }





}
