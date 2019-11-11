package whu.textbase.tfidf.storage.index;

import whu.textbase.btree.api.LengthedPair;
import whu.textbase.tfidf.storage.reader.TfTokenPair;

public class TfPrefPair extends LengthedPair {

    private TfTokenPair[] tokenEx;
    private double accu;
    private short tf;

    public TfPrefPair(int id, double length, double accu, TfTokenPair[] tokenEx, short tf) {
        super(id, length);
        this.accu = accu;
        this.tokenEx = tokenEx;
        this.tf = tf;
    }

    public TfTokenPair[] getTokenEx() {
        return tokenEx;
    }

    public double getAccu() {
        return accu;
    }

    public short getTf() {
        return tf;
    }
}
