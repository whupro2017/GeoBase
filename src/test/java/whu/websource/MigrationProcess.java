package whu.websource;

import java.io.*;
import java.util.*;

public class MigrationProcess {
    public static void main(String[] args) throws IOException {
        InputStreamReader fi = new InputStreamReader(new FileInputStream("./resources/migration/shenzhen2020-01-20.csv"), "gbk");
        BufferedReader br = new BufferedReader(fi);
        String line;
        List<Map<String, Double>> listmap = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            listmap.add(new HashMap<>());
        }
        int lc = 0;
        Set<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            String[] fields = line.split(",");
            for (int i = 0; i < 12; i++) {
                if (!fields[2 * i].isEmpty()) {
                    listmap.get(i).put(fields[2 * i].trim(), Double.parseDouble(fields[2 * i + 1]));
                    set.add(fields[2 * i].trim());
                }
            }
        }
        br.close();
        fi.close();
        for (String e : set) {
            System.out.print(e + "\t");
            for (int i = 0; i < 12; i++) {
                if (listmap.get(i).containsKey(e)) {
                    System.out.print(listmap.get(i).get(e) + "\t");
                } else {
                    System.out.print(0 + "\t");
                }
            }
            System.out.println();
        }
    }
}
