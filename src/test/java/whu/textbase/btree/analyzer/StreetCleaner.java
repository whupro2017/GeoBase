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
            for (int idx = 2; idx <= 3; idx++) {
                String dummyString = "[0-9]+|\\(|[A-Z]+|-|一|二|三|四|五|六|七|八|九|十|业区|龙岗|业园|科技|国际|大厦" +
                        "|村委|街道|龙岗|中心|罗湖|惠州|东莞|东门|国际|南山|山区|岗区|深圳|圳市|田区|安区|工业|龙华|横岗" +
                        "|新区|家园|福田|湖北|社区|宝安";//"[0-9]+|\\(|[A-Z]+|-";
                Pattern dummy = Pattern.compile(dummyString);
                String patternString = "\\[|\\(";//"[0-9]+|\\(|[A-Z]+|-|一|二|三|四|五|六|七|八|九|十";
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(fields[idx]);
                int pos = fields[idx].length();
                if (matcher.find()) {
                    pos = matcher.start();
                }
                String key = fields[idx].substring(0, pos);
                if (redcued) {
                    String[] adds = key.split("·");
                    if (adds.length > 1)
                        key = adds[adds.length - 1];
                }
                patternString = "工业园|科技园|商业城|好邻居|水泥厂";
                pattern = Pattern.compile(patternString);
                matcher = pattern.matcher(key);
                String tiny;
                if (matcher.find()) {
                    pos = matcher.start();
                    if (pos < 2) System.out.println(key);
                    else {
                        tiny = key.substring(pos - 2, pos);
                        if (!dummy.matcher(tiny).find()) map.put(tiny, line);
                    }
                } else {
                    patternString = "数码|街道|大厦|桥头|新圩|公司|花院|小学|大学|中学|学院|时代|横巷|酒店|广场|路口|大楼" +
                            "|新村|旧村|名苑|公寓|宿舍|老村|小区|花园|上村|下村|东村|北村|南村|西村|社区|市场|半山|商住" +
                            "|百货|大街|西坊|东坊|南坊|北坊|公馆|南区|东区|西区|北区|南巷|北巷|东巷|西巷|医院|居委|山庄";
                    pattern = Pattern.compile(patternString);
                    matcher = pattern.matcher(key);
                    if (matcher.find()) {
                        pos = matcher.start();
                        if (pos < 2) System.out.println(key);
                        else {
                            tiny = key.substring(pos - 2, pos);
                            if (!dummy.matcher(tiny).find() && !pattern.matcher(tiny).find()) map.put(tiny, line);
                        }
                    } else {
                        patternString = "村|巷|府|镇|岭|坊|湖|埔|屯|阁|洞|坪|井|组|排|沟|家|路|厂|城";
                        pattern = Pattern.compile(patternString);
                        matcher = pattern.matcher(key);
                        if (matcher.find()) {
                            pos = matcher.start();
                            if (pos < 2) System.out.println(key);
                            else {
                                tiny = key.substring(pos - 2, pos);
                                if (!dummy.matcher(tiny).find() && !pattern.matcher(tiny).find()) map.put(tiny, line);
                            }
                        }
                    }
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
                        if (!dummy.matcher(add).find()) map.put(add, line);
            }
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
