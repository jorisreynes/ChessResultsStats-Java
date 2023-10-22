package com.example.demo.model;

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
    private String endtime;
    private String termination;
    private String moves;
    private String playerusername;

    private String dateandendtime; // a mettre en unique value

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

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
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

    public void setMoves(String moves) {
        this.moves = moves;
    }

    public String getPlayerusername() {return playerusername;}

    public void setPlayerusername(String playerusername) {this.playerusername = playerusername;}

    public String getDateandendtime() {
        return dateandendtime;
    }

    public void setDateandendtime(String dateandendtime) {
        this.dateandendtime = dateandendtime;
    }
}
