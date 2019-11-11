package whu.textbase.btree.utils.skiplist;

import java.util.Random;

public class SkipList<T extends Comparable<T>, V> {
    public iNode head;

    private int[] base;
    private int size;
    private int maxLevel;
    public int height;
    public Random random;

    public SkipList(int maxLevel) {
        head = new LeafNode();
        height = size = 0;
        random = new Random();
        if (maxLevel > 31) {
            this.maxLevel = 31;
        } else {
            this.maxLevel = maxLevel;
        }
        base = new int[maxLevel];
        generateBase();
    }

    public int size() {
        return size;
    }

    public void generateBase() {
        base[maxLevel - 1] = (2 << (maxLevel - 1)) - 1;
        for (int i = maxLevel - 2, j = 0; i >= 0; i--, j++)
            base[i] = base[i + 1] - (2 << j);
    }

    public int getLevel() {
        int i, r = Math.abs(random.nextInt()) % base[maxLevel - 1] + 1;
        for (i = 1; i < maxLevel; i++)
            if (r < base[i])
                return i - 1;
        return i - 1;
    }

    @SuppressWarnings("unchecked") public iNode findEntry(T k) {
        iNode p = head;

        while (true) {
            while (p.next != null && p.next.key.compareTo(k) < 0) {
                p = p.next;
            }

            if (p instanceof SkipList.InternalNode) {
                p = ((InternalNode) p).down;
            } else
                break;
        }

        return p;
    }

    @SuppressWarnings("unchecked") public V get(T k) {
        iNode p;
        p = findEntry(k);

        if (p.key == null || p.key.compareTo(k) < 0) {
            p = p.next;
        }
        return (((LeafNode) p).value);
    }

    @SuppressWarnings("unchecked") public V getSp(T k) {
        iNode p;
        p = findEntry(k);
        return ((LeafNode) p).value;
    }

    public void put(T k, V v) {

        iNode entry = findEntry(k);

        if (k.equals(entry.key)) {
            return;
        }
        size++;
        LeafNode newLeaf = new LeafNode();
        newLeaf.key = k;
        newLeaf.value = v;
        newLeaf.prev = entry;
        newLeaf.next = entry.next;
        if (entry.next != null) {
            entry.next.prev = newLeaf;
        }
        entry.next = newLeaf;

        int curLevel = 0;

        int level = getLevel();
        iNode p = entry, q = newLeaf;
        while (curLevel < level) {

            if (curLevel >= height) {

                height = height + 1;
                InternalNode temp = new InternalNode();
                temp.down = head;

                head.up = temp;
                head = temp;
            }

            while (p.up == null) {
                p = p.prev;
            }
            p = p.up;

            InternalNode newInter = new InternalNode();
            newInter.key = k;
            newInter.prev = p;
            newInter.next = p.next;
            newInter.down = q;

            if (p.next != null) {
                p.next.prev = newInter;
            }
            p.next = newInter;
            q.up = newInter;

            q = newInter;
            curLevel = curLevel + 1;

        }

        return;
    }

    private class iNode {
        T key;
        public iNode up, prev, next;
    }

    private class InternalNode extends iNode {
        iNode down;
    }

    private class LeafNode extends iNode {
        V value;
    }

    @SuppressWarnings("unchecked") public void print() {
        iNode q = head;
        while (q != null) {
            iNode p = q.next;
            if (q instanceof SkipList.InternalNode) {
                q = ((InternalNode) q).down;
            } else {
                q = null;
            }
            while (p != null) {
                System.out.print(p.key + " ");
                p = p.next;
            }
            System.out.println();
        }
    }
}