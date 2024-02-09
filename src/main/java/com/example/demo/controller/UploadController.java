package com.example.demo.controller;

import com.example.demo.service.GamesService;
import com.example.demo.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/api")
@ResponseStatus(HttpStatus.OK)
public class UploadController {

    private final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private GamesService gamesService;

    // Function to save a file uploaded from the Angular front-end interface
    @CrossOrigin(origins = "*")
    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file) {

        String folderPath = "uploads";
        String filePath = new File(folderPath, file.getOriginalFilename()).getAbsolutePath();

        // If the file already exists, delete it
        File existingFile = new File(filePath);

        try {
            if (existingFile.exists()) {
                existingFile.delete();
            }

            // If the folder doesn't exist, create it
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Save the file
            file.transferTo(new File(filePath));

        } catch (Exception ex) {
            logger.error("File Upload error: " + ex);
            System.out.println("Error uploading file: " + ex);
        }

        // Create a list of games from the uploaded file
        List<Game> currentGamesList = createGamesList(filePath);

        String playerUsername = findPlayerUsername(currentGamesList);



        for(Game game : currentGamesList){

            game.setPlayerusername(playerUsername);

            game.setMoves(formatMoves(game.getMoves()));

            game.setResultForPlayer(findResultForPlayer(game.getTermination(), game.getPlayerusername()));

            game.setEndOfGameBy(HowEndedTheGame(game.getTermination()));

            // We add the Game to the database
            gamesService.addGame(game);
        }

    }

    // Create a list of Game objects from a PGN file
    public static List<Game> createGamesList(String filePath) {
        List<Game> games = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Game currentGame = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[Event ")) {
                    // If a new game is encountered, add the previous game to the list
                    if (currentGame != null) {
                        games.add(currentGame);
                    }
                    // Create a new Game object for the current game
                    currentGame = new Game();
                }

                if (line.startsWith("[")) {
                    // Parse the key and value from the line
                    String key = line.substring(1, line.indexOf(' '));
                    String value = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));

                    // Set the appropriate field in the Game object based on the key
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
                    // If the line is not empty, append it to the moves of the current game
                    if (currentGame.getMoves() == null) {
                        currentGame.setMoves("");
                    }
                    currentGame.setMoves(currentGame.getMoves() + line + " ");
                }
                currentGame.setDateandendtime(currentGame.getDate() + " " + currentGame.getEndtime());
            }

            if (currentGame != null) {
                // Add the last game to the list
                games.add(currentGame);
            }

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        return games;
    }

    public static String findPlayerUsername(List<Game> games) {

        // Function to find the username of the player we have the most in white and black
        List<String> allUsernamesList = games.stream()
                .flatMap(game -> Stream.of(game.getWhite(), game.getBlack()))
                .toList();

        // We find the username that appears the most
        Map<String, Long> usernameCounts = allUsernamesList.stream()
                .collect(Collectors.groupingBy(String::toString, Collectors.counting()));

        String playerUsername = usernameCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return playerUsername;
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
