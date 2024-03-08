package com.example.demo.service;

import com.example.demo.model.Game;
import com.example.demo.repository.GamesRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.web.client.RestTemplate;
import java.time.YearMonth;

@Service
public class GamesService {

    @Autowired
    private GamesRepository gamesRepository;

    private static final Logger logger = LoggerFactory.getLogger(GamesService.class);


    public String getLastGameMonth(String username) {

        List<Game> games = gamesRepository.findByPlayerusername(username);

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


    public List<String> getGamesFromChessCom(String username, String lastGameMonth, int maximumNumberOfMonthsToFetch) {

        List<String> dataList = new ArrayList<>();

        YearMonth now = YearMonth.now();
        int numberOfMonthsToFetch;

        if (!lastGameMonth.equals("Aucune partie trouvée")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
            YearMonth lastGameYearMonth = YearMonth.parse(lastGameMonth, formatter);

            // We calculate the number of months between the lastGameMonth and the current month
            numberOfMonthsToFetch = (int) lastGameYearMonth.until(now, ChronoUnit.MONTHS) + 1; // +1 to include lastGameMonth

            // If the calculated number is bigger than maximumNumberOfMonthsToFetch, we use maximumNumberOfMonthsToFetch
            numberOfMonthsToFetch = Math.min(numberOfMonthsToFetch, maximumNumberOfMonthsToFetch);
        } else {
            // If there is no game in database, we fetch maximumNumberOfMonthsToFetch months of data
            numberOfMonthsToFetch = maximumNumberOfMonthsToFetch;
        }

        RestTemplate restTemplate = new RestTemplate();

        for (int i = 0; i < numberOfMonthsToFetch; i++) {

            // Calcul du mois pour lequel effectuer l'appel API
            YearMonth monthToFetch = now.minusMonths(i);

            String url = String.format("https://api.chess.com/pub/player/%s/games/%d/%02d", username, monthToFetch.getYear(), monthToFetch.getMonthValue());
            try {
                // API call
                String response = restTemplate.getForObject(url, String.class);

                dataList.add(response);

            } catch (Exception e) {
                Thread.currentThread().interrupt();
                logger.error("Error in getGamesFromChessCom", e);
            }
        }
        return dataList;
    }

    // Create a list of Game objects
    public List<Game> createFormattedGamesList(List<String> dataList, String username) {

        List<Game> gamesToReturn = new ArrayList<>();

        for(String data : dataList)
        {
            JSONObject obj = new JSONObject(data);
            JSONArray games = obj.getJSONArray("games");

            for (int i = 0; i < games.length(); i++) {

                JSONObject game = games.getJSONObject(i);
                String pgnData = game.getString("pgn");
                JSONObject white = game.getJSONObject("white");

                // Accuracy part, old games dont have accuracies
                double accuracy =0;
                if(game.has("accuracies")){
                    if(Objects.equals(white.getString("username"), username)){
                        accuracy = game.getJSONObject("accuracies").getDouble("white");
                    }
                    else{
                        accuracy = game.getJSONObject("accuracies").getDouble("black");
                    }
                }

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

                        if(accuracy!= 0 && currentGame!= null && currentGame.getAccuracy() == 0) {
                            currentGame.setAccuracy(accuracy);
                        }

                        if (currentGame != null && line.startsWith("[")) {
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
                                    currentGame.setEndTime(value);
                                    break;
                                case "Termination":
                                    currentGame.setTermination(value);
                                    break;
                                case "ECO":
                                    currentGame.setEco(value);
                                    break;
                                case "ECOUrl":
                                    String[] parts = value.split("/");
                                    currentGame.setOpening(parts[parts.length -1]);
                                    break;
                                default:
                                    break;
                            }
                        } else if (currentGame != null && !line.trim().isEmpty()) {
                            if (currentGame.getMoves() == null) {
                                currentGame.setMoves("");
                            }
                            currentGame.setMoves(currentGame.getMoves() + line + " ");
                        }
                        if (currentGame != null) {
                            currentGame.setDateandendtime(currentGame.getDate() + " " + currentGame.getEndTime());
                        }
                    }
                    if (currentGame != null) {

                        currentGame.setPlayerusername(username);

                        currentGame.setMoves(formatMoves(currentGame.getMoves()));

                        currentGame.setResultforplayer(findResultForPlayer(currentGame.getTermination(), currentGame.getPlayerusername()));

                        currentGame.setEndofgameby(howEndedTheGame(currentGame.getTermination()));

                        gamesToReturn.add(currentGame);
                    }
                } catch (Exception e) {
                    logger.error("Error in createFormattedGamesList", e);
                }
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
        String result;
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

    public static String howEndedTheGame(String termination){
        String result = "";

        if(termination.contains("temps") || termination.contains("time")) {
            result = "time";
        }
        else if (termination.contains("échec et mat") || termination.contains("checkmate")) {
            result = "checkmate";
        }
        else if (termination.contains("abandon") || termination.contains("resignation")) {
            result = "abandonment";
        }
        else if (termination.contains("accord mutuel") || termination.contains("mutual agreement")) {
            result = "agreement";
        }
        else if (termination.contains("manque de matériel") || termination.contains("insufficient material")) {
            result = "lack of equipment";
        }
        else if (termination.contains("pat") || termination.contains("stalemate")) {
            result = "pat";
        }
        else if (termination.contains("répétition") || termination.contains("repetition")) {
            result = "repeat";
        }
        return result;
    }

    public void saveGameInDatabase(List<Game> game){
        try{
            gamesRepository.saveAll(game);
        }
        catch(Exception e){
            logger.error("Error in saveGameInDatabase", e);
        }
    }

    public List<Game> getGames(String username){
        return gamesRepository.findByPlayerusername(username);
    }
}
