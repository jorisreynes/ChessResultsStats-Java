package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/api")
public class UploadController {

    private String folderPath = "uploads";

    // Fonction pour enregistrer un fichier téléchargé depuis l'interface front-end Angular

    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String filePath = new File(folderPath, file.getOriginalFilename()).getAbsolutePath();

        // Si le fichier existe déjà, nous le supprimons
        File existingFile = new File(filePath);
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
    }
}
