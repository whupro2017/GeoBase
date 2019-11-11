package whu.textbase.btree.serialize;

/*
 user-definited type for btree should implements this interface
 */
public interface iSerializable {

    public abstract byte[] serialize();// convert object to byte data

    public abstract void deseriablize(byte[] data);// recover object from byte data
}
