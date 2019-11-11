package whu.textbase.idf.storage.index.prefix;

import whu.textbase.btree.common.Utils;
import whu.textbase.btree.serialize.iSerializable;

import java.util.ArrayList;
import java.util.List;

public class IdfTokenSet implements iSerializable {

    List<Integer> record;

    public IdfTokenSet() {
    }

    public IdfTokenSet(List<Integer> record) {
        this.record = record;
    }

    @Override public void deseriablize(byte[] data) {
        // TODO Auto-generated method stub
        int size = data.length / 4;
        record = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            record.add(Utils.getInt(data, i * 4));
        }
    }

    @Override public byte[] serialize() {
        // TODO Auto-generated method stub
        byte[] data = new byte[record.size() * 4];
        int pos = 0;
        for (int i = 0; i < record.size(); i++) {
            int temp = record.get(i);
            data[pos++] = (byte) (temp & 0xff);
            data[pos++] = (byte) ((temp & 0xff00) >> 8);
            data[pos++] = (byte) ((temp & 0xff0000) >> 16);
            data[pos++] = (byte) ((temp & 0xff000000) >> 24);
        }
        return data;
    }

    public List<Integer> getRecord() {
        return record;
    }
}
