package whu.textbase.btree.analyzer;

import whu.utils.ConsoleColors;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class JiebaTokenizerTest {
    private static final boolean pt = true;
    private static final boolean nonsubstr = true;
    private static final String path = "./resources/texts/sz_poi/housingtencent.csv";

    private static class comp implements Comparator<String> {
        public int compare(String o1, String o2) {
            if (o1.length() > o2.length()) {
                return 1;
            } else if (o1.length() < o2.length()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public static void main(String argv[]) throws IOException {
        String filter = null;
        if (argv.length > 0) {
            filter = argv[0];
        }
        BufferedReader br = new BufferedReader(new FileReader(path));
        JiebaTokenizer jt = new JiebaTokenizer();
        String line;
        int ln = 0;
        int hit = 0;
        long begin = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
            if (filter != null && !line.contains(filter)) {
                ln++;
                continue;
            }
            String[] fields = line.split(",");
            String[] tokens = jt.tokenize(fields[3]);
            Arrays.sort(tokens, new comp());
            if (pt) {
                System.out.print(line + "\t");
                for (int i = 0; i < tokens.length; i++) {
                    System.out.print(ConsoleColors.BLUE + tokens[i] + "\t" + ConsoleColors.RESET);
                }
            }
            if (nonsubstr) {
                BitSet reserved = new BitSet();
                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].length() == 0 || tokens[i].equals(" ")) {
                        continue;
                    }
                    reserved.set(i);
                    for (int j = i + 1; j < tokens.length; j++) {
                        if (tokens[j].contains(tokens[i])) {
                            reserved.clear(i);
                            break;
                        }
                    }
                }
                String[] output = new String[reserved.cardinality()];
                int i = 0;
                int idx = 0;
                while (i < tokens.length) {
                    i = reserved.nextSetBit(i);
                    output[idx++] = tokens[i++];
                }
                tokens = output;
            }
            if (pt) {
                for (int i = 0; i < tokens.length; i++) {
                    System.out.print(ConsoleColors.RED + tokens[i] + "\t" + ConsoleColors.RESET);
                }
                System.out.println("\n");
            }
            ln++;
            hit++;
        }
        System.out.println((filter != null ? filter : null) + "\t" + (System.currentTimeMillis() - begin) + "\t" + ln + "\t" + hit);
        br.close();
    }
}
