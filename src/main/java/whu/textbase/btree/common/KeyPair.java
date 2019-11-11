package whu.textbase.btree.common;

import whu.textbase.btree.serialize.iSerializable;

public class KeyPair implements Comparable<KeyPair>, iSerializable {

    public int id;
    public double len;

    public KeyPair() {

    }

    public KeyPair(int id, double len) {
        this.id = id;
        this.len = len;
    }

    @Override public void deseriablize(byte[] data) {
        // TODO Auto-generated method stub
        int pos = 0;
        id = Utils.getInt(data, pos);
        pos += 4;
        len = Utils.getDouble(data, pos);
        pos += 8;
    }

    @Override public byte[] serialize() {
        // TODO Auto-generated method stub
        byte[] data = new byte[12];
        int pos = 0;
        data[pos++] = (byte) (id & 0xff);
        data[pos++] = (byte) ((id & 0xff00) >> 8);
        data[pos++] = (byte) ((id & 0xff0000) >> 16);
        data[pos++] = (byte) ((id & 0xff000000) >> 24);
        Utils.getDoubleBytes(len, data, pos);
        pos += 8;
        return data;
    }

    @Override public int compareTo(KeyPair o) {
        // TODO Auto-generated method stub
        int cmp = Integer.compare(id, o.id);
        if (cmp == 0) {
            return Double.compare(len, o.len);
        } else {
            return cmp;
        }
    }

    @Override public String toString() {
        return "(" + id + "," + len + ")";
    }

}
