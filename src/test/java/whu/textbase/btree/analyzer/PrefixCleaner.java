package whu.textbase.btree.analyzer;

import whu.utils.ConsoleColors;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefixCleaner {
    private static final boolean pt = true;
    private static final boolean redcued = true;
    private static final String path = "./resources/texts/sz_poi/housingtencent.csv";

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
            String patternString = "[0-9]+|\\(|[A-Z]+|-|一|二|三|四|五|六|七|八|九|十";
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
            if (key.length() > 1 && !key.equals("城市") && !key.equals("深圳市") && !key.equals("公寓") && !key.equals("宝安区") && !key.equals("住宅区") && !key.equals("坪山") && !key.equals("坪山区") && !key.equals("深圳") && !key.equals("龙岗区"))
                map.put(key, line);
        }
        br.close();
        int count = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter("./resources/texts/sz_poi/placepair-R.csv"));
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
