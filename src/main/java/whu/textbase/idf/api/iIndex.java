package whu.textbase.idf.api;

import whu.textbase.btree.common.KeyPair;
import whu.textbase.btree.core.Btree;
import whu.textbase.btree.core.BtreeCluster;
import whu.textbase.btree.utils.btree.BtreeClusterSp2;
import whu.textbase.idf.storage.index.IdfHeadTuple;
import whu.textbase.idf.storage.index.prefix.IdfTokenSet;
import whu.textbase.idf.storage.index.prefix.PrefNodeTuple;

import java.util.Map;

public interface iIndex {

    //    protected BtreeCluster<Integer, iNodeTuple> reverseBtree;
    //    protected BtreeCluster<Integer, HeadTuple> tokenBtree;

    public BtreeCluster<Integer, PrefNodeTuple> getReverseBtree();

    public BtreeCluster<Integer, IdfHeadTuple> getTokenBtree();

    public Btree<Integer, IdfTokenSet> getRecordBtree();

    public BtreeClusterSp2<KeyPair, Integer> getLengthBtree();

    public abstract void printReverseList(Map<Integer, String> idToTokenMap, Map<Integer, String> recordMap);

    public abstract void close();

}
