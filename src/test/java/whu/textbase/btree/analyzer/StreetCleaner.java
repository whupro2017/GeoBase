package whu.textbase.btree.analyzer;

import whu.utils.ConsoleColors;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreetCleaner {
    private static final boolean pt = true;
    private static final boolean redcued = true;
    private static final String path = "./resources/texts/sz_poi/infrastructure.csv";

    public static void main(String argv[]) throws IOException {
        String filter = null;
        Map<String, String> map = new HashMap<>();
        if (argv.length > 0) {
            filter = argv[0];
        }
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            if (filter != null && !line.contains(filter)) {
                continue;
            }
            //String patternString = "[0-9]+|\\(|[A-Z]+|-";
            String patternString = "\\[|\\(";//"[0-9]+|\\(|[A-Z]+|-|一|二|三|四|五|六|七|八|九|十";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(fields[2]);
            int pos = fields[2].length();
            if (matcher.find()) {
                pos = matcher.start();
            }
            String key = fields[2].substring(0, pos);
            if (redcued) {
                String[] adds = key.split("·");
                if (adds.length > 1)
                    key = adds[adds.length - 1];
            }
            List<String> keys = new ArrayList<>();
            patternString = "交叉口";
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(key);
            if (matcher.find()) {
                pos = matcher.start();
                key = key.substring(0, pos);
            }
            patternString = "与";
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(key);
            if (matcher.find()) {
                pos = matcher.start();
                keys.add(key.substring(0, pos));
                keys.add(key.substring(pos + 1));
            }
            for (String add : keys)
                if (add.length() > 1 && !add.equals("城市") && !add.equals("深圳市") && !add.equals("公寓") && !add.equals("宝安区") && !add.equals("住宅区") && !add.equals("坪山") && !add.equals("坪山区") && !add.equals("深圳") && !add.equals("龙岗区"))
                    map.put(add, line);
        }
        br.close();
        int count = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter("./resources/texts/sz_poi/street-R.csv"));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (pt) {
                System.out.println(ConsoleColors.BLUE + entry.getKey() + "\t" + ConsoleColors.RED + entry.getValue() + ConsoleColors.RESET);
            }
            count++;
            bw.write(entry.getKey() + "," + entry.getValue() + '\n');
        }
        bw.close();
        System.out.println(count);
    }
}
