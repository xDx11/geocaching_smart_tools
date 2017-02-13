package cz.uhk.fim.soucera.geocatcher;

import java.util.ArrayList;

import cz.uhk.fim.soucera.geocatcher.logs.Log_Cache;

public class Cache {

    private int id;
    private String name;
    private String type;
    private String size;
    private String help;
    private double difficulty;
    private double terrain;
    private double lon;
    private double lat;
    private String code;
    private String url;
    private String desc;
    private ArrayList<Log_Cache> logs;
    private double distance_to_me;

    private int id_list;

    public Cache(int id, String name, String type, String size, String help, double difficulty, double terrain, double lon, double lat) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.size = size;
        this.help = help;
        this.difficulty = difficulty;
        this.terrain = terrain;
        this.lon = lon;
        this.lat = lat;
        logs = new ArrayList<>();
    }

    public Cache(int id, String name, String code, String type, String size, double difficulty, double terrain, double lon, double lat, String desc, String help, String URL, int id_list) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
        this.size = size;
        this.difficulty = difficulty;
        this.terrain = terrain;
        this.lon = lon;
        this.lat = lat;
        this.desc = desc;
        this.help = help;
        this.url = URL;
        this.id_list = id_list;
        logs = new ArrayList<>();
    }

    public Cache(){
        logs = new ArrayList<>();
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public double getTerrain() {
        return terrain;
    }

    public void setTerrain(double terrain) {
        this.terrain = terrain;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getId_list() {
        return id_list;
    }

    public void setId_list(int id_list) {
        this.id_list = id_list;
    }

    public ArrayList<Log_Cache> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<Log_Cache> logs) {
        this.logs = logs;
    }

    public double getDistance_to_me() {
        return distance_to_me;
    }

    public void setDistance_to_me(double distance_to_me) {
        this.distance_to_me = distance_to_me;
    }
}
