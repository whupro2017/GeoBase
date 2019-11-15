package whu.textbase.btree.core;

import whu.textbase.btree.common.Utils;
import whu.textbase.btree.serialize.iSerializable;

import java.io.*;
import java.util.*;

/*
 * * this class is designed for Btree that data and leafnode are stored apart.
 * It supports unfixed length value or large value, but key should be fixed length.
 * It directly support Integer and Long as its key, Integer, Long and String as its
 * value.If you want to use self-defined type for key or value, these rules should be
 * followed:
 *     1.key must be fixed-length and implements java.lang.Comparable and serialize.iSerializable interface.
 *     2.value must implements serialize.iSerializable interface.
 *
 * To use this Btree, first you should configure necessary parameters in api.Conf and use this object
 *     to Construct Btree, then you can use insert and find to operate, now update not supported.
 * Detailed use tips can be found in src/test/java.btree.UnClusteredBtreeTest.
 *
 * tips: if value is length fixed, using BtreeCluster instead of Btree will get better performance.
 *
 */

public class Btree<T extends Comparable<T>, V> {
    private TreeCache cache;
    private Class<?> keytype, valuetype;
    private boolean type;
    private iNode root = null;
    private int keyNum, keylen, rootaddr, headaddr, nodecachesize, tailaddr, blockSize, interInitial = -1, leafInitial =
            -2, inodensize = 12, internsize, InterNum, LeafNum, dataBlockNum, leafnsize, nodeNumOfBlock, datacachesize,
            treecachesize, size;
    private float cachefac;
    private long readNo, writeNo;
    private byte[] nodeFlash, keyFlash;
    private RandomAccessFile raf;
    private BitSet blockFlag;
    private String path;//must be absolute path

    public Btree(int nodeNumOfBlock, String path, int treecachesize, int blocksize, int datacachesize, float cachefac) {
        this.blockSize = blocksize;
        this.nodeNumOfBlock = nodeNumOfBlock;
        this.cachefac = cachefac;
        this.datacachesize = datacachesize;
        this.treecachesize = treecachesize;
        this.readNo = this.writeNo = this.InterNum = this.LeafNum = this.dataBlockNum = 0;
        this.writeNo = 0;
        this.path = path.substring(0, path.lastIndexOf(File.separator) + 1) + path
                .substring(path.lastIndexOf(File.separator) + 1) + ".meta.data";
        type = false;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        } else {
            String apath = file.getAbsolutePath();
            if (!file.getParentFile().isDirectory()) {
                file.getParentFile().mkdirs();
            }
        }
        try {
            raf = new RandomAccessFile(path, "rw");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Btree(String path) {
        this(20, path, 100, 65536, 100, 0.6f);
    }

    public Btree() { // only used for load
        this.readNo = this.writeNo = 0;
    }

    public void insert(T key, V value) {
        if (key == null)
            throw new NullPointerException("must not be null for key.");
        size++;
        if (!type) {
            keytype = key.getClass();
            valuetype = value.getClass();
            type = true;
            if (keytype == Integer.class) {
                keylen = 4;
            } else if (keytype == Long.class) {
                keylen = 8;
            } else {
                keylen = ((iSerializable) key).serialize().length;
            }
            keyNum = ((blockSize - 1) / nodeNumOfBlock - inodensize - 8) / (keylen + 8) + 1;
            if (keyNum < 1) {
                keyNum = 1;
            }
            internsize = 4 * (keyNum + 1) + inodensize + (keyNum) * keylen;
            leafnsize = 8 * (keyNum - 1) + inodensize + (keyNum - 1) * keylen + 8;
            nodeFlash = new byte[internsize > leafnsize ? internsize : leafnsize];
            keyFlash = new byte[keylen];
            cache = new TreeCache(treecachesize, datacachesize, cachefac, raf, nodeNumOfBlock);
            root = cache.getNode(leafInitial);
            rootaddr = headaddr = tailaddr = root.addr;
        }
        byte[] data = null;
        if (valuetype == Integer.class) {//Integer and long only for test, no use
            data = new byte[4];
            // Minor revision on type cast by Wenhai.
            Utils.getBytes4((Integer) value, data, 0);
        } else if (valuetype == Long.class) {
            data = new byte[8];
            // Minor revision on type cast by Wenhai.
            Utils.getBytes8((Long) value, data, 0);
        } else if (valuetype == String.class) {
            data = ((String) value).getBytes();
        } else if (iSerializable.class.isAssignableFrom(valuetype)) {
            data = ((iSerializable) value).serialize();
        } else {
            try {
                throw new Exception("unsupported value type inserted");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        int length = data.length;
        while (length > blockSize) {
            long addr = cache.saveData(data, data.length - length, blockSize);
            length -= blockSize;
            iNode node = root.insert(key, addr, cache);
            if (node != null) {
                root = node;
                rootaddr = root.addr;
            }
        }
        long addr = cache.saveData(data, data.length - length, length);
        iNode node = root.insert(key, addr, cache);
        if (node != null) {
            root = node;
            rootaddr = root.addr;
        }
    }

    @SuppressWarnings("unchecked")
    public V find(T key) {
        if (root == null) {
            return null;
        }
        byte[] data = root.find(key, cache);
        if (data == null) {
            return null;
        }
        if (valuetype == Integer.class) {
            Integer result = Utils.getInt(data, 0);
            return (V) result;
        } else if (valuetype == Long.class) {
            Long result = Utils.getLong(data, 0);
            return (V) result;
        } else if (valuetype == String.class) {
            return (V) new String(data);
        } else {
            iSerializable result = null;
            try {
                result = (iSerializable) valuetype.newInstance();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            result.deseriablize(data);
            return (V) result;
        }
    }

    @SuppressWarnings("unchecked")
    public int getHeight() {
        int height = 1;
        iNode node = root;
        while (!(node instanceof Btree.LeafNode)) {
            height++;
            node = cache.getNode(((InternalNode) node).pointers[0]);
        }
        return height;
    }

    public int getKeyNum() {
        return keyNum;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getCacheSize() {
        return datacachesize + treecachesize;
    }

    public void printTree() {
        printNode(root);
    }

    public void sync() {
        cache.sync();
    }

    public void close() {
        cache.close();
        ByteArrayOutputStream byt = new ByteArrayOutputStream();
        try {
            ObjectOutputStream obj = new ObjectOutputStream(byt);
            obj.writeObject(keytype);
            obj.writeObject(valuetype);
            obj.writeInt(keylen);
            obj.writeInt(keyNum);
            obj.writeInt(blockSize);
            obj.writeInt(treecachesize);
            obj.writeInt(datacachesize);
            obj.writeFloat(cachefac);
            obj.writeInt(nodeNumOfBlock);
            obj.writeInt(headaddr);
            obj.writeInt(tailaddr);
            obj.writeInt(size);
            obj.writeInt(nodecachesize);
            obj.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        byte[] data = byt.toByteArray();
        try {
            RandomAccessFile metaraf = new RandomAccessFile(path, "rw");
            boolean typeOfRoot = root instanceof Btree.LeafNode;
            root.serialize();
            int length = typeOfRoot ? leafnsize : internsize;
            metaraf.writeInt(data.length);
            metaraf.write(data);
            metaraf.writeBoolean(typeOfRoot);
            metaraf.writeInt(length);
            metaraf.write(nodeFlash, 0, length);
            metaraf.close();
            raf.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        root = null;
    }

    public void readMeta(String path) {
        try {
            this.path = path.substring(0, path.lastIndexOf(File.separator) + 1) + path
                    .substring(path.lastIndexOf(File.separator) + 1) + ".meta.data";
            RandomAccessFile raf = new RandomAccessFile(this.path, "r");
            int length = raf.readInt();
            byte[] data = new byte[length];
            raf.read(data);
            ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
            ObjectInputStream objInput = new ObjectInputStream(byteInput);
            try {
                keytype = (Class<?>) objInput.readObject();
                valuetype = (Class<?>) objInput.readObject();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            keylen = objInput.readInt();
            keyNum = objInput.readInt();
            blockSize = objInput.readInt();
            treecachesize = objInput.readInt();
            datacachesize = objInput.readInt();
            cachefac = objInput.readFloat();
            nodeNumOfBlock = objInput.readInt();
            headaddr = objInput.readInt();
            tailaddr = objInput.readInt();
            size = objInput.readInt();
            nodecachesize = objInput.readInt();
            boolean type = raf.readBoolean();
            internsize = 4 * (keyNum + 1) + inodensize + (keyNum) * keylen;
            leafnsize = 8 * (keyNum - 1) + inodensize + (keyNum - 1) * keylen + 8;
            nodeFlash = new byte[internsize > leafnsize ? internsize : leafnsize];
            keyFlash = new byte[keylen];
            raf.read(nodeFlash, 0, raf.readInt());
            root = type ? new LeafNode() : new InternalNode();
            root.deserialize(nodeFlash);
            raf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        type = true;
        try {
            raf = new RandomAccessFile(path, "rw");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void open(String path) {
        readMeta(path);
        cache = new TreeCache(treecachesize, datacachesize, cachefac, raf, nodeNumOfBlock);
        rootaddr = root.addr;
    }

    public void open(String path, int treecacheSize, int datacacheSize) {
        readMeta(path);
        this.treecachesize = treecacheSize;
        this.datacachesize = datacacheSize;
        cache = new TreeCache(treecachesize, datacachesize, cachefac, raf, nodeNumOfBlock);
        rootaddr = root.addr;
    }

    public Iterator<IteratorEntry<T, V>> iterator() {
        return new InnerIterator();
    }

    public Iterator<IteratorEntry<T, V>> reverseIterator() {
        return new InnerReverseIterator();
    }

    @SuppressWarnings("unchecked")
    private void loadNode(iNode node) {
        if (node instanceof Btree.LeafNode) {
            LeafNode leafNode = (LeafNode) node;
            for (int i = 0; i < leafNode.size; i++) {
                cache.getData(leafNode.values[i]);
            }
            return;
        }
        for (int i = 0; i <= node.size; i++) {
            loadNode(cache.getNode(((InternalNode) node).pointers[i]));
        }
    }

    public void loadAll() {
        loadNode(root);
    }

    @SuppressWarnings("unchecked")
    private void loadNodeRestrict(iNode node) {
        if (node instanceof Btree.LeafNode) {
            return;
        }
        if (cache.getNode(((InternalNode) node).pointers[0]) instanceof Btree.LeafNode) {
            return;
        }
        for (int i = 1; i <= node.size; i++) {
            loadNodeRestrict(cache.getNode(((InternalNode) node).pointers[i]));
        }
    }

    public void loadPart() {
        loadNodeRestrict(root);
    }

    @SuppressWarnings("unchecked")
    private class InnerIterator implements Iterator<IteratorEntry<T, V>> {
        private LeafNode curNode;
        private int curIndex;

        public InnerIterator() {
            curNode = (LeafNode) cache.getNode(headaddr);
            curIndex = 0;
        }

        @Override
        public boolean hasNext() {
            // TODO Auto-generated method stub
            if (curIndex >= curNode.size) {
                return false;
            }
            return true;
        }

        @Override
        public IteratorEntry<T, V> next() {
            // TODO Auto-generated method stub
            List<byte[]> resultList = new ArrayList<byte[]>();
            byte[] temp;
            int length = 0;
            T curKey = (T) curNode.keys[curIndex];
            for (; curIndex < curNode.size && ((T) curNode.keys[curIndex]).compareTo(curKey) == 0; curIndex++) {
                temp = cache.getData(curNode.values[curIndex]);
                length += temp.length;
                resultList.add(temp);
            }
            while (curIndex >= curNode.size && curNode.next != -1) {
                curNode = (LeafNode) cache.getNode(curNode.next);
                for (
                        curIndex = 0;
                        curIndex < curNode.size && ((T) curNode.keys[curIndex]).compareTo(curKey) == 0; curIndex++) {
                    temp = cache.getData(curNode.values[curIndex]);
                    length += temp.length;
                    resultList.add(temp);
                }
            }
            byte[] data = new byte[length];
            for (int i = 0; i < resultList.size(); i++) {
                temp = resultList.get(i);
                System.arraycopy(temp, 0, data, data.length - length, temp.length);
                length -= temp.length;
            }
            if (valuetype == Integer.class) {
                Integer result = Utils.getInt(data, 0);
                return new IteratorEntry<T, V>(curKey, (V) result);
            } else if (valuetype == Long.class) {
                Long result = Utils.getLong(data, 0);
                return new IteratorEntry<T, V>(curKey, (V) result);
            } else if (valuetype == String.class) {
                return new IteratorEntry<T, V>(curKey, (V) new String(data));
            } else {
                iSerializable result = null;
                try {
                    result = (iSerializable) valuetype.newInstance();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                result.deseriablize(data);
                return new IteratorEntry<T, V>(curKey, (V) result);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private class InnerReverseIterator implements Iterator<IteratorEntry<T, V>> {
        private LeafNode curNode;
        private int curIndex;

        public InnerReverseIterator() {
            curNode = (LeafNode) cache.getNode(tailaddr);
            curIndex = curNode.size - 1;
        }

        @Override
        public boolean hasNext() {
            // TODO Auto-generated method stub
            if (curIndex < 0) {
                return false;
            }
            return true;
        }

        @Override
        public IteratorEntry<T, V> next() {
            // TODO Auto-generated method stub
            List<byte[]> resultList = new ArrayList<byte[]>();
            byte[] temp;
            int length = 0;
            T curKey = (T) curNode.keys[curIndex];
            for (; curIndex >= 0 && ((T) curNode.keys[curIndex]).compareTo(curKey) == 0; curIndex--) {
                temp = cache.getData(curNode.values[curIndex]);
                length += temp.length;
                resultList.add(temp);
            }
            while (curIndex < 0 && curNode.pre != -1) {
                curNode = (LeafNode) cache.getNode(curNode.pre);
                for (
                        curIndex = curNode.size - 1;
                        curIndex >= 0 && ((T) curNode.keys[curIndex]).compareTo(curKey) == 0; curIndex--) {
                    temp = cache.getData(curNode.values[curIndex]);
                    length += temp.length;
                    resultList.add(temp);
                }
            }
            byte[] data = new byte[length];
            for (int i = resultList.size() - 1; i >= 0; i--) {
                temp = resultList.get(i);
                System.arraycopy(temp, 0, data, data.length - length, temp.length);
                length -= temp.length;
            }
            if (valuetype == Integer.class) {
                Integer result = Utils.getInt(data, 0);
                return new IteratorEntry<T, V>(curKey, (V) result);
            } else if (valuetype == Long.class) {
                Long result = Utils.getLong(data, 0);
                return new IteratorEntry<T, V>(curKey, (V) result);
            } else if (valuetype == String.class) {
                return new IteratorEntry<T, V>(curKey, (V) new String(data));
            } else {
                iSerializable result = null;
                try {
                    result = (iSerializable) valuetype.newInstance();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                result.deseriablize(data);
                return new IteratorEntry<T, V>(curKey, (V) result);
            }
        }

    }

    public Btree(List<Btree<T, V>> btreeList, int nodeNumOfBlock, String path, int treecachesize, int blocksize,
                 int datacachesize, float cachefac) {
        this(nodeNumOfBlock, path, treecachesize, blocksize, datacachesize, cachefac);
        PriorityQueue<MergeEntry<T, V>> entryHeap = new PriorityQueue<MergeEntry<T, V>>(btreeList.size());
        List<Iterator<IteratorEntry<T, V>>> iteratorList = new ArrayList<Iterator<IteratorEntry<T, V>>>();
        for (int i = 0; i < btreeList.size(); i++) {
            Iterator<IteratorEntry<T, V>> it = btreeList.get(i).iterator();
            if (it.hasNext()) {
                IteratorEntry<T, V> tempItEntry = it.next();
                entryHeap.add(new MergeEntry<T, V>(tempItEntry.key, tempItEntry.value, i));
            }
            iteratorList.add(it);
        }
        while (!entryHeap.isEmpty()) {
            MergeEntry<T, V> mEntry = entryHeap.remove();
            this.insert(mEntry.key, mEntry.value);
            Iterator<IteratorEntry<T, V>> it = iteratorList.get(mEntry.index);
            if (it.hasNext()) {
                IteratorEntry<T, V> tempItEntry = it.next();
                entryHeap.add(new MergeEntry<T, V>(tempItEntry.key, tempItEntry.value, mEntry.index));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void printNode(iNode node) {
        if (node == null) {
            return;
        }
        if (node instanceof Btree.LeafNode) {
            node.printInfo();
            return;
        }
        node.printInfo();
        for (int i = 0; i < node.size + 1; i++) {
            printNode(cache.getNode(((InternalNode) node).pointers[i]));
        }
    }

    private abstract class iNode {
        public int parent;
        public Object[] keys;
        public int size;
        public int addr;
        private boolean dirty;
        private boolean isBusy;

        public abstract iNode insert(T key, long pointer, TreeCache cache);

        public abstract byte[] find(T key, TreeCache cache);

        public void serialize() {
            int pos = 0;
            nodeFlash[pos++] = (byte) (parent & 0xff);
            nodeFlash[pos++] = (byte) ((parent & 0xff00) >> 8);
            nodeFlash[pos++] = (byte) ((parent & 0xff0000) >> 16);
            nodeFlash[pos++] = (byte) ((parent & 0xff000000) >> 24);
            nodeFlash[pos++] = (byte) (size & 0xff);
            nodeFlash[pos++] = (byte) ((size & 0xff00) >> 8);
            nodeFlash[pos++] = (byte) ((size & 0xff0000) >> 16);
            nodeFlash[pos++] = (byte) ((size & 0xff000000) >> 24);
            nodeFlash[pos++] = (byte) (addr & 0xff);
            nodeFlash[pos++] = (byte) ((addr & 0xff00) >> 8);
            nodeFlash[pos++] = (byte) ((addr & 0xff0000) >> 16);
            nodeFlash[pos++] = (byte) ((addr & 0xff000000) >> 24);
        }

        public void deserialize(byte[] data) {
            int pos = 0;
            parent = Utils.getInt(data, pos);
            pos += 4;
            size = Utils.getInt(data, pos);
            pos += 4;
            addr = Utils.getInt(data, pos);
            pos += 4;
        }

        public boolean isChanged() {
            return dirty;
        }

        public void setChanged(boolean dirty) {
            this.dirty = dirty;
        }

        public boolean isBusy() {
            return isBusy;
        }

        public void setBusy(boolean isBusy) {
            this.isBusy = isBusy;
        }

        public abstract void printInfo();
    }

    private class InternalNode extends iNode {
        public int[] pointers;

        public InternalNode() {
            this.size = 0;
            this.pointers = new int[keyNum + 1];
            this.parent = interInitial;
            this.keys = new Object[keyNum];
        }

        @SuppressWarnings("unchecked")
        public iNode insert(T key, long pointer, TreeCache cache) {
            int pos = -1;
            if (this.size == 0 || key.compareTo((T) this.keys[0]) < 0) {
                pos = 0;
            } else if (key.compareTo((T) this.keys[this.size - 1]) > 0) {
                pos = this.size;
            } else {
                pos = Utils.bSearch2(this.keys, 0, this.size, key);
                if (pos < this.size && key.compareTo((T) this.keys[pos]) == 0) {
                    pos++;
                }
            }

            iNode child = cache.getNode(pointers[pos]);
            iNode node = child.insert(key, pointer, cache);
            return node;
        }

        @SuppressWarnings("unchecked")
        iNode insert(T key, int leftChild, int rightChild, TreeCache cache) {
            this.setChanged(true);
            this.setBusy(true);
            if (this.size == 0) {
                this.size++;
                this.pointers[0] = leftChild;
                this.pointers[1] = rightChild;
                this.keys[0] = key;
                this.setBusy(false);
                return this;
            }

            Object[] newKeys = new Object[keyNum + 1];
            int[] newPointers = new int[keyNum + 2];
            int pos = -1;
            if (this.size == 0 || key.compareTo((T) this.keys[0]) < 0) {
                pos = 0;
            } else if (key.compareTo((T) this.keys[this.size - 1]) > 0) {
                pos = this.size;
            } else {
                if (leftChild == -1) {
                    pos = Utils.bSearch(this.keys, 0, this.size, key);
                } else {
                    pos = Utils.bSearch2(this.keys, 0, this.size, key);
                    if (pos < this.size && key.compareTo((T) this.keys[pos]) == 0) {
                        pos++;
                    }
                }
            }

            System.arraycopy(this.keys, 0, newKeys, 0, pos);
            newKeys[pos] = key;
            System.arraycopy(this.keys, pos, newKeys, pos + 1, this.size - pos);
            System.arraycopy(this.pointers, 0, newPointers, 0, pos + 1);
            newPointers[pos + 1] = rightChild;
            System.arraycopy(this.pointers, pos + 1, newPointers, pos + 2, this.size - pos);
            this.size++;
            if (this.size <= keyNum) {
                System.arraycopy(newKeys, 0, this.keys, 0, this.size);
                System.arraycopy(newPointers, 0, this.pointers, 0, this.size + 1);
                this.setBusy(false);
                return null;
            }
            int m = (this.size / 2);
            int total = this.size;
            if (newKeys[0] == newKeys[total - 1]) {
                m = total - 2;
            }
            System.arraycopy(newKeys, 0, this.keys, 0, m);
            System.arraycopy(newPointers, 0, this.pointers, 0, m + 1);
            this.size = m;
            InternalNode newNode = (InternalNode) cache.getNode(interInitial);
            newNode.setBusy(true);
            newNode.setChanged(true);
            newNode.size = total - m - 1;
            System.arraycopy(newKeys, m + 1, newNode.keys, 0, newNode.size);

            System.arraycopy(newPointers, m + 1, newNode.pointers, 0, newNode.size + 1);
            for (int j = 0; j < newNode.size + 1; j++) {
                iNode child = cache.getNode(newNode.pointers[j]);
                child.parent = newNode.addr;
                child.setChanged(true);
            }
            InternalNode parentNode = (InternalNode) cache.getNode(this.parent);
            newNode.parent = this.parent = parentNode.addr;
            newNode.setBusy(false);
            leftChild = this.addr;
            if (newNode.keys[0] == this.keys[m - 1] && newNode.size > 1 && this.keys[0] != newNode.keys[0]
                    && parentNode.size != 0) {
                leftChild = -1;
            }
            iNode node = (parentNode).insert((T) newKeys[m], leftChild, newNode.addr, cache);
            this.setBusy(false);
            return node;
        }

        @SuppressWarnings("unchecked")
        public byte[] find(T key, TreeCache cache) {
            int middle = Utils.bSearch(this.keys, 0, this.size, key);
            iNode child;
            int length = 0;
            List<byte[]> result = new ArrayList<byte[]>();
            byte[] temp;
            if (middle < this.size && key.compareTo((T) this.keys[middle]) == 0) {
                child = cache.getNode(this.pointers[middle]);
                temp = child.find(key, cache);
                if (temp != null) {
                    length += temp.length;
                    result.add(temp);
                }
                for (int i = middle; i < this.size && ((T) this.keys[i]).compareTo(key) == 0; i++) {
                    child = cache.getNode(this.pointers[i + 1]);
                    temp = child.find(key, cache);
                    if (temp != null) {
                        length += temp.length;
                        result.add(temp);
                    }
                }
            } else {
                child = cache.getNode(this.pointers[middle]);
                temp = child.find(key, cache);
                if (temp == null) {
                    return null;
                }
                length += temp.length;
                result.add(temp);
            }
            byte[] bytes = new byte[length];
            for (int i = 0; i < result.size(); i++) {
                temp = result.get(i);
                System.arraycopy(temp, 0, bytes, bytes.length - length, temp.length);
                length -= temp.length;
            }
            return bytes;
        }

        public void serialize() {
            super.serialize();
            int pos = inodensize;
            if (keytype == Integer.class) {
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] == null) {
                        for (int j = 0; j < keylen; j++) {
                            nodeFlash[pos++] = -1;
                        }
                        pos += (keys.length - i - 1) * keylen;
                        break;
                    }
                    Utils.getBytes4((int) keys[i], nodeFlash, pos);
                    pos += keylen;
                }
            } else if (keytype == Long.class) {
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] == null) {
                        for (int j = 0; j < keylen; j++) {

                            nodeFlash[pos++] = -1;
                        }
                        pos += (keys.length - i - 1) * keylen;
                        break;
                    }
                    Utils.getBytes8((long) keys[i], nodeFlash, pos);
                    pos += keylen;
                }
            } else {
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] == null) {
                        for (int j = 0; j < keylen; j++) {
                            nodeFlash[pos++] = -1;
                        }
                        pos += (keys.length - i - 1) * keylen;
                        break;
                    }
                    byte[] temp = ((iSerializable) keys[i]).serialize();
                    System.arraycopy(temp, 0, nodeFlash, pos, temp.length);
                    pos += temp.length;

                }
            }
            for (int i = 0; i < pointers.length; i++) {
                nodeFlash[pos++] = (byte) (pointers[i] & 0xff);
                nodeFlash[pos++] = (byte) ((pointers[i] & 0xff00) >> 8);
                nodeFlash[pos++] = (byte) ((pointers[i] & 0xff0000) >> 16);
                nodeFlash[pos++] = (byte) ((pointers[i] & 0xff000000) >> 24);
            }
        }

        public void deserialize(byte[] data) {
            super.deserialize(data);
            int pos = inodensize;
            if (keytype == Integer.class) {
                for (int i = 0; i < keys.length; i++) {
                    int j = 0;
                    for (; j < keylen; j++) {
                        if (data[pos + j] != -1) {
                            break;
                        }
                    }
                    if (j == keylen) {
                        pos += (keys.length - i) * keylen;
                        break;
                    }
                    keys[i] = Utils.getInt(data, pos);
                    pos += keylen;
                }
            } else if (keytype == Long.class) {
                for (int i = 0; i < keys.length; i++) {
                    int j = 0;
                    for (; j < keylen; j++) {
                        if (data[pos + j] != -1) {
                            break;
                        }
                    }
                    if (j == keylen) {
                        pos += (keys.length - i) * keylen;
                        break;
                    }
                    keys[i] = Utils.getLong(data, pos);
                    pos += keylen;
                }
            } else {
                for (int i = 0; i < keys.length; i++) {
                    int j = 0;
                    for (; j < keylen; j++) {
                        if (data[pos + j] != -1) {
                            break;
                        }
                    }
                    if (j == keylen) {
                        pos += (keys.length - i) * keylen;
                        break;
                    }
                    System.arraycopy(data, pos, keyFlash, 0, keylen);
                    try {
                        keys[i] = (iSerializable) keytype.newInstance();
                    } catch (InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    ((iSerializable) keys[i]).deseriablize(keyFlash);
                    pos += keylen;
                }
            }
            for (int i = 0; i < pointers.length; i++) {
                pointers[i] = Utils.getInt(data, pos);
                pos += 4;
            }
        }

        public void printInfo() {
            int pointerlen;
            // System.out.println("this.addr = " + this.addr);
            // System.out.println("this.parent = " + this.parent);
            pointerlen = this.size + 1;
            System.out.print("InternalNode ");
            for (int i = 0; i < this.size; i++) {
                System.out.print(i + ":" + keys[i] + " ");
            }
            System.out.print(" : ");
            for (int i = 0; i < pointerlen; i++) {
                System.out.print(i + ":" + pointers[i] + " ");
            }
            System.out.println();
        }
    }

    private class LeafNode extends iNode {
        long[] values;
        int pre, next;

        public LeafNode() {
            this.size = 0;
            this.pre = this.next = -1;
            this.values = new long[keyNum - 1];
            this.parent = interInitial;
            this.keys = new Object[keyNum - 1];
        }

        @SuppressWarnings("unchecked")
        public iNode insert(T key, long pointer, TreeCache cache) {
            this.setChanged(true);
            this.setBusy(true);
            Object[] newKeys = new Object[keyNum];
            long[] newPointers = new long[keyNum];
            int pos = -1;
            if (this.size == 0 || key.compareTo((T) this.keys[0]) < 0) {
                pos = 0;
            } else if (key.compareTo((T) this.keys[this.size - 1]) > 0) {
                pos = this.size;
            } else {
                pos = Utils.bSearch2(this.keys, 0, this.size, key);
                if (pos < this.size && key.compareTo((T) this.keys[pos]) == 0) {
                    pos++;
                }
            }
            System.arraycopy(this.keys, 0, newKeys, 0, pos);
            newKeys[pos] = key;
            System.arraycopy(this.keys, pos, newKeys, pos + 1, this.size - pos);
            System.arraycopy(this.values, 0, newPointers, 0, pos);
            newPointers[pos] = pointer;
            System.arraycopy(this.values, pos, newPointers, pos + 1, this.size - pos);
            this.size++;
            if (this.size <= keyNum - 1) {
                System.arraycopy(newKeys, 0, this.keys, 0, this.size);
                System.arraycopy(newPointers, 0, this.values, 0, this.size);
                this.setBusy(false);
                return null;
            }
            int m = this.size / 2;
            int total = this.size;
            if (newKeys[0] == newKeys[total - 1]) {
                m = total - 1;
            }
            System.arraycopy(newKeys, 0, this.keys, 0, m);
            System.arraycopy(newPointers, 0, this.values, 0, m);
            this.size = m;
            LeafNode newNode = (LeafNode) cache.getNode(leafInitial);
            newNode.setBusy(true);
            newNode.size = total - this.size;
            System.arraycopy(newKeys, m, newNode.keys, 0, newNode.size);
            System.arraycopy(newPointers, m, newNode.values, 0, newNode.size);
            InternalNode parentNode = (InternalNode) cache.getNode(this.parent);
            newNode.parent = this.parent = parentNode.addr;
            newNode.next = this.next;
            newNode.setChanged(true);
            newNode.pre = this.addr;
            newNode.setBusy(false);
            if (this.next != -1) {
                LeafNode temp = (LeafNode) cache.getNode(this.next);
                temp.pre = newNode.addr;
                temp.setChanged(true);
            }
            this.next = newNode.addr;
            if (this.addr == tailaddr) {
                tailaddr = newNode.addr;
            }
            int leftChild = this.addr;
            if (((T) newNode.keys[0]).compareTo((T) this.keys[m - 1]) == 0 && newNode.size > 1
                    && ((T) newNode.keys[0]).compareTo((T) this.keys[0]) != 0 && parentNode.size > 0) {
                leftChild = -1;
            }
            iNode node = (parentNode).insert((T) newNode.keys[0], leftChild, newNode.addr, cache);
            this.setBusy(false);
            return node;
        }

        @SuppressWarnings("unchecked")
        public byte[] find(T key, TreeCache cache) {
            if (this.size == 0) {
                return null;
            }
            int middle = Utils.bSearch(this.keys, 0, this.size, key);
            if (middle >= this.size || key.compareTo((T) this.keys[middle]) != 0) {
                return null;
            }
            int length = 0;
            List<byte[]> result = new ArrayList<byte[]>();
            byte[] temp;
            for (int i = middle; i < this.size && key.compareTo((T) this.keys[i]) == 0; i++) {
                temp = cache.getData(this.values[i]);
                length += temp.length;
                result.add(temp);
            }
            byte[] bytes = new byte[length];
            for (int i = 0; i < result.size(); i++) {
                temp = result.get(i);
                System.arraycopy(temp, 0, bytes, bytes.length - length, temp.length);
                length -= temp.length;
            }
            return bytes;
        }

        public void serialize() {
            super.serialize();
            int pos = inodensize;
            nodeFlash[pos++] = (byte) (pre & 0xff);
            nodeFlash[pos++] = (byte) ((pre & 0xff00) >> 8);
            nodeFlash[pos++] = (byte) ((pre & 0xff0000) >> 16);
            nodeFlash[pos++] = (byte) ((pre & 0xff000000) >> 24);
            nodeFlash[pos++] = (byte) (next & 0xff);
            nodeFlash[pos++] = (byte) ((next & 0xff00) >> 8);
            nodeFlash[pos++] = (byte) ((next & 0xff0000) >> 16);
            nodeFlash[pos++] = (byte) ((next & 0xff000000) >> 24);
            if (keytype == Integer.class) {
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] == null) {
                        for (int j = 0; j < keylen; j++) {
                            nodeFlash[pos++] = -1;
                        }
                        pos += (keys.length - i - 1) * keylen;
                        break;
                    }
                    Utils.getBytes4((int) keys[i], nodeFlash, pos);
                    pos += keylen;
                }
            } else if (keytype == Long.class) {
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] == null) {
                        for (int j = 0; j < keylen; j++) {
                            nodeFlash[pos++] = -1;
                        }
                        pos += (keys.length - i - 1) * keylen;
                        break;
                    }
                    Utils.getBytes8((long) keys[i], nodeFlash, pos);
                    pos += keylen;
                }
            } else {
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] == null) {
                        for (int j = 0; j < keylen; j++) {
                            nodeFlash[pos++] = -1;
                        }
                        pos += (keys.length - i - 1) * keylen;
                        break;
                    }
                    byte[] temp = ((iSerializable) keys[i]).serialize();
                    System.arraycopy(temp, 0, nodeFlash, pos, temp.length);
                    pos += temp.length;
                }
            }
            for (int i = 0; i < values.length; i++) {
                nodeFlash[pos++] = (byte) (values[i] & 0xff);
                nodeFlash[pos++] = (byte) ((values[i] >> 8) & 0xff);
                nodeFlash[pos++] = (byte) ((values[i] >> 16) & 0xff);
                nodeFlash[pos++] = (byte) ((values[i] >> 24) & 0xff);
                nodeFlash[pos++] = (byte) ((values[i] >> 32) & 0xff);
                nodeFlash[pos++] = (byte) ((values[i] >> 40) & 0xff);
                nodeFlash[pos++] = (byte) ((values[i] >> 48) & 0xff);
                nodeFlash[pos++] = (byte) ((values[i] >> 56) & 0xff);
            }
        }

        public void deserialize(byte[] data) {
            super.deserialize(data);
            int pos = inodensize;
            pre = Utils.getInt(data, pos);
            pos += 4;
            next = Utils.getInt(data, pos);
            pos += 4;
            if (keytype == Integer.class) {
                for (int i = 0; i < keys.length; i++) {
                    int j = 0;
                    for (; j < keylen; j++) {
                        if (data[pos + j] != -1) {
                            break;
                        }
                    }
                    if (j == keylen) {
                        pos += (keys.length - i) * keylen;
                        break;
                    }
                    keys[i] = Utils.getInt(data, pos);
                    pos += keylen;
                }
            } else if (keytype == Long.class) {
                for (int i = 0; i < keys.length; i++) {
                    int j = 0;
                    for (; j < keylen; j++) {
                        if (data[pos + j] != -1) {
                            break;
                        }
                    }
                    if (j == keylen) {
                        pos += (keys.length - i) * keylen;
                        break;
                    }
                    keys[i] = Utils.getLong(data, pos);
                    pos += keylen;
                }
            } else {
                for (int i = 0; i < keys.length; i++) {
                    int j = 0;
                    for (; j < keylen; j++) {
                        if (data[pos + j] != -1) {
                            break;
                        }
                    }
                    if (j == keylen) {
                        pos += (keys.length - i) * keylen;
                        break;
                    }
                    System.arraycopy(data, pos, keyFlash, 0, keylen);
                    try {
                        keys[i] = (iSerializable) keytype.newInstance();
                    } catch (InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    ((iSerializable) keys[i]).deseriablize(keyFlash);
                    pos += keylen;
                }
            }
            for (int i = 0; i < values.length; i++) {
                values[i] = Utils.getLong(data, pos);
                pos += 8;
            }
        }

        public void printInfo() {
            int pointerlen;
            // System.out.println("this.addr = " + this.addr);
            // System.out.println("this.parent = " + this.parent);
            pointerlen = this.size;
            System.out.print("LeafNode ");
            for (int i = 0; i < this.size; i++) {
                System.out.print(i + ":" + keys[i] + " ");
            }
            System.out.print(" : ");
            for (int i = 0; i < pointerlen; i++) {
                System.out.print(i + ":" + values[i] + " ");
            }
            System.out.println();
        }
    }

    private class TreeCache {
        private final int capacity;
        private Pair first, last, second;
        private HashMap<Integer, Pair> hashMap;
        private int leafMax, interMax, nBlockBase, discard, leafno, leafoff, interno, interoff, datano, dataoff;
        private long dBlockSplit, dBlockBase;
        protected RandomAccessFile raf;
        public NodeCache nodecache;
        public DataCache datacache;

        public TreeCache(int treecachesize, int datacachesize, float cachefac, RandomAccessFile raf,
                         int nodeNumOfBlock) {
            leafoff = leafno = interoff = dataoff = 0;
            leafno = 1;
            datano = 2;
            blockFlag = new BitSet(treecachesize + datacachesize);
            this.capacity = treecachesize;
            this.raf = raf;
            this.leafMax = (blockSize - 1) / leafnsize;
            this.interMax = (blockSize - 1) / internsize;
            this.nBlockBase = (int) Math.pow(2, ((Math.log(Math.max(interMax, leafMax)) / Math.log(2)) + 1));
            this.dBlockBase = blockSize << 1;
            this.dBlockSplit = (long) blockSize * blockSize << 2;
            if (nodecachesize == 0) {
                nodecachesize = 1048576 / leafnsize;
                nodecache = new NodeCache(nodecachesize, this);
            } else {
                nodecache = new NodeCache(nodecachesize, this);
            }
            datacache = new DataCache(raf, datacachesize, 0.8f);
            this.discard = Math.max((int) (capacity * (1 - cachefac)), 1);
            hashMap = new HashMap<Integer, Pair>();
        }

        public iNode getNode(int addr) {
            if (nodecache.containsKey(addr)) {
                iNode node = nodecache.get(addr);
                return node;
            }
            if (addr == -1) {
                iNode newnode = new InternalNode();
                InterNum++;
                addr = interno * nBlockBase + interoff;
                newnode.addr = addr;
                interoff++;
                if (interoff >= interMax) {
                    interoff = 0;
                    interno = Math.max(Math.max(interno, leafno), datano) + 1;
                }
                nodecache.put(addr, newnode);
                return newnode;
            } else if (addr == -2) {
                iNode newnode = new LeafNode();
                LeafNum++;
                addr = leafno * nBlockBase + leafoff;
                newnode.addr = addr;
                leafoff++;
                if (leafoff >= leafMax) {
                    leafoff = 0;
                    leafno = Math.max(Math.max(interno, leafno), datano) + 1;
                }
                nodecache.put(addr, newnode);
                return newnode;
            }
            int bno = addr / nBlockBase;
            int nno = addr % nBlockBase;
            if (this.containsKey(bno)) {
                NodeBlock block = this.get(bno);
                iNode node = block.getNode(nno);
                node.setChanged(false);
                nodecache.put(addr, node);
                return node;
            }
            try {
                raf.seek((long) bno * blockSize);
                NodeBlock block = new NodeBlock(0);
                blockFlag.set(bno);
                byte[] data = new byte[blockSize];
                raf.read(data);
                readNo++;
                block.load(data);
                this.put(bno, block);
                iNode node = block.getNode(nno);
                node.setChanged(false);
                nodecache.put(addr, node);
                // node.printInfo();
                return node;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        public long saveData(byte[] data, int offset, int len) {
            DataBlock block = null;
            if (dataoff == 0 || dataoff + len > blockSize) {
                block = new DataBlock();
                if (dataoff + len > blockSize) {
                    dataoff = 0;
                    datano = Math.max(Math.max(interno, leafno), datano) + 1;
                    dataBlockNum++;
                }
                datacache.put(datano, block);
            } else {
                block = datacache.get(datano);
                if (block == null) {
                    try {
                        raf.seek((long) datano * blockSize);
                        block = new DataBlock();
                        byte[] bdata = new byte[blockSize];
                        readNo++;
                        raf.read(bdata);
                        block.load(bdata);
                        datacache.put(datano, block);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            block.setData(data, offset, len, dataoff);
            long addr = datano * dBlockSplit + dataoff * dBlockBase + len;
            dataoff += data.length;
            if (dataoff >= blockSize) {
                dataoff = 0;
                datano = Math.max(Math.max(interno, leafno), datano) + 1;
                dataBlockNum++;
            }
            return addr;
        }

        public byte[] getData(long addr) {

            int bno = (int) (addr / dBlockSplit);
            long baddr = (addr % dBlockSplit);
            int boff = (int) (baddr / dBlockBase);
            int blen = (int) (baddr % dBlockBase);
            if (datacache.containsKey(bno)) {
                DataBlock block = datacache.get(bno);
                byte[] data = block.getData(boff, blen);
                return data;
            }
            try {
                raf.seek((long) bno * blockSize);
                DataBlock block = new DataBlock();
                byte[] bdata = new byte[blockSize];
                raf.read(bdata);
                readNo++;
                block.load(bdata);
                datacache.put(bno, block);
                return block.getData(boff, blen);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;

        }

        public void put(Integer key, NodeBlock value) {
            Pair pair = getPair(key);
            if (pair == null) {
                if (hashMap.size() > capacity) {
                    Map<Integer, NodeBlock> temp = new TreeMap<Integer, NodeBlock>();
                    for (int i = 0; i < discard; i++) {
                        if (last.value.isChanged()) {
                            temp.put(last.key, last.value);
                        }
                        hashMap.remove(last.key);
                        removeLast();
                    }
                    for (Iterator<Integer> it = temp.keySet().iterator(); it.hasNext(); ) {
                        int bno = it.next();
                        try {
                            raf.seek((long) bno * blockSize);
                            writeNo++;
                            raf.write(temp.get(bno).store());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                pair = new Pair();
                pair.key = key;
            }
            pair.value = value;
            if (value.data[0] == 1) {
                moveToFirst(pair);
            } else {
                moveToSecond(pair);
            }
            hashMap.put(key, pair);
        }

        public NodeBlock get(Integer key) {
            Pair pair = getPair(key);
            if (pair == null)
                return null;
            if (pair.value.data[0] == 1) {
                moveToFirst(pair);
            } else {
                moveToSecond(pair);
            }
            return pair.value;
        }

        public boolean containsKey(Integer key) {
            if (hashMap.containsKey(key)) {
                return true;
            }
            return false;
        }

        public void sync() {
            nodecache.sync();
            datacache.sync();
            Pair pair = first;
            while (pair != null) {
                if (pair.value.isChanged()) {
                    try {
                        raf.seek((long) pair.key * blockSize);
                        raf.write(pair.value.store());
                        pair.value.setChanged(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                pair = pair.next;
            }

        }

        public void close() {
            nodecache.close();
            datacache.close();
            Map<Integer, NodeBlock> temp = new TreeMap<Integer, NodeBlock>();
            while (last != null) {
                if (last.value.isChanged()) {
                    temp.put(last.key, last.value);
                }
                hashMap.remove(last.key);
                removeLast();
            }
            for (Iterator<Integer> it = temp.keySet().iterator(); it.hasNext(); ) {
                int bno = it.next();
                try {
                    raf.seek((long) bno * blockSize);
                    raf.write(temp.get(bno).store());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void moveToFirst(Pair entry) {
            if (entry == first)
                return;
            if (entry.pre != null)
                entry.pre.next = entry.next;
            if (entry.next != null)
                entry.next.pre = entry.pre;
            if (entry == last)
                last = last.pre;
            if (first == null) {
                entry.next = second;
                /*
                 * ���ڴ���һ�����棬���Զ�������Ҳ������ֻ���м�ڵ�����
                 */
                if (second != null) {
                    second.pre = entry;
                }
                if (last == null) {
                    last = entry;
                }
                entry.pre = null;
                first = entry;
                return;
            }
            entry.next = first;
            first.pre = entry;
            first = entry;
            entry.pre = null;
        }

        /*
         * Ҷ�ڵ�ר��
         */
        private void moveToSecond(Pair entry) {
            if (entry == second)
                return;
            /*
             * ��������ǰ��Ķ����൱�ڴ�������ɾ��������
             */
            if (entry.pre != null)
                entry.pre.next = entry.next;
            if (entry.next != null)
                entry.next.pre = entry.pre;
            /*
             * ����ƶ�����lastָ��Ľ�β����
             */
            if (entry == last) {
                last = last.pre;
            }
            /*
             * ���first��lastΪnull˵������entry֮ǰ����Ϊ��
             */
            if (second == null) {
                entry.pre = last;
                if (last != null) {
                    last.next = entry;
                }
                entry.next = null;
                last = second = entry;
                return;
            }
            /*
             * ��entry��������
             */
            entry.next = second;
            entry.pre = second.pre;
            if (second.pre != null) {
                second.pre.next = entry;
            }
            second.pre = entry;
            second = entry;
        }

        private void removeLast() {
            if (last != null) {
                last = last.pre;
                if (last == null) {
                    first = second = null;
                } else {
                    if (last.value.data[0] == 1) {
                        second = null;
                    }
                    last.next = null;
                }
            }
        }

        private Pair getPair(Integer key) {
            return hashMap.get(key);
        }

        class Pair {
            public Pair pre;
            public Pair next;
            public Integer key;
            public NodeBlock value;
        }

        public int size() {
            return hashMap.size();
        }
    }

    private class NodeCache {
        private final int capacity, discard;
        private Pair first, last;
        private int nBlockBase;
        private HashMap<Integer, Pair> hashMap;
        private TreeCache cache;
        private RandomAccessFile raf;

        public NodeCache(int size, TreeCache cache) {
            this.cache = cache;
            this.capacity = size;
            this.raf = cache.raf;
            this.discard = Math.max((int) (capacity * 0.4), 1);
            this.nBlockBase = cache.nBlockBase;
            hashMap = new HashMap<Integer, Pair>();
        }

        public void put(Integer key, iNode value) {
            Pair pair = getEntry(key);
            if (pair == null) {
                if (hashMap.size() > capacity) {
                    Map<Integer, iNode> temp = new TreeMap<Integer, iNode>();
                    for (int i = 0; i < discard; i++) {
                        while (last.value.isBusy() || last.value.addr == rootaddr) {
                            moveToFirst(last);
                        }
                        if (last.value.isChanged()) {
                            temp.put(last.key, last.value);
                        }
                        hashMap.remove(last.key);
                        removeLast();
                    }
                    for (Iterator<Integer> it = temp.keySet().iterator(); it.hasNext(); ) {
                        int addr = it.next();
                        iNode tempNode = temp.get(addr);
                        int bno = addr / nBlockBase;
                        int nno = addr % nBlockBase;
                        NodeBlock block = cache.get(bno);
                        if (block == null) {
                            if (blockFlag.get(bno) && nodeNumOfBlock != 1) {
                                block = new NodeBlock();
                                try {
                                    raf.seek((long) bno * blockSize);
                                    raf.read(block.data);
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            } else {
                                int type = tempNode instanceof Btree.LeafNode ? 2 : 1;
                                block = new NodeBlock(type);
                                blockFlag.set(bno);
                            }
                            cache.put(bno, block);
                        }
                        block.setNode(tempNode, nno);
                    }
                }
                pair = new Pair();
                pair.key = key;
            }
            pair.value = value;
            moveToFirst(pair);
            hashMap.put(key, pair);
        }

        public iNode get(Integer key) {
            Pair entry = getEntry(key);
            if (entry == null)
                return null;
            moveToFirst(entry);
            return entry.value;
        }

        public boolean containsKey(Integer key) {
            if (hashMap.containsKey(key)) {
                return true;
            }
            return false;
        }

        protected void sync() {
            Pair pair = first;
            while (pair != null) {
                if (pair.value.isChanged()) {
                    int bno = pair.key / nBlockBase;
                    int nno = pair.key % nBlockBase;
                    NodeBlock block = cache.get(bno);
                    if (block == null) {
                        if (blockFlag.get(bno)) {

                            block = new NodeBlock();

                            try {
                                raf.seek((long) bno * blockSize);
                                raf.read(block.data);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            int type = last.value instanceof Btree.LeafNode ? 2 : 1;
                            block = new NodeBlock(type);
                            blockFlag.set(bno);
                        }
                        cache.put(bno, block);
                    }
                    block.setNode(pair.value, nno);
                    pair.value.setChanged(false);
                }
                pair = pair.next;
            }
        }

        protected void close() {
            while (last != null) {
                if (last.value.isChanged()) {
                    int bno = last.key / nBlockBase;
                    int nno = last.key % nBlockBase;
                    NodeBlock block = cache.get(bno);
                    if (block == null) {
                        if (blockFlag.get(bno)) {

                            block = new NodeBlock();

                            try {
                                raf.seek((long) bno * blockSize);
                                raf.read(block.data);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            int type = last.value instanceof Btree.LeafNode ? 2 : 1;
                            block = new NodeBlock(type);
                            blockFlag.set(bno);
                        }
                        cache.put(bno, block);
                    }
                    block.setNode(last.value, nno);
                    last.value.setChanged(false);
                }
                hashMap.remove(last.key);
                removeLast();
            }
        }

        private void moveToFirst(Pair pair) {
            if (pair == first)
                return;
            if (pair.pre != null)
                pair.pre.next = pair.next;
            if (pair.next != null)
                pair.next.pre = pair.pre;
            if (pair == last)
                last = last.pre;
            if (first == null || last == null) {
                first = last = pair;
                return;
            }
            pair.next = first;
            first.pre = pair;
            first = pair;
            pair.pre = null;
        }

        private void removeLast() {
            if (last != null) {
                last = last.pre;
                if (last == null)
                    first = null;
                else
                    last.next = null;
            }
        }

        private Pair getEntry(Integer key) {
            return hashMap.get(key);
        }

        private class Pair {
            Pair pre;
            Pair next;
            Integer key;
            iNode value;

            @Override
            public String toString() {
                return "Pair [key=" + key + "]";
            }
        }
    }

    private class DataCache {
        private final int capacity;
        private Pair first, last;
        private HashMap<Integer, Pair> hashMap;
        private int discard;
        private RandomAccessFile raf;

        public DataCache(RandomAccessFile raf, int size, float factor) {
            this.capacity = size;
            this.raf = raf;
            this.discard = Math.max((int) (capacity * (1 - cachefac)), 1);
            hashMap = new HashMap<Integer, Pair>();
        }

        public void put(Integer key, DataBlock value) {
            Pair pair = getEntry(key);
            if (pair == null) {
                if (hashMap.size() > capacity) {
                    Map<Integer, DataBlock> temp = new TreeMap<Integer, DataBlock>();
                    for (int i = 0; i < discard; i++) {
                        if (last.value.isChanged()) {
                            temp.put(last.key, last.value);
                        }
                        hashMap.remove(last.key);
                        removeLast();
                    }

                    for (Iterator<Integer> it = temp.keySet().iterator(); it.hasNext(); ) {
                        int bno = it.next();
                        try {
                            raf.seek((long) bno * blockSize);
                            raf.write(temp.get(bno).store());
                            writeNo++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                pair = new Pair();
                pair.key = key;
            }
            pair.value = value;
            moveToFirst(pair);
            hashMap.put(key, pair);
        }

        public DataBlock get(Integer key) {
            Pair pair = getEntry(key);
            if (pair == null)
                return null;
            moveToFirst(pair);
            return pair.value;
        }

        public boolean containsKey(Integer key) {
            if (hashMap.containsKey(key)) {
                return true;
            }
            return false;
        }

        public void sync() {
            Pair pair = first;
            while (pair != null) {
                if (pair.value.isChanged()) {
                    try {
                        raf.seek((long) pair.key * blockSize);
                        raf.write(pair.value.store());
                        pair.value.setChanged(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                pair = pair.next;
            }
        }

        public void close() {
            Map<Integer, DataBlock> temp = new TreeMap<Integer, DataBlock>();
            while (last != null) {
                if (last.value.isChanged()) {
                    temp.put(last.key, last.value);
                }
                hashMap.remove(last.key);
                removeLast();
            }
            for (Iterator<Integer> it = temp.keySet().iterator(); it.hasNext(); ) {
                int bno = it.next();
                try {
                    raf.seek((long) bno * blockSize);
                    raf.write(temp.get(bno).store());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void moveToFirst(Pair pair) {
            if (pair == first)
                return;
            if (pair.pre != null)
                pair.pre.next = pair.next;
            if (pair.next != null)
                pair.next.pre = pair.pre;
            if (pair == last)
                last = last.pre;
            if (first == null || last == null) {
                first = last = pair;
                return;
            }
            pair.next = first;
            first.pre = pair;
            first = pair;
            pair.pre = null;
        }

        private void removeLast() {
            if (last != null) {
                last = last.pre;
                if (last == null)
                    first = null;
                else
                    last.next = null;
            }
        }

        private Pair getEntry(Integer key) {
            return hashMap.get(key);
        }

        class Pair {
            public Pair pre;
            public Pair next;
            public Integer key;
            public DataBlock value;
        }
    }

    private abstract class Block {
        protected byte[] data;
        private boolean changed;

        public Block() {
            data = new byte[blockSize];
            this.changed = false;
        }

        public byte[] store() {
            return data;
        }

        public void load(byte[] data) {
            this.setChanged(false);
            this.data = data;
        }

        public boolean isChanged() {
            return this.changed;
        }

        public void setChanged(boolean changed) {
            this.changed = changed;
        }
    }

    private class NodeBlock extends Block {
        public NodeBlock() {
            super.setChanged(false);
        }

        public NodeBlock(int type) {
            super.setChanged(true);
            if (type == 1) {
                data[0] = 1;
            } else if (type == 2) {
                data[0] = 2;
            }
        }

        public iNode getNode(int offset) {
            iNode node = null;
            int length = 0;
            if (data[0] == 1) {
                length = internsize;
                node = new InternalNode();
            } else if (data[0] == 2) {
                length = leafnsize;
                node = new LeafNode();
            }
            System.arraycopy(data, offset * length + 1, nodeFlash, 0, length);
            node.deserialize(nodeFlash);
            return node;
        }

        public void setNode(iNode node, int offset) {
            node.serialize();
            int length = data[0] == 1 ? internsize : leafnsize;
            System.arraycopy(nodeFlash, 0, this.data, offset * length + 1, length);
            this.setChanged(true);
        }
    }

    private class DataBlock extends Block {

        public byte[] getData(int offset, int length) {
            byte[] data = new byte[length];
            System.arraycopy(super.data, offset, data, 0, length);
            return data;
        }

        public void setData(byte[] data, int begin, int len, int offset) {
            System.arraycopy(data, begin, super.data, offset, len);
            this.setChanged(true);
        }

    }

    public static class IteratorEntry<T, V> {
        private T key;
        private V value;

        public IteratorEntry(T key, V value) {
            this.key = key;
            this.value = value;
        }

        public T getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Entry [key=" + key + ", value=" + value + "]";
        }

    }

    static class MergeEntry<T extends Comparable<T>, V> implements Comparable<MergeEntry<T, V>> {
        private T key;
        private V value;
        private int index;

        public MergeEntry(T key, V value, int index) {
            this.key = key;
            this.value = value;
            this.index = index;
        }

        public T getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return "Entry [key=" + key + ", value=" + value + "]";
        }

        @Override
        public int compareTo(MergeEntry<T, V> o) {
            // TODO Auto-generated method stub
            return key.compareTo(o.key);
        }

    }

    public int getSize() {
        return size;
    }

    public long getReadNo() {
        return readNo;
    }

    public void setReadNo(long readNo) {
        this.readNo = readNo;
    }

    public long getWriteNo() {
        return writeNo;
    }

    public void setWriteNo(long writeNo) {
        this.writeNo = writeNo;
    }

    public int getInterNum() {
        return InterNum;
    }

    public int getLeafNum() {
        return LeafNum;
    }

    public int getDataBlockNum() {
        return dataBlockNum;
    }

    public int getInternsize() {
        return internsize;
    }

    public int getLeafnsize() {
        return leafnsize;
    }

    public void setNodecachesize(int nodecachesize) {
        this.nodecachesize = nodecachesize;
    }

    public int getNodeCacheSize() {
        // TODO Auto-generated method stub
        return this.nodecachesize;
    }
}
