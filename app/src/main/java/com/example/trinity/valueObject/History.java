package com.example.trinity.valueObject;

import java.time.Instant;

public class History {

    private Manga manga;
    private long lastAcess;

    public History(Manga manga, long lastAcess) {
        this.manga = manga;
        this.lastAcess = lastAcess;

    }

    public Manga getManga() {
        return manga;
    }

    public void setManga(Manga manga) {
        this.manga = manga;
    }

    public long getLastAcess() {
        return lastAcess;
    }

    public void setLastAcess(long lastAcess) {
        this.lastAcess = lastAcess;
    }

    public String returnLastTimeAccessed(){

        long currentDate = Instant.now().getEpochSecond();
        long time = currentDate - this.lastAcess;
        long year = time / 31556926;
        time = year > 0?time % 31556926:time;
        long month = time / 2592000;
        time = month > 0?time % 2592000:time;
        long day = time/86000;
        time = day > 0?time%86000:time;
        long hour = time / 3600;
        time  = hour > 0?time%hour:time;
        long min = time/60;
        time = min > 0?time%60:time;
        long sec = time;

        if(year > 0){
            return "Há "+year+(year > 1?" anos atrás":" ano atrás");
        }
        if(month > 0){
            return "Há "+month+(month > 1?" meses atrás":" mês atrás");
        }
        if(day > 0){
            return "Há "+day+(day > 1?" dias atrás":" dia atrás");
        }
        if(hour > 0){
            return "Há "+hour+(hour > 1?" horas atrás":" hora atrás");
        }
        if(min > 0){
            return "Há "+min+(min > 1?" minutos atrás":" minuto atrás");
        }
        return "Há "+sec+(sec > 1?" segundos atrás":" segundo atrás");
    }
}
