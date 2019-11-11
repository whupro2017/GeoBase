package whu.textbase.btree.analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JiebaTokenizerTest {
    private static final boolean pt = false;
    private static final String path = "./resources/texts/sz_poi/tencent.csv";

    public static void main(String argv[]) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        JiebaTokenizer jt = new JiebaTokenizer();
        String line;
        int ln = 0;
        long begin = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            String[] tokens = jt.tokenize(fields[3]);
            if (pt) {
                System.out.print(fields[3] + "\t");
                for (int i = 0; i < tokens.length; i++) {
                    System.out.print(tokens[i] + "\t");
                }
                System.out.println("\n");
            }
            ln++;
        }
        System.out.println((System.currentTimeMillis() - begin) + "\t" + ln);
        br.close();
    }
}
