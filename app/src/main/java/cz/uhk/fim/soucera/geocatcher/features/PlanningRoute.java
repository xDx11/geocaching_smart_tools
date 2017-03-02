package cz.uhk.fim.soucera.geocatcher.features;

import android.util.Log;
import com.google.android.gms.maps.model.Marker;
import java.util.ArrayList;
import cz.uhk.fim.soucera.geocatcher.utils.Utils;

/**
 * Created by Radek Soucek on 02.03.2017.
 */

public class PlanningRoute {
    private String TAG = PlanningRoute.class.toString();
    private double totalDistance;

    public PlanningRoute(){
        totalDistance = 0;
    }

    /*       Planning shortest linear route          */
    public ArrayList<Marker> planningShortestRoute(ArrayList<Marker> routePoints) {
        Log.i(TAG, "planningShortestRoute");
        //get distances matrix
        int matrixSize = routePoints.size();
        double[][] matrix = new double[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrix[i][j] = Utils.CalculationByDistance(routePoints.get(i).getPosition(), routePoints.get(j).getPosition());
            }
        }

        //display matrix and routePoints
                /*
                for (int i = 0; i < matrixSize; i++) {
                    for (int j = 0; j < matrixSize; j++) {
                        System.out.print("\t" + matrix[i][j]);
                    }
                    System.out.println();
                }
                System.out.println("///////////////////////////////");
                for (int i = 0; i < routePoints.size(); i++) {
                    System.out.println(routePoints.get(i).getTitle());
                }
                System.out.println("///////////////////////////////");
                */

        //PLANNING ROUTE
        ArrayList<Marker> shortestRoutePoints = new ArrayList<>();
        totalDistance = 0;
        try {
            shortestRoutePoints.add(routePoints.get(0));
            int pomIndex = 0;
            while (shortestRoutePoints.size() != routePoints.size()) {
                double min = 0;
                int pomJ;
                int i = pomIndex;
                for (int j = 0; j < matrixSize; j++) {
                    pomJ = 0;
                    while (min == 0) {
                        if (!shortestRoutePoints.contains(routePoints.get(pomJ))) {
                            min = matrix[i][pomJ];
                            pomIndex = pomJ;
                        }
                        if (min == 0)
                            pomJ += 1;
                    }
                    if (matrix[i][j] <= min && matrix[pomIndex][j] != 0) {
                        if (!shortestRoutePoints.contains(routePoints.get(j))) {
                            min = matrix[i][j];
                            pomIndex = j;
                        }
                    }
                }
                totalDistance += min;
                shortestRoutePoints.add(routePoints.get(pomIndex));
            }


            //display shortest Route Points
                    /*
                    for (int i = 0; i < shortestRoutePoints.size(); i++) {
                        System.out.println(shortestRoutePoints.get(i).getTitle());
                    }
                    */

            //drawLine points between shortest Route Points, rewriting markers snipper, recolor markers


            return shortestRoutePoints;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /*       GOOGLE API DIRECTION - planning shortest route          */
    public ArrayList<Marker> planningShortestRouteGoogleApiDirection(ArrayList<Marker> routePoints) {
        Log.i(TAG, "planningShortestRouteGoogleApiDirection");
        //get distances matrix
        int matrixSize = routePoints.size();
        double[][] matrix = new double[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrix[i][j] = Utils.CalculationByDistance(routePoints.get(i).getPosition(), routePoints.get(j).getPosition());
            }
        }

        //PLANNING ROUTE
        ArrayList<Marker> shortestRoutePoints = new ArrayList<>();
        try {
            shortestRoutePoints.add(routePoints.get(0));
            int pomIndex = 0;
            while (shortestRoutePoints.size() != routePoints.size()) {
                double min = 0;
                int pomJ;
                int i = pomIndex;
                for (int j = 0; j < matrixSize; j++) {
                    pomJ = 0;
                    while (min == 0) {
                        if (!shortestRoutePoints.contains(routePoints.get(pomJ))) {
                            min = matrix[i][pomJ];
                            pomIndex = pomJ;
                        }
                        if (min == 0)
                            pomJ += 1;
                    }
                    if (matrix[i][j] <= min && matrix[pomIndex][j] != 0) {
                        if (!shortestRoutePoints.contains(routePoints.get(j))) {
                            min = matrix[i][j];
                            pomIndex = j;
                        }
                    }
                }
                shortestRoutePoints.add(routePoints.get(pomIndex));
            }
            return shortestRoutePoints;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getTotalDistance() {
        return totalDistance;
    }
}
