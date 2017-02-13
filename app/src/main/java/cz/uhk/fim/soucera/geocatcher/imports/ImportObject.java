package cz.uhk.fim.soucera.geocatcher.imports;

import java.util.ArrayList;

import cz.uhk.fim.soucera.geocatcher.Cache;
import cz.uhk.fim.soucera.geocatcher.waypoints.Waypoint;

public class ImportObject {
    ArrayList<Waypoint> waypoints;
    ArrayList<Cache> caches;
    ArrayList<ArrayList<Waypoint>> groupsOfWaypoints;

    public ImportObject(ArrayList<Waypoint> waypoints, ArrayList<Cache> caches) {
        this.waypoints = waypoints;
        this.caches = caches;
    }

    public ImportObject(ArrayList<Waypoint> waypoints, ArrayList<Cache> caches, ArrayList<ArrayList<Waypoint>> groupsOfWaypoints) {
        this.waypoints = waypoints;
        this.caches = caches;
        this.groupsOfWaypoints = groupsOfWaypoints;
    }

    public ImportObject() {
    }

    public ArrayList<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(ArrayList<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public ArrayList<Cache> getCaches() {
        return caches;
    }

    public void setCaches(ArrayList<Cache> caches) {
        this.caches = caches;
    }

    public ArrayList<ArrayList<Waypoint>> getGroupsOfWaypoints() {
        return groupsOfWaypoints;
    }

    public void setGroupsOfWaypoints(ArrayList<ArrayList<Waypoint>> groupsOfWaypoints) {
        this.groupsOfWaypoints = groupsOfWaypoints;
    }
}
