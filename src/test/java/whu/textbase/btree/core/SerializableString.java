package whu.textbase.btree.core;

import whu.textbase.btree.serialize.iSerializable;

class SerializableString implements iSerializable, Comparable<SerializableString> {
    private String string;

    public SerializableString() {
    }

    public SerializableString(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public byte[] serialize() {
        return string.getBytes();
    }

    @Override
    public void deseriablize(byte[] data) {
        string = new String(data);
    }

    @Override
    public int compareTo(SerializableString o) {
        return string.compareTo(o.getString());
    }

    @Override
    public String toString() {
        return string;
    }
}