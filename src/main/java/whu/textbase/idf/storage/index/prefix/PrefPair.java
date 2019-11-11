package whu.textbase.idf.storage.index.prefix;

import whu.textbase.btree.api.LengthedPair;

public class PrefPair extends LengthedPair {

    private int[] tokenEx;
    private double accu;

    public PrefPair(int id, double length, double accu, int[] tokenEx) {
        super(id, length);
        this.accu = accu;
        this.tokenEx = tokenEx;
    }

    public int[] getTokenEx() {
        return tokenEx;
    }

    public double getAccu() {
        return accu;
    }
}
