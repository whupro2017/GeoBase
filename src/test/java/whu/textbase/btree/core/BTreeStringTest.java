package whu.textbase.btree.core;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BTreeStringTest {

    private static final String path = "./resources/btree.idx";

    private static Btree<SerializableString, SerializableString> bt;

    @Before
    public void constructTree() {
        /*try {
            File root = new File(".");
            File clsp = new File("./whu/textbase/btree/core");
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL(), clsp.toURI().toURL()});
            Class.forName("SerializableString", true, classLoader).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        bt = new Btree<>(20, path, 100, 4096, 100, 0.6f);
        System.out.println("Tree built");
        bt.insert(new SerializableString("dummy"), new SerializableString("dummy"));
    }

    @Test
    public void insertTest() {
        for (int i = 0; i < 100000; i++) {
            //System.out.println("\t" + i + " level " + bt.getHeight());
            bt.insert(new SerializableString("key" + i), new SerializableString("value" + i));
        }
        System.out.println("Tree inserted by level " + bt.getHeight());
    }

    @After
    public void queryTest() {
        System.out.println("Tree lookup triggered");
        for (int i = 0; i < 100000; i++) {
            System.out.println(i);
            assert (bt.find(new SerializableString("key" + i)).equals("key" + i));
        }
        System.out.println("Tree lookup completed");
    }
}
