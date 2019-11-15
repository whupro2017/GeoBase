package whu.textbase.idf.storage.index.prefix;

import whu.textbase.idf.storage.index.IdfHeadTuple;

public class TokenMergeEntry implements Comparable<TokenMergeEntry> {
    private int token;
    private IdfHeadTuple tuple;
    private int index;

    public TokenMergeEntry(int token, IdfHeadTuple tuple, int index) {
        this.token = token;
        this.tuple = tuple;
        this.index = index;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public int getIndex() {
        return index;
    }

    public IdfHeadTuple getTuple() {
        return tuple;
    }

    public void setTuple(IdfHeadTuple tuple) {
        this.tuple = tuple;
    }

    @Override
    public int compareTo(TokenMergeEntry o) {
        // TODO Auto-generated method stub
        return token - o.token;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "tokenid:" + token + " " + tuple + " " + "index:" + index;
    }
}
