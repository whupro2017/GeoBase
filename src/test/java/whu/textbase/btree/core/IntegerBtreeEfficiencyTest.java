package whu.textbase.btree.core;

import java.io.File;
import java.util.Random;

public class IntegerBtreeEfficiencyTest {
    private static final String path = "./resources/btree.idx";
    private static Btree<Integer, Integer> bt;

    public static void main(String[] argv) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        bt = new Btree<>(20, path, 100, 4096, 100, 0.6f);
        System.out.println("Tree built");

        Random random = new Random();
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            int t = random.nextInt();
            if (bt.find(t) == null)
                bt.insert(t, t);
        }
        System.out
                .println("Tree inserted by level " + bt.getHeight() + " time " + (System.currentTimeMillis() - begin));

        begin = System.currentTimeMillis();
        System.out.println("Tree lookup triggered");
        int notfound = 0;
        random = new Random();
        for (int i = 0; i < 10000000; i++) {
            if (null == bt.find(random.nextInt()))
                notfound++;
            //assert (i == bt.find(i));
        }
        System.out.println(
                "Tree lookup completed" + " time " + (System.currentTimeMillis() - begin) + " miss " + notfound);
    }
}
