package whu.textbase.tfidf.storage.index;

import whu.textbase.btree.common.Utils;
import whu.textbase.btree.serialize.iSerializable;
import whu.textbase.tfidf.storage.reader.TfTokenPair;

public class TfPrefNodeTuple implements iSerializable {

    public int next;
    public TfPrefPair pair;

    public TfPrefNodeTuple(TfPrefPair pair) {
        this.pair = pair;
    }

    public TfPrefNodeTuple() {
        // TODO Auto-generated constructor stub
    }

    @Override public void deseriablize(byte[] data) {
        // TODO Auto-generated method stub
        int pos = 0;
        next = Utils.getInt(data, pos);
        pos += 4;
        int id = Utils.getInt(data, pos);
        pos += 4;
        double len = Utils.getDouble(data, pos);
        pos += 8;
        double accu = Utils.getDouble(data, pos);
        pos += 8;
        short tf = Utils.getShort(data, pos);
        pos += 2;
        TfTokenPair[] tokenEx = new TfTokenPair[(data.length - pos) / 6];
        for (int i = 0; i < tokenEx.length; i++) {
            int tidtemp = Utils.getInt(data, pos);
            pos += 4;
            short tftemp = Utils.getShort(data, pos);
            pos += 2;
            tokenEx[i] = new TfTokenPair(tidtemp, tftemp);
        }
        pair = new TfPrefPair(id, len, accu, tokenEx, tf);

    }

    @Override public byte[] serialize() {
        // TODO Auto-generated method stub
        TfTokenPair[] tokenEx = ((TfPrefPair) pair).getTokenEx();
        byte[] data = new byte[26 + tokenEx.length * 6];
        int pos = 0;
        Utils.getBytes4(next, data, pos);
        pos += 4;
        Utils.getBytes4(pair.getId(), data, pos);
        pos += 4;
        Utils.getDoubleBytes(pair.getLen(), data, pos);
        pos += 8;
        Utils.getDoubleBytes(pair.getAccu(), data, pos);
        pos += 8;
        Utils.getBytes2(((TfPrefPair) pair).getTf(), data, pos);
        pos += 2;
        for (int i = 0; i < tokenEx.length; i++) {
            Utils.getBytes4(tokenEx[i].getTid(), data, pos);
            pos += 4;
            Utils.getBytes2(tokenEx[i].getTf(), data, pos);
            pos += 2;
        }
        return data;
    }

    @Override public String toString() {
        // TODO Auto-generated method stub
        return pair.toString();
    }
}