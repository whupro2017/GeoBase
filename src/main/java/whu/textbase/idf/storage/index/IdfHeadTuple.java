package whu.textbase.idf.storage.index;

import whu.textbase.btree.common.Utils;
import whu.textbase.btree.serialize.iSerializable;

public class IdfHeadTuple implements iSerializable {
    public int head;
    public double idf;

    public IdfHeadTuple() {

    }

    public IdfHeadTuple(int head, double idf) {
        this.head = head;
        this.idf = idf;
    }

    @Override public void deseriablize(byte[] data) {
        // TODO Auto-generated method stub
        int pos = 0;
        head = Utils.getInt(data, pos);
        pos += 4;
        idf = Utils.getDouble(data, pos);
        pos += 8;
    }

    @Override public byte[] serialize() {
        // TODO Auto-generated method stub
        byte[] data = new byte[12];
        int pos = 0;
        data[pos++] = (byte) (head & 0xff);
        data[pos++] = (byte) ((head & 0xff00) >> 8);
        data[pos++] = (byte) ((head & 0xff0000) >> 16);
        data[pos++] = (byte) ((head & 0xff000000) >> 24);
        Utils.getDoubleBytes(idf, data, pos);
        pos += 8;
        return data;
    }

    @Override public String toString() {
        // TODO Auto-generated method stub
        return " addr:" + head + " idf:" + idf + " ";
    }
}
