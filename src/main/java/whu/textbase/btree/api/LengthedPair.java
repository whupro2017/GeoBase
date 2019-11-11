package whu.textbase.btree.api;

public abstract class LengthedPair implements Comparable<LengthedPair> {

    protected int id;
    protected double weightedLen;

    public LengthedPair(int id, double len) {
        this.id = id;
        this.weightedLen = len;
    }

    public int getId() {
        return this.id;
    }

    public double getLen() {
        return this.weightedLen;
    }

    @Override public String toString() {
        return "Pair [id=" + id + ", len=" + weightedLen + "]";
    }

    @Override public int compareTo(LengthedPair o) {
        // TODO Auto-generated method stub
        int cmp = Double.compare(this.getLen(), o.getLen());
        if (cmp == 0) {
            return Integer.compare(this.getId(), o.getId());
        } else {
            return cmp;
        }
    }

}
