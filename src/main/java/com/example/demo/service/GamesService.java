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
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public LocalDateTime getLastGameDateAndTime(String username) {

        List<Game> games = gamesRepository.findByPlayerUsername(username);

        if (games.isEmpty()) {
            return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        }
        // Date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        // We look for the last Game
        Game lastGame = games.stream()
                .max(Comparator.comparing(Game::getDateAndEndTime)).orElse(null);

        if (lastGame == null) {
            return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        }
        // We look for the date of the last Game
		return lastGame.getDateAndEndTime();
    }

    public List<String> getGamesFromChessCom(String username, LocalDateTime lastGameDateAndTime, int maximumNumberOfMonthsToFetch) {

        List<String> dataList = new ArrayList<>();

        YearMonth now = YearMonth.now();
        int numberOfMonthsToFetch;

        if (lastGameDateAndTime != null) {

            // We transform the string yyyy.mm.dd hh:mm:ss into a date yyyy.mm
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

            YearMonth lastGameYearMonth = YearMonth.from(lastGameDateAndTime);

            // We calculate the number of months between the lastGameDateAndTime and the current month
            numberOfMonthsToFetch = (int) lastGameYearMonth.until(now, ChronoUnit.MONTHS) + 1; // +1 to include lastGameDateAndTime

            // If the calculated number is bigger than maximumNumberOfMonthsToFetch, we use maximumNumberOfMonthsToFetch
            numberOfMonthsToFetch = Math.min(numberOfMonthsToFetch, maximumNumberOfMonthsToFetch);
        } else {
            // If there is no game in database, we fetch maximumNumberOfMonthsToFetch months of data
            numberOfMonthsToFetch = maximumNumberOfMonthsToFetch;
        }

        RestTemplate restTemplate = new RestTemplate();

        for (int i = numberOfMonthsToFetch -1; i >= 0; i--) {

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
    public List<Game> createFormattedGamesList(List<String> dataList, String username, LocalDateTime lastGameDateAndTime) {

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

                            if (currentGame != null && currentGame.getDateAndEndTime().isAfter(lastGameDateAndTime)) {
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
                                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                                    LocalDate date = LocalDate.parse(value, dateFormatter);
                                    currentGame.setDate(date);
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
                                    currentGame.setWhiteElo(Integer.parseInt(value));
                                    break;
                                case "BlackElo":
                                    currentGame.setBlackElo(Integer.parseInt(value));
                                    break;
                                case "TimeControl":
                                    currentGame.setTimeControl(value);
                                    break;
                                case "EndTime":
                                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                                    LocalTime time = LocalTime.parse(value, timeFormatter);
                                    currentGame.setEndTime(time);
                                    if(currentGame.getDate() != null){
                                        LocalDateTime dateTime = LocalDateTime.of(currentGame.getDate(), time);
                                        currentGame.setDateAndEndTime(dateTime);
                                    }
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
                    }
                    if (currentGame != null) {

                        // Here we set some additional data based on first data
                        if(Objects.equals(currentGame.getWhite(), username)){
                            currentGame.setPlayerElo(currentGame.getWhiteElo());
                        }
                        else {
                            currentGame.setPlayerElo(currentGame.getBlackElo());
                        }
                        currentGame.setPlayerUsername(username);

                        currentGame.setMoves(formatMoves(currentGame.getMoves()));

                        currentGame.setCategory(setCategoryFromTimeControl(currentGame.getTimeControl()));

                        currentGame.setResultForPlayer(findResultForPlayer(currentGame.getTermination(), currentGame.getPlayerUsername()));

                        currentGame.setEndOfGameBy(howEndedTheGame(currentGame.getTermination()));

                        if(currentGame.getDateAndEndTime().isAfter(lastGameDateAndTime)){
                            gamesToReturn.add(currentGame);
                        }
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

    public static String setCategoryFromTimeControl(String timeControl) {
        String category = "";
        switch (timeControl) {
            case "60", "120", "120+1":
                category = "bullet";
            break;
            case "180", "180+2", "300":
                category = "blitz";
            break;
            case "600", "600+5", "1800":
                category = "rapid";
                break;
        }
        return category;
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
        return gamesRepository.findByPlayerUsername(username);
    }
}
