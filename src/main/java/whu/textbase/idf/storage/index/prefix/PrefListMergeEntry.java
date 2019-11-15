package whu.textbase.idf.storage.index.prefix;

public class PrefListMergeEntry implements Comparable<PrefListMergeEntry> {
    private PrefPair lpair;
    private int index;
    private int next;

    public PrefListMergeEntry(PrefPair lpair, int index, int next) {
        this.lpair = lpair;
        this.index = index;
        this.next = next;
    }

    public PrefPair getLpair() {
        return lpair;
    }

    public int getIndex() {
        return index;
    }

    public int getNext() {
        return next;
    }

    @Override
    public String toString() {
        return "Entry [lpair=" + lpair + ", index=" + index + "]";
    }

    @Override
    public int compareTo(PrefListMergeEntry o) {
        // TODO Auto-generated method stub
        return lpair.compareTo(o.lpair);
    }

}