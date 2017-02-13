package cz.uhk.fim.soucera.geocatcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import cz.uhk.fim.soucera.geocatcher.logs.Log_Cache;
import cz.uhk.fim.soucera.geocatcher.waypoints.Waypoint;

public class Caches_DB {
    private static final String TAG = Caches_DB.class.getName();
    private static final String DATABASE_NAME = "CacheDB";
    private static final int DATABASE_VERSION = 3;
    private static final String TB_NAME_CACHE = "Caches";
    private static final String TB_NAME_LIST = "Lists";
    private static final String TB_NAME_LOG = "Logs";
    private static final String TB_NAME_WAYPOINT = "Waypoint";
    //SPOLECNE
    private static final String COLUMN_ID = "_id";
    //TABLE CACHES
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CODE = "code";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_SIZE = "size";
    private static final String COLUMN_DIFFICULTY = "difficulty";
    private static final String COLUMN_TERRAIN = "terrain";
    private static final String COLUMN_LONG = "long";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_DESC = "desc";
    private static final String COLUMN_HELP = "help";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_ID_LIST = "id_list";
    //TABLE LISTS
    private static final String COLUMN_LIST_NAME = "name";
    //TABLE LOGS
    private static final String COLUMN_LOG_DATE = "date";
    private static final String COLUMN_LOG_TYPE = "type";
    private static final String COLUMN_LOG_FINDER = "finder";
    private static final String COLUMN_LOG_TEXT = "description";
    private static final String COLUMN_LOG_ID_CACHE = "id_cache";
    //TABLE WAYPOINTS
    private static final String COLUMN_WPT_TYPE = "type";
    private static final String COLUMN_WPT_CMT = "cmt";
    private static final String COLUMN_WPT_DESC = "desc";
    private static final String COLUMN_WPT_LAT = "lat";
    private static final String COLUMN_WPT_LON = "lon";
    private static final String COLUMN_WPT_NAME = "name";
    private static final String COLUMN_WPT_SYM = "sym";
    private static final String COLUMN_WPT_ID_CACHE = "id_cache";
    private static final String COLUMN_WPT_ID_LIST = "id_list";



    private static final int LIST_HLEDANE = 0;
    private static final int LIST_NALEZENE = 1;
    private static final int FILTER_ALL = 0;
    private static final int FILTER_FOUND = 1;
    private static final int FILTER_NOT_FOUND = 2;
    private static final int FILTER_TRADITIONAL = 1;
    private static final int FILTER_MULTI = 2;
    private static final int FILTER_MYSTERY = 3;

    private static final String[] columns = { COLUMN_ID,COLUMN_NAME, COLUMN_CODE, COLUMN_TYPE,
            COLUMN_SIZE, COLUMN_DIFFICULTY, COLUMN_TERRAIN, COLUMN_LONG, COLUMN_LAT, COLUMN_DESC, COLUMN_HELP, COLUMN_URL, COLUMN_ID_LIST };

    private static final String[] COLUMNS_LOGS = { COLUMN_ID,COLUMN_LOG_DATE, COLUMN_LOG_TYPE, COLUMN_LOG_FINDER,
             COLUMN_LOG_TEXT, COLUMN_LOG_ID_CACHE};

    private static final String[] COLUMNS_WPTS = { COLUMN_ID,COLUMN_WPT_LAT, COLUMN_WPT_LON, COLUMN_WPT_NAME,
            COLUMN_WPT_CMT, COLUMN_WPT_DESC, COLUMN_WPT_SYM, COLUMN_WPT_TYPE, COLUMN_WPT_ID_CACHE, COLUMN_WPT_ID_LIST};

    private static final String ORDER_BY = COLUMN_ID + " DESC";
    private static final String ORDER_BY_ASC = COLUMN_ID + " ASC";

    private SQLiteOpenHelper openHelper;

    public Caches_DB(Context ctx) {
        openHelper = new DatabaseHelper(ctx);
    }

    /*
    public Cursor getCachesCursor() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db.query(TB_NAME, columns, null, null, null, null, ORDER_BY);
    }
    */

    public ArrayList<Cache> getCaches() {
        Log.i(TAG, "getCaches");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c = db.query(TB_NAME_CACHE, columns, null, null, null, null, ORDER_BY);
        ArrayList<Cache> caches = new ArrayList<>();
        if (c != null) {
            while(c.moveToNext()) {
                caches.add(new Cache(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getString(9), c.getString(10), c.getString(11), c.getInt(12)));
            }
            c.close();
        }
        db.close();
        return caches;
    }

    public ArrayList<Cache> getCaches(String findString) {
        Log.i(TAG, "getCachesFound");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c = db.query(TB_NAME_CACHE, columns, COLUMN_NAME + " LIKE '%" + findString + "%'", null, null, null, ORDER_BY);
        ArrayList<Cache> caches = new ArrayList<>();
        if (c != null) {
            while(c.moveToNext()) {
                caches.add(new Cache(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getString(9), c.getString(10), c.getString(11), c.getInt(12)));
            }
            c.close();
        }
        db.close();
        return caches;
    }

    public ArrayList<Cache> getCaches(int filterType, int sortingType, String findString) {
        Log.i(TAG, "getCachesFilter");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c;

        final int NAME = 1;
        final int CODE = 2;
        final int TYPE = 3;
        String sorting_order_by;
        switch(sortingType){
            case NAME:
                sorting_order_by = COLUMN_NAME + " ASC";
                break;
            case CODE:
                sorting_order_by = COLUMN_CODE + " ASC";
                break;
            case TYPE:
                sorting_order_by = COLUMN_TYPE + " ASC";
                break;
            default:
                sorting_order_by = COLUMN_ID + " DESC";
                break;
        }

        switch(filterType){
            case FILTER_FOUND:
                c = db.query(TB_NAME_CACHE, columns, COLUMN_ID_LIST+"="+LIST_NALEZENE + " AND " +COLUMN_NAME + " LIKE '%" + findString + "%'", null, null, null, sorting_order_by);
                break;
            case FILTER_NOT_FOUND:
                c = db.query(TB_NAME_CACHE, columns, COLUMN_ID_LIST+"="+LIST_HLEDANE + " AND " +COLUMN_NAME + " LIKE '%" + findString + "%'", null, null, null, sorting_order_by);
                break;
            default:
                c = db.query(TB_NAME_CACHE, columns, COLUMN_NAME + " LIKE '%" + findString + "%'", null, null, null, sorting_order_by);
                break;
        }
        ArrayList<Cache> caches = new ArrayList<>();
        if (c != null) {
            while(c.moveToNext()) {
                caches.add(new Cache(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getString(9), c.getString(10), c.getString(11), c.getInt(12)));
            }
            c.close();
        }
        db.close();
        return caches;
    }

    public ArrayList<Cache> getCaches(int filterType, int sortingType) {
        Log.i(TAG, "getCachesFilter");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c;

        final int NAME = 1;
        final int CODE = 2;
        final int TYPE = 3;
        String sorting_order_by;
        switch(sortingType){
            case NAME:
                sorting_order_by = COLUMN_NAME + " ASC";
                break;
            case CODE:
                sorting_order_by = COLUMN_CODE + " ASC";
                break;
            case TYPE:
                sorting_order_by = COLUMN_TYPE + " ASC";
                break;
            default:
                sorting_order_by = COLUMN_ID + " DESC";
                break;
        }

        switch(filterType){
            case FILTER_FOUND:
                c = db.query(TB_NAME_CACHE, columns, COLUMN_ID_LIST+"="+LIST_NALEZENE, null, null, null, sorting_order_by);
                break;
            case FILTER_NOT_FOUND:
                c = db.query(TB_NAME_CACHE, columns, COLUMN_ID_LIST+"="+LIST_HLEDANE, null, null, null, sorting_order_by);
                break;
            default:
                c = db.query(TB_NAME_CACHE, columns, null, null, null, null, sorting_order_by);
                break;
        }
        ArrayList<Cache> caches = new ArrayList<>();
        if (c != null) {
            while(c.moveToNext()) {
                caches.add(new Cache(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getString(9), c.getString(10), c.getString(11), c.getInt(12)));
            }
            c.close();
        }
        db.close();
        return caches;
    }

    public ArrayList<Cache> getCachesByFilter(int filterFind, int filterType) {
        Log.i(TAG, "getCachesFilterFindAndType");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c;

        String filterClause;
        switch(filterType){
            case FILTER_ALL:
                filterClause = "'%'";
                break;
            case FILTER_TRADITIONAL:
                filterClause = "'%traditional%'";
                break;
            case FILTER_MULTI:
                filterClause = "'%multi%'";
                break;
            case FILTER_MYSTERY:
                filterClause = "'%unknown%'";
                break;
            default:
                filterClause = "'%'";
                break;
        }
        try {
            switch(filterFind){
                case FILTER_FOUND:
                    c = db.query(TB_NAME_CACHE, columns, COLUMN_ID_LIST+"="+LIST_NALEZENE + " AND " + COLUMN_TYPE + " LIKE " + filterClause, null, null, null, ORDER_BY);
                    break;
                case FILTER_NOT_FOUND:
                    c = db.query(TB_NAME_CACHE, columns, COLUMN_ID_LIST+"="+LIST_HLEDANE + " AND " + COLUMN_TYPE + " LIKE " + filterClause, null, null, null, ORDER_BY);
                    break;
                default:
                    c = db.query(TB_NAME_CACHE, columns, COLUMN_TYPE + " LIKE " + filterClause, null, null, null, ORDER_BY);
                    break;
            }
            ArrayList<Cache> caches = new ArrayList<>();
            if (c != null) {
                while(c.moveToNext()) {
                    caches.add(new Cache(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getString(9), c.getString(10), c.getString(11), c.getInt(12)));
                }
                c.close();
            }
            db.close();
            return caches;
        } catch (Exception e){
            e.printStackTrace();
        }


        return null;
    }

    public Cache getCache(int id) {
        Log.i(TAG, "getCache");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] selectionArgs = { String.valueOf(id) };
        Cursor c = db.query(TB_NAME_CACHE, columns, COLUMN_ID + "= ?", selectionArgs, null, null, ORDER_BY);
        Cache cache = null;
        if (c != null) {
            while(c.moveToNext()) {
                cache = new Cache(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getString(9), c.getString(10), c.getString(11), c.getInt(12));
            }
            c.close();
        }
        db.close();
        return cache;
    }

    public boolean deleteCache(long id) {
        Log.i(TAG, "deleteCache with id:" + id);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        String[] selectionArgs = { String.valueOf(id) };
        int deletedCount = db.delete(TB_NAME_CACHE, COLUMN_ID + "= ?", selectionArgs);
        int deletedCountWpt = db.delete(TB_NAME_WAYPOINT, COLUMN_WPT_ID_CACHE + "= ?", selectionArgs);
        db.close();
        return deletedCount > 0;
    }

    public boolean isCacheRecordFound(String geoCode){
        Log.i(TAG, "isCacheRecordFound with geocode: "+geoCode);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Cursor c = db.query(TB_NAME_CACHE, columns, COLUMN_CODE + " = '" + geoCode + "'", null, null, null, ORDER_BY);
        Cache cache = null;
        if (c != null) {
            while(c.moveToNext()) {
                cache = new Cache(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getString(9), c.getString(10), c.getString(11), c.getInt(12));
            }
            c.close();
        }
        db.close();
        return cache != null;
    }

    public long insertCache(Cache cache) {
        Log.i(TAG, "insertCache");
        SQLiteDatabase db = openHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, cache.getName());
        values.put(COLUMN_CODE, cache.getCode());
        values.put(COLUMN_TYPE, cache.getType());
        values.put(COLUMN_SIZE, cache.getSize());
        values.put(COLUMN_DIFFICULTY, cache.getDifficulty());
        values.put(COLUMN_TERRAIN, cache.getTerrain());
        values.put(COLUMN_LONG, cache.getLon());
        values.put(COLUMN_LAT, cache.getLat());
        values.put(COLUMN_DESC, cache.getDesc());
        values.put(COLUMN_HELP, cache.getHelp());
        values.put(COLUMN_URL, cache.getUrl());
        values.put(COLUMN_ID_LIST, 0);
        long id = db.insert(TB_NAME_CACHE, null, values);
        db.close();
        return id;
    }

    public boolean updateCache(Cache cache) {
        Log.i(TAG, "updateCache with id: " + cache.getId());
        SQLiteDatabase db = openHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, cache.getName());
        values.put(COLUMN_CODE, cache.getCode());
        values.put(COLUMN_TYPE, cache.getType());
        values.put(COLUMN_SIZE, cache.getSize());
        values.put(COLUMN_DIFFICULTY, cache.getDifficulty());
        values.put(COLUMN_TERRAIN, cache.getTerrain());
        values.put(COLUMN_LONG, cache.getLon());
        values.put(COLUMN_LAT, cache.getLat());
        values.put(COLUMN_DESC, cache.getDesc());
        values.put(COLUMN_HELP, cache.getHelp());
        values.put(COLUMN_URL, cache.getUrl());
        String[] selectionArgs = { String.valueOf(cache.getId()) };
        int updateCount = db.update(TB_NAME_CACHE, values, COLUMN_ID + "= ?", selectionArgs);
        db.close();
        return updateCount>0;
    }

    public boolean isCacheFound(Cache cache){
        Log.i(TAG, "updateCacheFoundStatus with id: " + cache.getId());
        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if(cache.getId_list()==LIST_HLEDANE){
            values.put(COLUMN_ID_LIST, LIST_NALEZENE);
        } else {
            values.put(COLUMN_ID_LIST, LIST_HLEDANE);
        }
        String[] selectionArgs = { String.valueOf(cache.getId()) };
        int updateCount = db.update(TB_NAME_CACHE, values, COLUMN_ID + "= ?", selectionArgs);
        db.close();

        ArrayList<Waypoint> wpts = getWpts(cache.getId());
        db = openHelper.getWritableDatabase();
        for(int i = 0; i < wpts.size(); i++){
            values = new ContentValues();
            if(wpts.get(i).getId_list()==LIST_HLEDANE){
                values.put(COLUMN_ID_LIST, LIST_NALEZENE);
            } else {
                values.put(COLUMN_ID_LIST, LIST_HLEDANE);
            }
            String[] selectionArgsWpt = { String.valueOf(wpts.get(i).getId()) };
            int updateCount2 = db.update(TB_NAME_WAYPOINT, values, COLUMN_ID + "= ?", selectionArgsWpt);
        }
        db.close();


        if(cache.getId_list()==LIST_HLEDANE){
            return false;
        } else {
            return true;
        }
    }

    public ArrayList<Log_Cache> getLogs(int id) {
        Log.i(TAG, "getLogs");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] selectionArgs = { String.valueOf(id) };
        Cursor c = db.query(TB_NAME_LOG, COLUMNS_LOGS, COLUMN_LOG_ID_CACHE + "= ?", selectionArgs, null, null, ORDER_BY_ASC);
        ArrayList<Log_Cache> logs = new ArrayList<>();
        if (c != null) {
            while(c.moveToNext()) {
                logs.add(new Log_Cache(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getInt(5)));
            }
            c.close();
        }
        db.close();
        return logs;
    }

    public boolean insertLogs(ArrayList<Log_Cache> logs, long id_cache){
        Log.i(TAG, "insertLogs");
        SQLiteDatabase db = openHelper.getWritableDatabase();
        long id = -1;
        for(int i = 0; i < logs.size(); i++){
            ContentValues values = new ContentValues();
            values.put(COLUMN_LOG_DATE, logs.get(i).getDate());
            values.put(COLUMN_LOG_TYPE, logs.get(i).getType());
            values.put(COLUMN_LOG_FINDER, logs.get(i).getFinder());
            values.put(COLUMN_LOG_TEXT, logs.get(i).getText());
            values.put(COLUMN_LOG_ID_CACHE, id_cache);
            id = db.insert(TB_NAME_LOG, null, values);
        }

        db.close();
        if(id>-1)
            return true;
        else
            return false;
    }

    public ArrayList<Waypoint> getWpts(int id) {
        Log.i(TAG, "getWpts");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] selectionArgs = { String.valueOf(id) };
        Cursor c = db.query(TB_NAME_WAYPOINT, COLUMNS_WPTS, COLUMN_WPT_ID_CACHE + "= ?", selectionArgs, null, null, ORDER_BY);
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        if (c != null) {
            while(c.moveToNext()) {
                waypoints.add(new Waypoint(c.getInt(0), c.getDouble(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getInt(8), c.getInt(9)));
            }
            c.close();
        }
        db.close();
        return waypoints;
    }

    public ArrayList<Waypoint> getWptsFilter(int filterType) {
        Log.i(TAG, "getWpts");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] selectionArgs = { String.valueOf(filterType) };
        Cursor c;
        switch(filterType){
            case FILTER_FOUND:
                c = db.query(TB_NAME_WAYPOINT, COLUMNS_WPTS, COLUMN_WPT_ID_LIST+"="+LIST_NALEZENE, null, null, null, ORDER_BY);
                break;
            case FILTER_NOT_FOUND:
                c = db.query(TB_NAME_WAYPOINT, COLUMNS_WPTS, COLUMN_WPT_ID_LIST+"="+LIST_HLEDANE, null, null, null, ORDER_BY);
                break;
            default:
                c = db.query(TB_NAME_WAYPOINT, COLUMNS_WPTS, null, null, null, null, ORDER_BY);
                break;
        }
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        if (c != null) {
            while(c.moveToNext()) {
                waypoints.add(new Waypoint(c.getInt(0), c.getDouble(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getInt(8), c.getInt(9)));
            }
            c.close();
        }
        db.close();
        return waypoints;
    }

    public ArrayList<Waypoint> getWptsByCode(String code) {
        Log.i(TAG, "getWpts");
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] selectionArgs = { code };
        Cursor c = db.query(TB_NAME_WAYPOINT, COLUMNS_WPTS, COLUMN_WPT_NAME + " LIKE '%" + code + "'", null, null, null, ORDER_BY);
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        if (c != null) {
            while(c.moveToNext()) {
                waypoints.add(new Waypoint(c.getInt(0), c.getDouble(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getInt(8), c.getInt(9)));
            }
            c.close();
        }
        db.close();
        return waypoints;
    }

    public Waypoint getWpt(int id) {
        Log.i(TAG, "getWpt with id: " + id);
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] selectionArgs = { String.valueOf(id) };
        Cursor c = db.query(TB_NAME_WAYPOINT, COLUMNS_WPTS, COLUMN_ID + "= ?", selectionArgs, null, null, ORDER_BY);
        Waypoint wpt = null;
        if (c != null) {
            while(c.moveToNext()) {
                wpt = new Waypoint(c.getInt(0), c.getDouble(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getInt(8), c.getInt(9));
            }
            c.close();
        }
        db.close();
        return wpt;
    }

    public boolean isWaypointRecordFound(String wptCode){
        Log.i(TAG, "isWaypointRecordFound with geocode: "+wptCode);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Cursor c = db.query(TB_NAME_WAYPOINT, COLUMNS_WPTS, COLUMN_WPT_NAME + " = '" + wptCode + "'", null, null, null, ORDER_BY);
        Waypoint wpt = null;
        if (c != null) {
            while(c.moveToNext()) {
                wpt = new Waypoint(c.getInt(0), c.getDouble(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getInt(8), c.getInt(9));
            }
            c.close();
        }
        db.close();
        return wpt != null;
    }

    public boolean insertWpt(Waypoint wpt, long id_cache){
        Log.i(TAG, "insertWpt");
        SQLiteDatabase db = openHelper.getWritableDatabase();
        long id = -1;
            ContentValues values = new ContentValues();
            values.put(COLUMN_WPT_LAT, wpt.getLat());
            values.put(COLUMN_WPT_LON, wpt.getLon());
            values.put(COLUMN_WPT_NAME, wpt.getName());
            values.put(COLUMN_WPT_CMT, wpt.getCmt());
            values.put(COLUMN_WPT_DESC, wpt.getDesc());
            values.put(COLUMN_WPT_SYM, wpt.getSym());
            values.put(COLUMN_LOG_TYPE, wpt.getType());
            if(id_cache>-1) {
                Log.i(TAG, "Parametr pairing");
                values.put(COLUMN_LOG_ID_CACHE, id_cache);
            }else if(wpt.getId_cache()>0) {
                Log.i(TAG, "Object pairing");
                values.put(COLUMN_LOG_ID_CACHE, wpt.getId_cache());
            }else {
                Log.i(TAG, "NO PAIRING!");
                values.put(COLUMN_LOG_ID_CACHE, -1);
            }
            values.put(COLUMN_WPT_ID_LIST, 0);
            id = db.insert(TB_NAME_WAYPOINT, null, values);
        db.close();
        if(id>-1)
            return true;
        else
            return false;
    }


    public boolean insertWpts(ArrayList<Waypoint> waypoints, long id_cache){
        Log.i(TAG, "insertWpts");
        SQLiteDatabase db = openHelper.getWritableDatabase();
        long id = -1;
        for(int i = 0; i < waypoints.size(); i++){
            ContentValues values = new ContentValues();
            values.put(COLUMN_WPT_LAT, waypoints.get(i).getLat());
            values.put(COLUMN_WPT_LON, waypoints.get(i).getLon());
            values.put(COLUMN_WPT_NAME, waypoints.get(i).getName());
            values.put(COLUMN_WPT_CMT, waypoints.get(i).getCmt());
            values.put(COLUMN_WPT_DESC, waypoints.get(i).getDesc());
            values.put(COLUMN_WPT_SYM, waypoints.get(i).getSym());
            values.put(COLUMN_LOG_TYPE, waypoints.get(i).getType());
            if(id_cache>-1) {
                Log.i(TAG, "Parametr pairing");
                values.put(COLUMN_LOG_ID_CACHE, id_cache);
            }else if(waypoints.get(i).getId_cache()>0) {
                Log.i(TAG, "Object pairing");
                values.put(COLUMN_LOG_ID_CACHE, waypoints.get(i).getId_cache());
            }else {
                Log.i(TAG, "NO PAIRING!");
                values.put(COLUMN_LOG_ID_CACHE, -1);
            }
            values.put(COLUMN_WPT_ID_LIST, 0);
            id = db.insert(TB_NAME_WAYPOINT, null, values);
        }

        db.close();
        if(id>-1)
            return true;
        else
            return false;
    }

    public boolean updateWpts(ArrayList<Waypoint> waypoints, long id_cache) {
        Log.i(TAG, "updateWptsCacheId");
        SQLiteDatabase db = openHelper.getWritableDatabase();
        long id = -1;
        for(int i = 0; i < waypoints.size(); i++){
            ContentValues values = new ContentValues();
            if(id_cache>-1)
                values.put(COLUMN_LOG_ID_CACHE, id_cache);
            else
                values.put(COLUMN_LOG_ID_CACHE, -1);
            values.put(COLUMN_WPT_ID_LIST, 0);
            //id = db.insert(TB_NAME_WAYPOINT, null, values);
            String[] selectionArgs = { String.valueOf(waypoints.get(i).getId()) };
            id = db.update(TB_NAME_WAYPOINT, values, COLUMN_ID + "= ?", selectionArgs);
        }

        db.close();
        if(id>-1)
            return true;
        else
            return false;

    }

    public boolean isWptFound(Waypoint wpt){
        Log.i(TAG, "updateCacheFoundStatus with id: " + wpt.getId());
        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if(wpt.getId_list()==LIST_HLEDANE){
            values.put(COLUMN_ID_LIST, LIST_NALEZENE);
        } else {
            values.put(COLUMN_ID_LIST, LIST_HLEDANE);
        }
        String[] selectionArgs = { String.valueOf(wpt.getId()) };
        int updateCount = db.update(TB_NAME_WAYPOINT, values, COLUMN_ID + "= ?", selectionArgs);
        db.close();
        if(wpt.getId_list()==LIST_HLEDANE){
            return false;
        } else {
            return true;
        }
    }

    public void close() {
        openHelper.close();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TB_NAME_LIST + " ("
                            + COLUMN_ID + " INTEGER PRIMARY KEY,"
                            + COLUMN_LIST_NAME + " TEXT NOT NULL)");
            String Insert_Data="INSERT INTO " + TB_NAME_LIST + " VALUES(0, 'Hledane')";
            db.execSQL(Insert_Data);
            Insert_Data="INSERT INTO " + TB_NAME_LIST + " VALUES(1, 'Nalezene')";
            db.execSQL(Insert_Data);

            db.execSQL("CREATE TABLE " + TB_NAME_CACHE + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_NAME + " TEXT NOT NULL,"
                    + COLUMN_CODE + " TEXT,"
                    + COLUMN_TYPE + " TEXT,"
                    + COLUMN_SIZE + " TEXT,"
                    + COLUMN_DIFFICULTY + " REAL,"
                    + COLUMN_TERRAIN + " REAL,"
                    + COLUMN_LONG + " REAL,"
                    + COLUMN_LAT + " REAL,"
                    + COLUMN_DESC + " TEXT,"
                    + COLUMN_HELP + " TEXT,"
                    + COLUMN_URL + " TEXT,"
                    + COLUMN_ID_LIST + " INTEGER,"
                    + " FOREIGN KEY ("+COLUMN_ID_LIST+") REFERENCES "+TB_NAME_LIST+" ("+COLUMN_ID_LIST+"))");

            db.execSQL("CREATE TABLE " + TB_NAME_LOG + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_LOG_DATE + " TEXT,"
                    + COLUMN_LOG_TYPE + " TEXT,"
                    + COLUMN_LOG_FINDER + " TEXT,"
                    + COLUMN_LOG_TEXT + " TEXT,"
                    + COLUMN_LOG_ID_CACHE + " INTEGER, "
                    + " FOREIGN KEY ("+COLUMN_LOG_ID_CACHE+") REFERENCES "+TB_NAME_CACHE+" ("+COLUMN_ID+"))");

            db.execSQL("CREATE TABLE " + TB_NAME_WAYPOINT + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_WPT_LAT + " REAL,"
                    + COLUMN_WPT_LON + " REAL,"
                    + COLUMN_WPT_NAME + " TEXT,"
                    + COLUMN_WPT_CMT + " TEXT,"
                    + COLUMN_WPT_DESC + " TEXT,"
                    + COLUMN_WPT_SYM + " TEXT,"
                    + COLUMN_WPT_TYPE + " TEXT,"
                    + COLUMN_WPT_ID_CACHE + " INTEGER, "
                    + COLUMN_WPT_ID_LIST + " INTEGER, "
                    + " FOREIGN KEY ("+COLUMN_WPT_ID_CACHE+") REFERENCES "+TB_NAME_CACHE+" ("+COLUMN_ID+")"
                    + " FOREIGN KEY ("+COLUMN_WPT_ID_LIST+") REFERENCES "+TB_NAME_LIST+" ("+COLUMN_ID_LIST+"))");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS caches");
            db.execSQL("DROP TABLE IF EXISTS lists");
            db.execSQL("DROP TABLE IF EXISTS logs");
            db.execSQL("DROP TABLE IF EXISTS waypoint");
            onCreate(db);
        }
    }
}
