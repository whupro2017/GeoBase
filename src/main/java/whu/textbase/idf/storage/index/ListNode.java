package whu.textbase.idf.storage.index;

import whu.textbase.btree.api.LengthedPair;

public class ListNode implements Comparable<ListNode> {
    public ListNode next, pre;
    public LengthedPair pair;

    public ListNode(LengthedPair pair) {
        this.pair = pair;
    }

    @Override public String toString() {
        return "ListNode [pair=" + pair + "]";
    }

    @Override public int compareTo(ListNode arg0) {
        // TODO Auto-generated method stub
        return pair.compareTo(arg0.pair);
    }
}
