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
import java.util.Arrays;
import java.util.Comparator;
import org.springframework.web.client.RestTemplate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class GamesService {

    @Autowired
    private GamesRepository gamesRepository;

    public String getLastGameMonth(String username) {

        List<Game> games = gamesRepository.findByPlayerusername(username);

        //gameRepository.findAll().forEach(game -> games.add(game));
        //gamesRepository.findAll().forEach(games::add);

        if (games.isEmpty()) {
            return "Aucune partie trouvée";
        }

        // Date format
        DateTimeFormatter sourceFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("yyyy/MM");

        // We look for the last Game
        Game lastGame = games.stream()
                .max(Comparator.comparing(game -> LocalDate.parse(game.getDate(), sourceFormatter)))
                .orElse(null);

        if (lastGame == null) {
            return "Aucune partie trouvée";
        }

        // We look for the date of the last Game
        LocalDate lastGameDate = LocalDate.parse(lastGame.getDate(), sourceFormatter);
        return lastGameDate.format(targetFormatter);
    }

    public List<Game> getGames(){
        List<Game> games = new ArrayList<>();
        gamesRepository.findAll().forEach(game -> {
            games.add(game);
        });
        return games;
    }
    public void addGame(Game game){
        gamesRepository.save(game);
    }



    public void getGamesFromChessCom(String username, String lastGameMonth, int monthsToFetch) {

        YearMonth startMonth;

        // Si quelque chose en BDD, on définit la date de début au mois en cours
        if (!lastGameMonth.equals("Aucune partie trouvée")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
            startMonth = YearMonth.parse(lastGameMonth, formatter).plusMonths(1);
        } else {
            startMonth = YearMonth.now().minusMonths(monthsToFetch);
        }

        YearMonth endMonth = YearMonth.now();
        RestTemplate restTemplate = new RestTemplate();

        // boucle sur les endpoints
        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            String url = String.format("https://api.chess.com/pub/player/%s/games/%d/%02d", username, month.getYear(), month.getMonthValue());
            try {
                // Appel à l'API
                String response = restTemplate.getForObject(url, String.class);

                // Create a list of games
                List<Game> currentGamesList = createGamesList(response);

                for(Game game : currentGamesList){

                    game.setPlayerusername(username);

                    game.setMoves(formatMoves(game.getMoves()));

                    game.setResultForPlayer(findResultForPlayer(game.getTermination(), game.getPlayerusername()));

                    game.setEndOfGameBy(HowEndedTheGame(game.getTermination()));

                    // We add the Game to the database
                    addGame(game);
                }


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

    public static String formatMoves(String moves){


        // Regex to delete what is inside {}
        String cleanedString = moves.replaceAll("\\{[^}]+\\}", "");

        // Split the string into an array of moves
        String[] movesArray = cleanedString.split(" ");

        // Filter out moves containing "..."
        List<String> filteredMovesList = Arrays.stream(movesArray)
                .filter(move -> !move.contains("..."))
                .toList();

        // Join the filtered moves into a string
        String filteredMoves = String.join(" ", filteredMovesList);

        // Replace double spaces with single space
        return filteredMoves.replaceAll("  ", " ");


    }

    public static String findResultForPlayer(String termination, String playerUsername){
        String result = "";
        if(termination.contains("Partie nulle")){
            result = "drawn";
        }
        else if (termination.contains(playerUsername)){
            result = "won";
        }
        else {
            result = "lost";
        }
        return result;
    }

    public static String HowEndedTheGame(String termination){
        String result = "";

        if(termination.contains("temps")){result = "time";}
        else if (termination.contains("échec et mat")) {result = "checkmate";}
        else if (termination.contains("abandon")) {result = "abandonment";}
        else if (termination.contains("accord mutuel")) {result = "agreement";}
        else if (termination.contains("manque de matériel")) {result = "lack of equipment";}
        else if (termination.contains("pat")) {result = "pat";}
        else if (termination.contains("répétition")) {result = "repeat";}

        return result;
    }

}
