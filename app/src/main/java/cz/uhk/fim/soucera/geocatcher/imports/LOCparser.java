package cz.uhk.fim.soucera.geocatcher.imports;

import android.util.Log;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cz.uhk.fim.soucera.geocatcher.Cache;


public class LOCparser {
    private static String TAG = LOCparser.class.getName();
    private static ArrayList<Cache> caches;

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

    public static ArrayList<Cache> getCachceFromFile(String path, String name){
        try {
            Log.i(TAG, "GetCacheFromFileLOC!");

            File fXmlFile;
            String Fpath = path;
            fXmlFile = new File(Fpath);
            if (fXmlFile.exists()) {
                Log.i(TAG, "Default version Fpath(getData.getPath) works!");
                caches = readFile(fXmlFile);
            } else {
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
            }
            System.out.println("String xmlFile: " + fXmlFile.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return caches;
    }

    private static ArrayList<Cache> readFile(File fXmlFile) {
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            caches = new ArrayList<>();
            Cache cache;
            NodeList nList = doc.getElementsByTagName("waypoint");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                //System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    NodeList nodeChilds = nNode.getChildNodes();

                    /*
                    for (int temp2 = 0; temp2 < nodeChilds.getLength(); temp2++) {
                        System.out.println(temp2 + " " + nodeChilds.item(temp2).getNodeName());
                    }
                    */

                    Node name = nodeChilds.item(1);
                    Element eName = (Element) name;
                    Node coord = nodeChilds.item(3);
                    Element eCoord = (Element) coord;

                    /*
                    System.out.println("Cache id: " + eName.getAttribute("id"));
                    System.out.println("Cache name: " + getCharacterDataFromElement(eName));
                    System.out.println("lat: " + eCoord.getAttribute("lat"));
                    System.out.println("lon: " + eCoord.getAttribute("lon"));
                    System.out.println("Type: " + eElement.getElementsByTagName("type").item(0).getTextContent());
                    System.out.println("Details: " + eElement.getElementsByTagName("link").item(0).getTextContent());
                    */
                    cache = new Cache();
                    cache.setCode(eName.getAttribute("id"));
                    cache.setName(getCharacterDataFromElement(eName));
                    cache.setLat(Double.parseDouble(eCoord.getAttribute("lat")));
                    cache.setLon(Double.parseDouble(eCoord.getAttribute("lon")));
                    cache.setType(eElement.getElementsByTagName("type").item(0).getTextContent());
                    cache.setUrl(eElement.getElementsByTagName("link").item(0).getTextContent());
                    caches.add(cache);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return caches;
    }


    private static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    public static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }


}
