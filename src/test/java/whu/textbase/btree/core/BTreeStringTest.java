package whu.textbase.btree.core;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import whu.textbase.btree.serialize.iSerializable;

import java.io.File;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) public class BTreeStringTest {
    class SerializableString implements iSerializable, Comparable<SerializableString> {
        private String string;

        public SerializableString() {
            string = new String();
        }

        public SerializableString(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }

        @Override public byte[] serialize() {
            return string.getBytes();
        }

        @Override public void deseriablize(byte[] data) {
            string = new String(data);
        }

        @Override public int compareTo(SerializableString o) {
            return string.compareTo(o.getString());
        }
    }

    private static final String path = "./resources/btree.idx";

    private static Btree<SerializableString, SerializableString> bt;

    @Before public void constructTree() {
        try {
            Class.forName("SerializableString").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        bt = new Btree<>(20, path, 100, 4096, 100, 0.6f);
        System.out.println("Tree built");
        bt.insert(new SerializableString("dummy"), new SerializableString("dummy"));
    }

    @Test public void insertTest() {
        for (int i = 0; i < 10000; i++) {
            //System.out.println("\t" + i + " level " + bt.getHeight());
            bt.insert(new SerializableString("key" + i), new SerializableString("value" + i));
        }
        System.out.println("Tree inserted by level " + bt.getHeight());
    }

    @After public void queryTest() {
        System.out.println("Tree lookup triggered");
        for (int i = 0; i < 10000; i++) {
            System.out.println(i);
            assert (bt.find(new SerializableString("key" + i)).equals("key" + i));
        }
        System.out.println("Tree lookup completed");
    }
}
