package cz.uhk.fim.soucera.geocatcher.logs;

/**
 * Created by Radek Soucek on 01.02.2017.
 */

public class Log_Cache {
    private int id;
    private String date;
    private String type;
    private String finder;
    private String text;
    private int id_cache;

    public Log_Cache(){

    }

    public Log_Cache(int id, String date, String type, String finder, String text, int id_cache) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.finder = finder;
        this.text = text;
        this.id_cache = id_cache;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFinder() {
        return finder;
    }

    public void setFinder(String finder) {
        this.finder = finder;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId_cache() {
        return id_cache;
    }

    public void setId_cache(int id_cache) {
        this.id_cache = id_cache;
    }
}
