package whu.textbase.tfidf.storage.reader;

public class TfTokenPair implements Comparable<TfTokenPair> {

    int tid;
    short tf;

    public TfTokenPair(int tokenId, short tf) {
        this.tid = tokenId;
        this.tf = tf;
        // TODO Auto-generated constructor stub
    }

    public short getTf() {
        return tf;
    }
    
    public int getTid() {
        return tid;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "[" + tid + " ," + tf + "]";
    }

    @Override
    public int compareTo(TfTokenPair o) {
        // TODO Auto-generated method stub
        return Integer.compare(tid, o.tid);
    }

}
