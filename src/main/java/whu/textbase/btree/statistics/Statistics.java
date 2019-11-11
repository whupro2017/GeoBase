package whu.textbase.btree.statistics;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Statistics {

    public static double getAvgResultNum(String path) {
        String line = "";
        int count = 0, total = 0;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "gb2312"));
            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
                total += Integer.valueOf(line.trim());

                count++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return total * 1.0 / count;
    }

    public static int getInputDataLines(String path) {
        String line = "";
        int count = 0;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "gb2312"));
            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
                if (line.startsWith("Q")) {
                    count++;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return count;
    }

}
