package whu.textbase.idf.storage.index;

import whu.textbase.btree.api.LengthedPair;
import whu.textbase.btree.utils.skiplist.SkipList;

public class LinkList {
    public ListNode head;
    private SkipList<LengthedPair, ListNode> skip;
    int size, step, size_distinct;

    public LinkList() {
        size = 0;
        size_distinct = 0;
        head = new ListNode(null);
        skip = new SkipList<LengthedPair, ListNode>(20);
    }

    public void add(ListNode temp) {
        ListNode pos = skip.getSp(temp.pair);
        if (pos == null) {
            pos = head;
        }
        temp.next = pos.next;
        pos.next = temp;
        temp.pre = pos;
        if (temp.next != null) {
            temp.next.pre = temp;
        }
        skip.put(temp.pair, temp);
        size++;
    }

    public int size() {
        return size;
    }

    public void print() {
        ListNode p = head.next;
        while (p != null) {
            System.out.println(p);
            p = p.next;
        }
    }
}
