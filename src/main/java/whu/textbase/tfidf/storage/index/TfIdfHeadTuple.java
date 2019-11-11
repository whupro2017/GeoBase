package whu.textbase.tfidf.storage.index;

import whu.textbase.btree.common.Utils;
import whu.textbase.btree.serialize.iSerializable;

public class TfIdfHeadTuple implements iSerializable {
    private int head;
    private double idf;
    private short tfmax;

    public TfIdfHeadTuple() {

    }

    public TfIdfHeadTuple(int head, double idf, int tfmax) {
        this.head = head;
        this.idf = idf;
        this.tfmax = (short) tfmax;
    }

    @Override public void deseriablize(byte[] data) {
        // TODO Auto-generated method stub
        int pos = 0;
        head = Utils.getInt(data, pos);
        pos += 4;
        idf = Utils.getDouble(data, pos);
        pos += 8;
        tfmax = Utils.getShort(data, pos);
        pos += 2;
    }

    @Override public byte[] serialize() {
        // TODO Auto-generated method stub
        byte[] data = new byte[14];
        int pos = 0;
        Utils.getBytes4(head, data, pos);
        pos += 4;
        Utils.getDoubleBytes(idf, data, pos);
        pos += 8;
        Utils.getBytes2(tfmax, data, pos);
        pos += 2;
        return data;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public double getIdf() {
        return idf;
    }

    public void setIdf(double idf) {
        this.idf = idf;
    }

    public short getTfmax() {
        return tfmax;
    }

    public void setTfmax(short tfmax) {
        this.tfmax = tfmax;
    }
}
