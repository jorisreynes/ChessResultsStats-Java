package com.example.demo.controller;

import com.example.demo.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin(origins="*")
@RequestMapping("/api")
@ResponseStatus(HttpStatus.OK)
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    // Fonction pour enregistrer un fichier téléchargé depuis l'interface front-end Angular

    @CrossOrigin(origins="*")
    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file) {
        String folderPath = "uploads";
        String filePath = new File(folderPath, file.getOriginalFilename()).getAbsolutePath();

        // Si le fichier existe déjà, nous le supprimons
        File existingFile = new File(filePath);

        try{
            if (existingFile.exists()) {
                existingFile.delete();
            }

            // Si le dossier n'existe pas, nous le créons
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Enregistrement du fichier
            file.transferTo(new File(filePath));

        } catch (Exception ex){
            logger.error("File Upload error : " + ex);
            System.out.println("Erreur dans l'upload du fichier : " + ex);
        }
        List<Game> currentGamesList = createGamesList(filePath);
    }



    public static List<Game> createGamesList(String filePath) {
        List<Game> games = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Game currentGame = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[Event ")) {
                    if (currentGame != null) {
                        games.add(currentGame);
                    }
                    currentGame = new Game();
                }

                if (line.startsWith("[")) {
                    String key = line.substring(1, line.indexOf(' ') );
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
                    currentGame.setMoves(currentGame.getMoves() + line + " ");
                }
            }

            if (currentGame != null) {
                games.add(currentGame);
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        return games;
    }
}
