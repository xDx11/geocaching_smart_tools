package cz.uhk.fim.soucera.geocatcher.waypoints;

/**
 * Created by Prdík on 03.02.2017.
 */

public class Waypoint {
    private int id;
    private double lat;
    private double lon;
    private String name;
    private String cmt;
    private String desc;
    private String sym;
    private String type;
    private int id_cache;
    private int id_list;

    public Waypoint(int id, double lat, double lon, String name, String cmt, String desc, String sym, String type, int id_cache) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.cmt = cmt;
        this.desc = desc;
        this.sym = sym;
        this.type = type;
        this.id_cache = id_cache;
    }

    public Waypoint(int id, double lat, double lon, String name, String cmt, String desc, String sym, String type, int id_cache, int id_list) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.cmt = cmt;
        this.desc = desc;
        this.sym = sym;
        this.type = type;
        this.id_cache = id_cache;
        this.id_list = id_list;
    }

    public Waypoint(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCmt() {
        return cmt;
    }

    public void setCmt(String cmt) {
        this.cmt = cmt;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSym() {
        return sym;
    }

    public void setSym(String sym) {
        this.sym = sym;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId_cache() {
        return id_cache;
    }

    public void setId_cache(int id_cache) {
        this.id_cache = id_cache;
    }

    public int getId_list() {
        return id_list;
    }

    public void setId_list(int id_list) {
        this.id_list = id_list;
    }
}
