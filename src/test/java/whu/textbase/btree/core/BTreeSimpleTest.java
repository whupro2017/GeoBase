package whu.textbase.btree.core;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BTreeSimpleTest {
    private static final String path = "./resources/btree.idx";
    private static Btree<Integer, Integer> bt;

    @Before
    public void constructTree() {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        bt = new Btree<>(20, path, 100, 4096, 100, 0.6f);
        System.out.println("Tree built");
    }

    @Test
    public void insertTest() {
        for (int i = 0; i < 100000; i++) {
            bt.insert(i, i);
        }
        System.out.println("Tree inserted by level " + bt.getHeight());
    }

    @After
    public void queryTest() {
        System.out.println("Tree lookup triggered");
        for (int i = 0; i < 100000; i++) {
            assert (i == bt.find(i));
        }
        System.out.println("Tree lookup completed");
    }
}
