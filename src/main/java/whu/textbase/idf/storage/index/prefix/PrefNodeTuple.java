package whu.textbase.idf.storage.index.prefix;

import whu.textbase.btree.common.Utils;
import whu.textbase.btree.serialize.iSerializable;

public class PrefNodeTuple implements iSerializable {

    public int next;
    public PrefPair pair;

    public PrefNodeTuple(PrefPair pair) {
        this.pair = pair;
    }

    public PrefNodeTuple() {
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
        int[] tokenEx = new int[(data.length - pos) / 4];
        for (int i = 0; i < tokenEx.length; i++) {
            tokenEx[i] = Utils.getInt(data, pos);
            pos += 4;
        }
        pair = new PrefPair(id, len, accu, tokenEx);

    }

    @Override public byte[] serialize() {
        // TODO Auto-generated method stub
        int[] tokenEx = ((PrefPair) pair).getTokenEx();
        byte[] data = new byte[24 + tokenEx.length * 4];
        int pos = 0;
        data[pos++] = (byte) (next & 0xff);
        data[pos++] = (byte) ((next & 0xff00) >> 8);
        data[pos++] = (byte) ((next & 0xff0000) >> 16);
        data[pos++] = (byte) ((next & 0xff000000) >> 24);
        data[pos++] = (byte) (pair.getId() & 0xff);
        data[pos++] = (byte) ((pair.getId() & 0xff00) >> 8);
        data[pos++] = (byte) ((pair.getId() & 0xff0000) >> 16);
        data[pos++] = (byte) ((pair.getId() & 0xff000000) >> 24);
        Utils.getDoubleBytes(pair.getLen(), data, pos);
        pos += 8;
        Utils.getDoubleBytes(pair.getAccu(), data, pos);
        pos += 8;
        for (int i = 0; i < tokenEx.length; i++) {
            data[pos++] = (byte) (tokenEx[i] & 0xff);
            data[pos++] = (byte) ((tokenEx[i] & 0xff00) >> 8);
            data[pos++] = (byte) ((tokenEx[i] & 0xff0000) >> 16);
            data[pos++] = (byte) ((tokenEx[i] & 0xff000000) >> 24);
        }
        return data;
    }
}