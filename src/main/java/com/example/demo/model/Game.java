package com.example.demo.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "games")
public class Game {

    private String event;
    private String site;
    private String date;
    private String round;
    private String white;
    private String black;
    private String result;
    private Integer whiteelo;
    private Integer blackelo;
    private String timecontrol;
    private String endTime;
    private String termination;
    private String moves;
    private String playerusername;
    private String resultforplayer;
    private String endofgameby;
    private double accuracy;
    private String opening;
    private String eco;
    private Integer playerelo;
    @Indexed(unique = true)
    private String dateandendtime;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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

    public Integer getWhiteelo() {
        return whiteelo;
    }

    public void setWhiteelo(Integer whiteelo) {
        this.whiteelo = whiteelo;
    }

    public Integer getBlackelo() {
        return blackelo;
    }

    public void setBlackelo(Integer blackelo) {
        this.blackelo = blackelo;
    }

    public String getTimecontrol() {
        return timecontrol;
    }

    public void setTimecontrol(String timecontrol) {
        this.timecontrol = timecontrol;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
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

    public String getPlayerusername() {return playerusername;}

    public void setPlayerusername(String playerusername) {this.playerusername = playerusername;}

    public String getDateandendtime() {return dateandendtime;}

    public void setDateandendtime(String dateandendtime) {
        this.dateandendtime = dateandendtime;
    }

    public String getResultforplayer() {return resultforplayer;}

    public void setResultforplayer(String resultforplayer) {this.resultforplayer = resultforplayer;}

    public String getEndofgameby() {return endofgameby;}

    public void setEndofgameby(String endofgameby) {this.endofgameby = endofgameby;}

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

    public Integer getPlayerelo() {
        return playerelo;
    }

    public void setPlayerelo(Integer playerelo) {
        this.playerelo = playerelo;
    }
}
