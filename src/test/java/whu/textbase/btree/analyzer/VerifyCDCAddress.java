package whu.textbase.btree.analyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VerifyCDCAddress {
    public static void main(String argv[]) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("./resources/texts/sz_poi/placepair-R.csv"));
        String source = "牛轭岭小区56号楼";
        List<String[]> targets = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            targets.add(line.split(","));
        }
        br.close();
        BufferedReader src = new BufferedReader(new FileReader("./resources/texts/sz_poi/placepair.csv"));
        while ((line = src.readLine()) != null) {
            for (String[] target : targets) {
                if (line.contains(target[0])) {
                    //System.out.println(target[0]);
                    for (int i = 0; i < target.length; i++) {
                        System.out.println(target[i]);
                    }
                }
            }
        }
        src.close();
    }
}
