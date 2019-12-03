package whu.textbase.btree.analyzer;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

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
        br = new BufferedReader(new FileReader("./resources/texts/corpus/missing.txt"));
        while ((line = br.readLine()) != null) {
            boolean found = false;
            for (String key : set) {
                if (line.contains(key)) {
                    found = true;
                    break;
                }
            }
            if (!found) System.out.println(line + " " + count++);
        }
        br.close();
    }
}
