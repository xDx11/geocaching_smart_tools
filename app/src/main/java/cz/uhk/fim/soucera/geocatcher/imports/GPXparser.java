package cz.uhk.fim.soucera.geocatcher.imports;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cz.uhk.fim.soucera.geocatcher.Cache;
import cz.uhk.fim.soucera.geocatcher.logs.Log_Cache;
import cz.uhk.fim.soucera.geocatcher.utils.BOMSkipper;
import cz.uhk.fim.soucera.geocatcher.waypoints.Waypoint;

public class GPXparser {
    private static String TAG = GPXparser.class.getName();
    private static ArrayList<Cache> caches;
    private static ArrayList<Waypoint> waypoints;
    private static ArrayList<ArrayList<Waypoint>> wptsGroups;

    private static String[] getPhysicalPaths() {
        return new String[]{
                "/storage/sdcard0",
                "/storage/sdcard1",                 //Motorola Xoom
                "/storage/extsdcard",               //Samsung SGS3
                "/storage/sdcard0/external_sdcard", //User request
                "/mnt/extsdcard",
                "/mnt/sdcard/external_sd",          //Samsung galaxy family
                "/mnt/external_sd",
                "/mnt/media_rw/sdcard1",            //4.4.2 on CyanogenMod S3
                "/removable/microsd",               //Asus transformer prime
                "/mnt/emmc",
                "/storage/external_SD",             //LG
                "/storage/ext_sd",                  //HTC One Max
                "/storage/removable/sdcard1",       //Sony Xperia Z1
                "/data/sdext",
                "/data/sdext2",
                "/data/sdext3",
                "/data/sdext4",
                "/sdcard1",                         //Sony Xperia Z
                "/sdcard2",                         //HTC One M8s
                "/storage/microsd"                  //ASUS ZenFone 2
        };
    }

    public static ImportObject getCacheFromFile(String path, String name){
            try {
                Log.i(TAG, "GetCacheFromFileGPX!");
                caches = null;
                waypoints = null;
                File fXmlFile;
                String Fpath = path;
                fXmlFile = new File(Fpath);
                if (fXmlFile.exists()) { // 1. zpusob importu
                    Log.i(TAG, "Default version Fpath(getData.getPath) works!");
                    caches = readFile(fXmlFile);
                } else { // 2. zpusob importu, zjisteni prefixu nejznamnejsich moznosti
                    Log.e(TAG, "Default version Fpath(getData.getPath) doesn't work! Now find in a root of SD card!");
                    String[] optionsStorages = getPhysicalPaths();
                    for(int i = 0; i < optionsStorages.length; i++){
                        if(path.contains(optionsStorages[i])){
                            Log.i(TAG, "Found prefix of external storage!");
                            String environmentExternalPart = optionsStorages[i];
                            String fileName = name;
                            fXmlFile = new File(environmentExternalPart, fileName);
                            if(fXmlFile.exists()){
                                Log.i(TAG, "File is found in root of SD card!");
                                caches = readFile(fXmlFile);
                            } else {
                                Log.e(TAG, "Prefix found, but file NOT found!");
                            }
                            break;
                        } else {
                            Log.e(TAG, "Not Found prefix of external storage");
                        }
                    }
                    if(caches==null){ // 3. zpusob = nouzovy zpusob importu z korenove slozky SD karty
                        Log.i(TAG, "Emergency version of import - name from SD_ROOT");
                        String middlePartOfStorage = getRemovebleSDCardPath();
                        String[] path_parts = middlePartOfStorage.split("/");
                        String middlePart = path_parts[path_parts.length-1];

                        String[] firstPartOfStorage = path.split("/");
                        String firstPart = firstPartOfStorage[1];
                        //("First part: " + firstPart);
                        String fileName = firstPartOfStorage[firstPartOfStorage.length-1];
                        String pathStorage;
                        if(!firstPart.equals(middlePart)){
                            pathStorage = "/"+firstPart+"/"+middlePart;
                        } else {
                            pathStorage = "/"+firstPart;
                        }
                        Log.i(TAG, "Emergency path: "+ pathStorage + "/" + fileName);
                        fXmlFile = new File(pathStorage, fileName);
                        caches = readFile(fXmlFile);
                    }
                }
                Log.i(TAG,"Path xmlFile: " + fXmlFile.getPath());
                String fileWpts = fXmlFile.getPath();
                fileWpts = fileWpts.replace(".gpx", "-wpts.gpx");
                File fXmlFileWpts = new File(fileWpts);
                if(fXmlFileWpts.exists()){
                    Log.i(TAG, "fXmlFileWpts exist!");
                    waypoints = readFileWpts(fXmlFileWpts);
                    processingWpts();
                } else {
                    wptsGroups = null;
                    waypoints = null;
                    try {
                        waypoints = readFileWpts(fXmlFile);
                        if(waypoints.size()==0)
                            waypoints = null;
                    } catch (Exception e){
                        e.printStackTrace();
                        waypoints = null;
                    }

                }
            } catch (Exception e){
                e.printStackTrace();
            }
        if(wptsGroups != null) {
            return new ImportObject(null, caches, wptsGroups);
        } else if(waypoints != null){
            return new ImportObject(waypoints, caches);
        } else {
            return new ImportObject(null, caches);
        }
    }

    private static void processingWpts(){
        int cachesSize = caches.size();
        String previousCode = null;
        String currentCode;
        int numberGroupOfWaypoints = 1;
        ArrayList<String> groupCode = new ArrayList<>();
        wptsGroups = new ArrayList<>();
        ArrayList<Waypoint> oneGroup = new ArrayList<>();

        for(int i = 0; i < waypoints.size(); i++){
            if(previousCode == null){
                previousCode = waypoints.get(i).getName().substring(Math.max(0,waypoints.get(i).getName().length() - 5));
                groupCode.add(previousCode);
            }
            currentCode = waypoints.get(i).getName().substring(Math.max(0,waypoints.get(i).getName().length() - 5));
            if(previousCode.equals(currentCode)){
                oneGroup.add(waypoints.get(i));
                if(i+1 == waypoints.size())
                    wptsGroups.add(oneGroup);
            } else {
                previousCode = currentCode;
                groupCode.add(previousCode);
                numberGroupOfWaypoints += 1;
                wptsGroups.add(oneGroup);
                oneGroup = new ArrayList<>();
                oneGroup.add(waypoints.get(i));
            }
        }
        //waypoints.clear();
        if(cachesSize != numberGroupOfWaypoints){
            wptsGroups = null;
        }
    }

    private static ArrayList<Cache> readFile(File fXmlFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fXmlFile), "UTF-8"));
            BOMSkipper.skip(br);
            Document doc = dBuilder.parse(new InputSource(br));

            caches = new ArrayList<>();
            Cache cache;

            NodeList nList = doc.getElementsByTagName("wpt");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    NodeList nodeChilds = nNode.getChildNodes();
                        /*
                        System.out.println("lat: " + eElement.getAttribute("lat"));
                        System.out.println("lon: " + eElement.getAttribute("lon"));
                        System.out.println("code: " + eElement.getElementsByTagName("name").item(0).getTextContent());
                        System.out.println("name: " + eElement.getElementsByTagName("groundspeak:name").item(0).getTextContent());
                        System.out.println("url: " + eElement.getElementsByTagName("url").item(0).getTextContent());
                        System.out.println("Type: " + eElement.getElementsByTagName("groundspeak:type").item(0).getTextContent());
                        System.out.println("difficulty: " + eElement.getElementsByTagName("groundspeak:difficulty").item(0).getTextContent());
                        System.out.println("terrain: " + eElement.getElementsByTagName("groundspeak:terrain").item(0).getTextContent());
                        System.out.println("Velikost: " + eElement.getElementsByTagName("groundspeak:container").item(0).getTextContent());
                        System.out.println("description: " + getCharacterDataFromElement(eDesc));
                        System.out.println("help: " + eElement.getElementsByTagName("groundspeak:encoded_hints").item(0).getTextContent());
                        System.out.println("----------------------------");
                        */
                    if(eElement.getElementsByTagName("cmt").getLength() == 0){
                        cache = new Cache();
                        cache.setCode(eElement.getElementsByTagName("name").item(0).getTextContent());
                        cache.setName(eElement.getElementsByTagName("groundspeak:name").item(0).getTextContent());
                        cache.setUrl(eElement.getElementsByTagName("url").item(0).getTextContent());
                        cache.setType(eElement.getElementsByTagName("groundspeak:type").item(0).getTextContent());
                        cache.setSize(eElement.getElementsByTagName("groundspeak:container").item(0).getTextContent());
                        cache.setDifficulty(Double.parseDouble(eElement.getElementsByTagName("groundspeak:difficulty").item(0).getTextContent()));
                        cache.setTerrain(Double.parseDouble(eElement.getElementsByTagName("groundspeak:terrain").item(0).getTextContent()));
                        cache.setLat(Double.parseDouble(eElement.getAttribute("lat")));
                        cache.setLon(Double.parseDouble(eElement.getAttribute("lon")));
                        cache.setHelp(eElement.getElementsByTagName("groundspeak:encoded_hints").item(0).getTextContent());
                        String shortDescription = eElement.getElementsByTagName("groundspeak:short_description").item(0).getTextContent();
                        String longDescription = eElement.getElementsByTagName("groundspeak:long_description").item(0).getTextContent();
                        cache.setDesc(shortDescription + "\n" + longDescription);

                        NodeList test_logs = doc.getElementsByTagName("groundspeak:logs");
                        NodeList groundspeak_logs = test_logs.item(temp).getChildNodes();

                        if(groundspeak_logs.getLength()>5){
                            ArrayList<Log_Cache> logsList = new ArrayList<>();
                            Log_Cache log_cache;
                            for(int i = 1; i < 10; i+=2){
                                Node log = groundspeak_logs.item(i);
                                Element eLog = (Element) log;

                                log_cache = new Log_Cache();
                                log_cache.setDate(eLog.getElementsByTagName("groundspeak:date").item(0).getTextContent());
                                log_cache.setType(eLog.getElementsByTagName("groundspeak:type").item(0).getTextContent());
                                log_cache.setFinder(eLog.getElementsByTagName("groundspeak:finder").item(0).getTextContent());
                                log_cache.setText(eLog.getElementsByTagName("groundspeak:text").item(0).getTextContent());
                                logsList.add(log_cache);

                        /*
                        System.out.println(log_cache.getDate());
                        System.out.println(log_cache.getType());
                        System.out.println(log_cache.getFinder());
                        System.out.println(log_cache.getText());
                        System.out.println("/////////////////////////////////////////");
                        */
                            }
                            cache.setLogs(logsList);
                        }


                    /*
                    System.out.println(eElement.getElementsByTagName("groundspeak:short_description").item(0).getTextContent());
                    System.out.println(getCharacterDataFromElement(eDesc));
                    System.out.println(cache.getDesc());

                    for (int temp2 = 0; temp2 < nodeChilds.getLength(); temp2++) {
                        System.out.println(temp2 + " " + nodeChilds.item(temp2).getNodeName());
                    }


                    for (int temp3 = 0; temp3 < groundChilds.getLength(); temp3++) {
                        System.out.println(temp3 + " " + groundChilds.item(temp3).getNodeName());
                    }

                    for (int temp3 = 0; temp3 < groundspeak_logs.getLength(); temp3++) {
                        System.out.println(temp3 + " " + groundspeak_logs.item(temp3).getNodeName());
                    }
                    */

                        caches.add(cache);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return caches;
    }

    private static ArrayList<Waypoint> readFileWpts(File fXmlFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fXmlFile), "UTF-8"));
            BOMSkipper.skip(br);
            Document doc = dBuilder.parse(new InputSource(br));

            waypoints = new ArrayList<>();
            Waypoint waypoint;

            NodeList nList = doc.getElementsByTagName("wpt");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    NodeList nodeChilds = nNode.getChildNodes();
                    if(eElement.getElementsByTagName("groundspeak:name").getLength()==0){
                        waypoint = new Waypoint();
                        waypoint.setName(eElement.getElementsByTagName("name").item(0).getTextContent());
                        waypoint.setCmt(eElement.getElementsByTagName("cmt").item(0).getTextContent());
                        waypoint.setDesc(eElement.getElementsByTagName("desc").item(0).getTextContent());
                        waypoint.setSym(eElement.getElementsByTagName("sym").item(0).getTextContent());
                        waypoint.setType(eElement.getElementsByTagName("type").item(0).getTextContent());
                        waypoint.setLat(Double.parseDouble(eElement.getAttribute("lat")));
                        waypoint.setLon(Double.parseDouble(eElement.getAttribute("lon")));
                        waypoints.add(waypoint);
                    }

                }
            }
            for(int i = 0; i<waypoints.size(); i++){
                System.out.println("WPT: " + waypoints.get(i).getDesc());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return waypoints;
    }

    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getRemovebleSDCardPath() throws IOException {
        String sdpath = null;
        File file = new File("/sys/class/block/");
        File[] files = file.listFiles(new MmcblkFilter("mmcblk\\d$"));
        String sdcardDevfile = null;
        for (File mmcfile : files) {
            Log.d("SDCARD", mmcfile.getAbsolutePath());
            File scrfile = new File(mmcfile, "device/scr");
            if (scrfile.exists()) {
                sdcardDevfile = mmcfile.getName();
                Log.d("SDCARD", mmcfile.getName());
                break;
            }
        }
        if (sdcardDevfile == null) {
            return null;
        }
        FileInputStream is;
        BufferedReader reader;

        files = file.listFiles(new MmcblkFilter(sdcardDevfile + "p\\d+"));
        String deviceName = null;
        if (files.length > 0) {
            Log.d("SDCARD", files[0].getAbsolutePath());
            File devfile = new File(files[0], "dev");
            if (devfile.exists()) {
                FileInputStream fis = new FileInputStream(devfile);
                reader = new BufferedReader(new InputStreamReader(fis));
                String line = reader.readLine();
                deviceName = line;
            }
            Log.d("SDCARD", "" + deviceName);
            if (deviceName == null) {
                return null;
            }
            Log.d("SDCARD", deviceName);

            final File mountFile = new File("/proc/self/mountinfo");

            if (mountFile.exists()) {
                is = new FileInputStream(mountFile);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    // Log.d("SDCARD", line);
                    // line = reader.readLine();
                    // Log.d("SDCARD", line);
                    String[] mPonts = line.split("\\s+");
                    if (mPonts.length > 6) {
                        if (mPonts[2].trim().equalsIgnoreCase(deviceName)) {
                            if (mPonts[4].contains(".android_secure")
                                    || mPonts[4].contains("asec")) {
                                continue;
                            }
                            sdpath = mPonts[4];
                            Log.d("SDCARD", mPonts[4]);
                        }
                    }
                }
            }
        }
        return sdpath;
    }

    public static class MmcblkFilter implements FilenameFilter {
        private String pattern;

        public MmcblkFilter(String pattern) {
            this.pattern = pattern;

        }

        @Override
        public boolean accept(File dir, String filename) {
            if (filename.matches(pattern)) {
                return true;
            }
            return false;
        }

    }

}
