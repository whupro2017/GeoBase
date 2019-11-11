package whu.textbase.tfidf.api;

import whu.textbase.btree.common.KeyPair;
import whu.textbase.btree.core.Btree;
import whu.textbase.btree.core.BtreeCluster;
import whu.textbase.btree.serialize.iSerializable;
import whu.textbase.btree.utils.btree.BtreeClusterSp;
import whu.textbase.btree.utils.btree.BtreeClusterSp2;
import whu.textbase.tfidf.storage.index.TfIdfHeadTuple;
import whu.textbase.tfidf.storage.index.TfTokenSet;

import java.util.Map;

public interface iIndex {

    //    protected BtreeCluster<Integer, iNodeTuple> reverseBtree;
    //    protected BtreeCluster<Integer, HeadTuple> tokenBtree;

    public BtreeCluster<Integer, iSerializable> getReverseBtree();

    public BtreeCluster<Integer, TfIdfHeadTuple> getTokenBtree();

    public Btree<Integer, TfTokenSet> getRecordBtree();

    public BtreeClusterSp2<KeyPair, Integer> getLengthBtree();

    public BtreeClusterSp<KeyPair, Short> getTfmaxBtree();

    public abstract void printReverseList(Map<Integer, String> idToTokenMap, Map<Integer, String> recordMap);

    public abstract void close();

}
