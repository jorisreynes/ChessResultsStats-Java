package com.example.demo.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Document(collection = "games")
public class Game {

    private String event;
    private String site;
    private LocalDate date;
    private String round;
    private String white;
    private String black;
    private String result;
    private Integer whiteElo;
    private Integer blackElo;
    private Integer playerElo;
    private String timeControl;
    private String category;
    private LocalTime endTime;
    private String termination;
    private String moves;
    private String playerUsername;
    private String resultForPlayer;
    private String endOfGameBy;
    private double accuracy;
    private String opening;
    private String eco;
    @Indexed(unique = true)
    @DateTimeFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime dateAndEndTime;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    public String getRound() {return round;}

    public void setRound(String round) {this.round = round;}

    public String getWhite() {
        return white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public String getBlack() {
        return black;
    }

    public void setBlack(String black) {
        this.black = black;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getWhiteElo() {
        return whiteElo;
    }

    public void setWhiteElo(Integer whiteElo) {
        this.whiteElo = whiteElo;
    }

    public Integer getBlackElo() {
        return blackElo;
    }

    public void setBlackElo(Integer blackElo) {
        this.blackElo = blackElo;
    }

    public String getTimeControl() {
        return timeControl;
    }

    public void setTimeControl(String timeControl) {
        this.timeControl = timeControl;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTermination() {
        return termination;
    }

    public void setTermination(String termination) {
        this.termination = termination;
    }

    public String getMoves() {
        return moves;
    }

    public void setMoves(String moves) {this.moves = moves;}

    public String getPlayerUsername() {return playerUsername;}

    public void setPlayerUsername(String playerUsername) {this.playerUsername = playerUsername;}

    public LocalDateTime  getDateAndEndTime() {return dateAndEndTime;}

    public void setDateAndEndTime(LocalDateTime  dateAndEndTime) {
        this.dateAndEndTime = dateAndEndTime;
    }

    public String getResultForPlayer() {return resultForPlayer;}

    public void setResultForPlayer(String resultForPlayer) {this.resultForPlayer = resultForPlayer;}

    public String getEndOfGameBy() {return endOfGameBy;}

    public void setEndOfGameBy(String endOfGameBy) {this.endOfGameBy = endOfGameBy;}

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getOpening() {
        return opening;
    }

    public void setOpening(String opening) {
        this.opening = opening;
    }

    public String getEco() { return eco; }

    public void setEco(String eco) { this.eco = eco; }

    public Integer getPlayerElo() {
        return playerElo;
    }

    public void setPlayerElo(Integer playerElo) {
        this.playerElo = playerElo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
