package whu.poi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author AxeLai
 * @date 2019-05-14 15:09
 */
public class AddressLngLatExchanger {
    /**
     * 高德地图通过地址获取经纬度
     */
    public static Map<String, Double> addressToLngAndLag(String address) {
        Map<String, Double> map = new HashMap<String, Double>();
        //"http://restapi.amap.com/v3/geocode/geo?address=上海市东方明珠&output=JSON&key=xxxxxxxxx";
        String geturl = "http://restapi.amap.com/v3/geocode/geo?key=7de8697669288fc848e12a08f58d995e&address=" + address;
        String location = "";
        try {
            URL url = new URL(geturl);    // 把字符串转换为URL请求地址
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();// 打开连接
            connection.connect();// 连接会话
            // 获取输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {// 循环读取流
                sb.append(line);
            }
            br.close();// 关闭流
            connection.disconnect();// 断开连接
            JSONObject a = JSON.parseObject(sb.toString());
            JSONArray sddressArr = JSON.parseArray(a.get("geocodes").toString());
            if (sddressArr.size() == 0) return null;
            JSONObject c = JSON.parseObject(sddressArr.get(0).toString());
            location = c.get("location").toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("失败!");
        }
        return stringToMap(location);
    }

    public static Map<String, Double> stringToMap(String LngLat) {
        Map<String, Double> map = new HashMap<String, Double>();
        String[] strArr = LngLat.split("\\,");
        map.put("lng", Double.parseDouble(strArr[0]));
        map.put("lat", Double.parseDouble(strArr[1]));
        return map;
    }

    private static void transfer() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("./resources/texts/szstation.csv"), Charset.forName("gbk")/*StandardCharsets.UTF_8*/));
        String line;
        int count = 0;
        while ((line = br.readLine()) != null) {
            System.out.print(line + ",");
            String[] fields = line.split(",");
            String address = fields[fields.length - 1];
            if (count > 0) {
                // Map<String, Double> map = addressLngLatExchange.addressToLngAndLag(address);
                Map<String, Double> map = AddressLngLatExchanger.addressToLngAndLag("深圳市" + address);
                int tick = 0;
                for (Map.Entry<String, Double> entry : map.entrySet()) {
                    String mapKey = entry.getKey();
                    Double mapValue = entry.getValue();
                    System.out.print(mapKey + ":" + mapValue);
                    if (tick == map.size() - 1) {
                    } else {
                        System.out.print("|");
                    }
                    tick++;
                }
                System.out.println();
            } else {
                System.out.println("Positions");
            }
            count++;
        }
        br.close();
    }

    private static void verify() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("./resources/texts/szstation_poi.csv"), /*Charset.forName("gbk")*/StandardCharsets.UTF_8));
        String line;

        JSONArray records = new JSONArray();
        int count = 0;
        while ((line = br.readLine()) != null) {
            if (count++ == 0) continue;
            String fields[] = line.split(",");
            System.out.println(line + "@" + fields[fields.length - 1]);
            JSONObject obj = new JSONObject();
            obj.put("tagger", fields[1]);
            obj.put("phone", fields[2]);
            obj.put("street", fields[3]);
            obj.put("address", fields[4]);
            JSONArray position = new JSONArray();
            String xy[] = fields[5].split("\\|");
            position.add(Float.parseFloat(xy[0].split(":")[1]));
            position.add(Float.parseFloat(xy[1].split(":")[1]));
            obj.put("position", position);
            records.add(obj);
        }
        System.out.println(records.toJSONString());
        br.close();
    }

    public static void main(String[] args) throws IOException {
        /*// GD2 addressLngLatExchange = new GD2();
        String address = "浙江杭州阿里巴巴大厦";
        // Map<String, Double> map = addressLngLatExchange.addressToLngAndLag(address);
        Map<String, Double> map = AddressLngLatExchanger.addressToLngAndLag(address);
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String mapKey = entry.getKey();
            Double mapValue = entry.getValue();
            System.out.println(mapKey + ":" + mapValue);
        }*/

        transfer();
        verify();
    }
}