package whu.textbase.btree.analyzer;

import whu.textbase.btree.utils.btree.BtreeClusterSp;

import java.io.*;
import java.util.*;

public class StreetMissing {
    public static void main(String argv[]) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader("./resources/texts/sz_poi/street-R.csv"));
        Set<String> set = new HashSet<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            if (fields.length > 0 && fields[0].length() > 0) {
                set.add(fields[0]);
            }
        }
        br.close();

        int count = 0;
        int total = -1;
        br = new BufferedReader(new FileReader("./resources/texts/corpus/missing.txt"));
        BufferedWriter bm = new BufferedWriter(new FileWriter("./resources/texts/corpus/match.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("./resources/texts/corpus/unmatch.txt"));
        Map<String, Integer> freq = new HashMap<>();
        while ((line = br.readLine()) != null) {
            total++;
            boolean found = false;
            String hitkey = "";
            for (String key : set) {
                if (line.contains(key)) {
                    hitkey = key;
                    found = true;
                    if (!freq.containsKey(hitkey)) freq.put(hitkey, 0);
                    freq.put(hitkey, freq.get(hitkey) + 1);
                    break;
                }
            }
            if (!found) bw.write(line + " " + count++ + " " + total + "\n");//System.out.println(line + " " + count++);
            else bm.write(line + "\t" + hitkey + " " + total + "\n");
        }
        br.close();
        bm.close();
        bw.close();

        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        for (Map.Entry<String, Integer> l : freq.entrySet()) list.add(l);
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                Integer fa = a.getValue(), fb = b.getValue();
                return fa.compareTo(fb);
            }
        });
        for (Map.Entry<String, Integer> e : list) System.out.println(e.getKey() + " " + e.getValue());
    }
}
