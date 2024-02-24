package com.example.demo.service;

import com.example.demo.model.Game;
import com.example.demo.repository.GamesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import org.springframework.web.client.RestTemplate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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

    public void updateGamesFromChessCom(String username, String lastGameMonth) {

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

        for (YearMonth month = startMonth; month.isBefore(currentMonth.plusMonths(1)); month = month.plusMonths(1)) {
            String url = String.format("https://api.chess.com/pub/player/%s/games/%d/%02d", username, month.getYear(), month.getMonthValue());
            try {
                // Appel à l'API
                String response = restTemplate.getForObject(url, String.class);

                // Ajouter le retour en BDD

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }
}
