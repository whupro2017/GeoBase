package whu.textbase.btree.statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IdfCalculate {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        //        String path = "D:\\bplustree\\stanfordSt\\stanfordSt~\\FreqStatistics.txt";
        String path = "D:\\bplustree\\ImdbSt\\ImdbSt~\\FreqStatistics.txt";
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String temp[] = line.split(" ");
                int id = Integer.valueOf(temp[0]);
                int count = Integer.valueOf(temp[2]);
                if (!map.containsKey(count)) {
                    map.put(count, 1);
                } else {
                    map.put(count, map.get(count) + 1);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int i = 1; i <= 1000; i++) {
            if (!map.containsKey(i)) {
                System.out.println(0);
            } else {
                System.out.println(map.get(i));
            }
        }
    }

}
