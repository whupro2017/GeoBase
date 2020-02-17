package whu.websource;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xpath.internal.operations.Bool;
import whu.lbs.BaiduFlow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BaiduFlowTest {
    private static int[] oddYearEndDates = {
            0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };
    private static int[] evenYearEndDates = {
            0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };
    private static boolean province = true;
    private static boolean movinout = true;
    private static int beginYear = 2016;

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Command province(boolean) moveinout(boolean) beginYear(int)");
            System.exit(-1);
        }
        province = Boolean.parseBoolean(args[0]);
        movinout = Boolean.parseBoolean(args[1]);
        beginYear = Integer.parseInt(args[2]);
        BufferedReader br = new BufferedReader(new FileReader("./resources/citycode.txt"));
        String line;
        int lc = 0;
        BaiduFlow bf = new BaiduFlow();
        while ((line = br.readLine()) != null) {
            String[] fields = line.split("\t");
            if (lc++ > 0) {
                for (int y = beginYear; y < 2021; y++) {
                    String yearString = "";
                    yearString += y;
                    boolean isEven = (y % 4 == 0) ? true : false;
                    int[] endDates = (isEven ? evenYearEndDates : oddYearEndDates);
                    for (int m = 1; m < endDates.length; m++) {
                        String monthString = yearString;
                        if (m < 10) {
                            monthString += "0";
                        }
                        monthString += m;
                        for (int d = 1; d <= endDates[m]; d++) {
                            String dateString = monthString;
                            if (d < 10) {
                                dateString += "0";
                            }
                            dateString += d;
                            for (int i = 0; i < 10; i++) {
                                System.out.println(fields[0]);
                                bf.setCitycode(Integer.parseInt(fields[0]));
                                bf.setDate(dateString);
                                bf.setProvince((province ? 1 : 0));
                                bf.setTypecode(movinout ? 0 : 1);
                                String ret = bf.action();
                                JSONObject jsonObject = (JSONObject) JSONObject.parse(ret);
                                if (jsonObject != null && jsonObject.get("errmsg") != null && jsonObject.get("errmsg").equals("SUCCESS")) {
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    data.put("cityCode", fields[0].trim());
                                    data.put("cityName", fields[1].trim().substring(0, fields[1].trim().length() - 1));
                                    System.out.println(data);
                                    break;
                                }
                            }
                            System.out.println(dateString);
                        }
                    }
                }
                //System.out.println(ret);
            }
        }
        br.close();
    }
}
