package whu.websource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

class Helper extends TimerTask {
    private static HashMap<String, Integer> provinceConfirmed = new HashMap<>();
    private static final HashMap<String, Integer> provinceSuspected = new HashMap<>();
    private static final HashMap<String, Integer> provinceCured = new HashMap<>();
    private static final HashMap<String, Integer> provinceDeath = new HashMap<>();
    private static final HashMap<String, Integer> cityConfirmed = new HashMap<>();
    private static final HashMap<String, Integer> citySuspected = new HashMap<>();
    private static final HashMap<String, Integer> cityCured = new HashMap<>();
    private static final HashMap<String, Integer> cityDeath = new HashMap<>();
    private static final HashMap<String, HashSet<String>> proviceCities = new HashMap<>();
    private static boolean firstRun = true;

    private static String httpRequest(String requestUrl) {
        StringBuffer buffer = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        InputStream inputStream = null;
        HttpURLConnection httpURLConn = null;

        try {
            URL url = new URL(requestUrl);
            httpURLConn = (HttpURLConnection) url.openConnection();
            httpURLConn.setDoInput(true);
            httpURLConn.setRequestMethod("GET");

            inputStream = httpURLConn.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            buffer = new StringBuffer();
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpURLConn != null) {
                httpURLConn.disconnect();
            }
        }
        return buffer.toString();
    }

    private static boolean tryUpdate(HashMap<String, Integer> map, String key, Integer value, String province) {
        if (!map.containsKey(key)) {
            map.put(key, value);
            if (province == null)
                proviceCities.put(key, new HashSet<>());
            else
                proviceCities.get(province).add(key);
            return true;
        } else if (map.get(key) != value) {
            map.put(key, value);
            return true;
        }
        return false;
    }

    private static boolean htmlFiter(String html) {
        boolean modified = false;
        StringBuffer buffer = new StringBuffer();
        try {
            Document doc = Jsoup.parse(html, "UTF-8");
            //Elements allAreas = allAreas.first();
            Element area = doc.getElementById("getAreaStat");
            String innert = area.childNode(0).toString();
            int begin = innert.indexOf("=");
            int end = innert.lastIndexOf("}catch");
            String inner = innert.substring(begin + 1, end);
            JsonElement object = JsonParser.parseString(inner);
            for (JsonElement province : object.getAsJsonArray()) {
                String provinceShortName = province.getAsJsonObject().get("provinceShortName").getAsString();
                int provinceConfirm = province.getAsJsonObject().get("confirmedCount").getAsInt();
                modified = tryUpdate(provinceConfirmed, provinceShortName, provinceConfirm, null);
                int provinceSuspect = province.getAsJsonObject().get("suspectedCount").getAsInt();
                modified = tryUpdate(provinceSuspected, provinceShortName, provinceSuspect, null);
                int provinceCure = province.getAsJsonObject().get("curedCount").getAsInt();
                modified = tryUpdate(provinceCured, provinceShortName, provinceCure, null);
                int provinceDead = province.getAsJsonObject().get("deadCount").getAsInt();
                modified = tryUpdate(provinceDeath, provinceShortName, provinceDead, null);
                JsonElement cities = province.getAsJsonObject().get("cities");
                for (JsonElement city : cities.getAsJsonArray()) {
                    String cityName = city.getAsJsonObject().get("cityName").getAsString();
                    int cityConfirm = city.getAsJsonObject().get("confirmedCount").getAsInt();
                    modified = tryUpdate(cityConfirmed, cityName, cityConfirm, provinceShortName);
                    int citySuspect = city.getAsJsonObject().get("suspectedCount").getAsInt();
                    modified = tryUpdate(citySuspected, cityName, citySuspect, provinceShortName);
                    int cityCure = city.getAsJsonObject().get("curedCount").getAsInt();
                    modified = tryUpdate(cityCured, cityName, cityCure, provinceShortName);
                    int cityDead = city.getAsJsonObject().get("deadCount").getAsInt();
                    modified = tryUpdate(cityDeath, cityName, cityDead, provinceShortName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modified;
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public void dumpSlice() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String cdate = formatter.format(date);
        provinceConfirmed = sortByValues(provinceConfirmed);
        if (firstRun) {
            String title = ",";
            for (String pName : provinceConfirmed.keySet()) {
                title += pName;
                title += ",";
                for (String cName : proviceCities.get(pName)) {
                    title += cName;
                    title += ",";
                }
            }
            System.out.println(title);
            firstRun = false;
        }
        {
            String confirm = cdate + ",";
            for (String pName : provinceConfirmed.keySet()) {
                confirm += provinceConfirmed.get(pName);
                confirm += ",";
                for (String cName : proviceCities.get(pName)) {
                    confirm += cityConfirmed.get(cName);
                    confirm += ",";
                }
            }
            System.out.println(confirm);
        }
        {
            String suspect = cdate + ",";
            for (String pName : provinceConfirmed.keySet()) {
                suspect += provinceSuspected.get(pName);
                suspect += ",";
                for (String cName : proviceCities.get(pName)) {
                    suspect += citySuspected.get(cName);
                    suspect += ",";
                }
            }
            System.out.println(suspect);
        }
        {
            String cured = cdate + ",";
            for (String pName : provinceConfirmed.keySet()) {
                cured += provinceCured.get(pName);
                cured += ",";
                for (String cName : proviceCities.get(pName)) {
                    cured += cityCured.get(cName);
                    cured += ",";
                }
            }
            System.out.println(cured);
        }
        String dead = cdate + ",";
        for (String pName : provinceConfirmed.keySet()) {
            dead += provinceDeath.get(pName);
            dead += ",";
            for (String cName : proviceCities.get(pName)) {
                dead += cityDeath.get(cName);
                dead += ",";
            }
        }
        System.out.println(dead);
    }

    public void run() {
        String content = httpRequest("http://3g.dxy.cn/newh5/view/pneumonia");
        if (htmlFiter(content)) {
            dumpSlice();
        }
        //System.out.print(content);
    }
}

public class DxyCrawler {

    public static void main(String[] args) {
        Timer timer = new Timer();
        TimerTask task = new Helper();

        timer.schedule(task, 2000, 120000);
    }
}
