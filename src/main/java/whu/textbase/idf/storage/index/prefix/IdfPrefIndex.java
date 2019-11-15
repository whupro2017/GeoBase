package whu.textbase.idf.storage.index.prefix;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import whu.textbase.btree.common.KeyPair;
import whu.textbase.btree.common.Tools;
import whu.textbase.btree.core.Btree;
import whu.textbase.btree.core.BtreeCluster;
import whu.textbase.btree.utils.btree.BtreeClusterSp2;
import whu.textbase.idf.api.iIndex;
import whu.textbase.idf.storage.index.IdfHeadTuple;
import whu.textbase.idf.storage.index.LinkList;
import whu.textbase.idf.storage.index.ListNode;

public class IdfPrefIndex implements iIndex {

    private Btree<Integer, IdfTokenSet> recordBtree;
    private BtreeClusterSp2<KeyPair, Integer> lengthBtree;
    private BtreeCluster<Integer, PrefNodeTuple> reverseBtree;
    private BtreeCluster<Integer, IdfHeadTuple> tokenBtree;


    public IdfPrefIndex(List<List<Integer>> recordList, Map<Integer, Double> idfMap, IdfPrefConfig conf,
                        int rExtendedNum, int lengthIndexGap) {
        tokenBtree = new BtreeCluster<Integer, IdfHeadTuple>(conf.getTokenNodeNumOfBlock(), conf.getIndexPath(),
                conf.getTokenCacheSize(), conf.getTokenBlockSize(), conf.getCachefac());
        tokenBtree.setNodeCacheSize(conf.getTokenNodeCacheSize());
        String recordPath = conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1) + "recordBtree";
        recordBtree = new Btree<Integer, IdfTokenSet>(conf.getRecordNodeNumOfBlock(), recordPath,
                (int) (conf.getRecordCacheSize()), conf.getRecordBlockSize(), (int) (conf.getRecordDataCacheSize()),
                conf.getCachefac());
        recordBtree.setNodecachesize(conf.getRecordNodeCacheSize());
        String reversePath = conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1)
                + "reverseBtree";
        reverseBtree = new BtreeCluster<Integer, PrefNodeTuple>(conf.getReverseNodeNumOfBlock(), reversePath,
                conf.getReverseCacheSize(), conf.getReverseBlockSize(), conf.getCachefac());
        reverseBtree.setNodeCacheSize(conf.getReverseNodeCacheSize());
        String lengthPath = conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1) + "lengthBtree";
        lengthBtree = new BtreeClusterSp2<>(conf.getLengthNodeNumOfBlock(), lengthPath, conf.getLengthCacheSize(),
                conf.getLengthBlockSize(), conf.getCachefac());
        lengthBtree.setNodeCacheSize(conf.getLengthNodeCacheSize());
        Map<Integer, LinkList> reverseList = new HashMap<Integer, LinkList>();
        for (int i = 0; i < recordList.size(); i++) {
            List<Integer> strList = recordList.get(i);
            strList.sort((a, b) -> {
                int cmp = idfMap.get(b).compareTo(idfMap.get(a));
                if (cmp == 0) {
                    return a.compareTo(b);
                } else {
                    return cmp;
                }
            });
            recordBtree.insert(i, new IdfTokenSet(strList));
            Double l2length = Tools.l2Compute(strList, idfMap);
            Set<Integer> tmp = new HashSet<Integer>();
            // double prefix = (1 - threshold) * l2length * l2length;
            double accu = 0.0;
            for (int j = 0; j < strList.size(); j++) {
                Integer token = strList.get(j);
                int[] tokenEx = new int[rExtendedNum];
                for (int k = 0; k < rExtendedNum; k++) {
                    if (j + k + 1 < strList.size()) {
                        tokenEx[k] = strList.get(j + k + 1);
                    } else {
                        tokenEx[k] = -1;
                    }
                }
                ListNode temp = new ListNode(new PrefPair(i, l2length, accu, tokenEx));
                if (!tmp.contains(token)) {
                    if (reverseList.containsKey(token)) {
                        LinkList tokenList = reverseList.get(token);
                        tokenList.add(temp);
                    } else {
                        LinkList tokenList = new LinkList();
                        tokenList.add(temp);
                        reverseList.put(token, tokenList);
                    }
                    tmp.add(token);
                }
                double idf = idfMap.get(token);
                accu += idf * idf;

            }
        }
        int addr = 0;
        for (Iterator<Map.Entry<Integer, LinkList>> iter = reverseList.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Integer, LinkList> e = iter.next();
            LinkList list = e.getValue();
            ListNode it = list.head.next;
            double idf = idfMap.get(e.getKey());
            tokenBtree.insert(e.getKey(), new IdfHeadTuple(addr, idf));
            int listCount = 0;
            while (it.next != null) {
                PrefNodeTuple node = new PrefNodeTuple((PrefPair) it.pair);
                node.next = addr + 1;
                reverseBtree.insert(addr, node);
                //                System.out.println(e.getKey() + " addr " + addr + " " + it.pair);
                if (listCount != 0 && listCount % lengthIndexGap == 0) {
                    if (it.pre.pair == null || it.pre.pair.getLen() != it.pair.getLen()) {
                        //                        System.out.println(addr);
                        lengthBtree.insert(new KeyPair(e.getKey(), it.pair.getLen()), addr);
                        listCount++;
                    }
                } else {
                    listCount++;
                }
                addr++;
                it = it.next;
            }
            PrefNodeTuple node = new PrefNodeTuple((PrefPair) it.pair);
            node.next = -1;
            //            System.out.println(e.getKey() + " addr " + addr + " " + it.pair);
            reverseBtree.insert(addr, node);
            if (listCount != 0 && listCount % lengthIndexGap == 0) {
                if (it.pre.pair == null || it.pre.pair.getLen() != it.pair.getLen()) {
                    //                    System.out.println(addr);
                    lengthBtree.insert(new KeyPair(e.getKey(), it.pair.getLen()), addr);
                    listCount++;
                }
            } else {
                listCount++;
            }
            addr++;
        }
        System.out.println("addr " + addr);
    }

    private IdfPrefIndex(String path, boolean allMemory) {
        String recordPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "recordBtree";
        recordBtree = new Btree<Integer, IdfTokenSet>();
        recordBtree.open(recordPath);
        String reversePath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "reverseBtree";
        reverseBtree = new BtreeCluster<Integer, PrefNodeTuple>();
        reverseBtree.open(reversePath);
        tokenBtree = new BtreeCluster<Integer, IdfHeadTuple>();
        tokenBtree.open(path);
        String lengthPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "lengthBtree";
        lengthBtree = new BtreeClusterSp2<KeyPair, Integer>();
        lengthBtree.open(lengthPath);
        if (allMemory) {
            loadAll();
        } else {
            loadPart();
        }
        tokenBtree.setReadNo(0);
        tokenBtree.setWriteNo(0);
        reverseBtree.setReadNo(0);
        reverseBtree.setWriteNo(0);
        recordBtree.setReadNo(0);
        recordBtree.setWriteNo(0);
        lengthBtree.setReadNo(0);
        lengthBtree.setWriteNo(0);
    }

    public void loadAll() {
        tokenBtree.loadAll();
        reverseBtree.loadAll();
        recordBtree.loadAll();
        lengthBtree.loadAll();
    }

    public void loadPart() {
        tokenBtree.loadAll();
        reverseBtree.loadPart();
        recordBtree.loadPart();
        lengthBtree.loadAll();
    }

    public Btree<Integer, IdfTokenSet> getRecordBtree() {
        return recordBtree;
    }

    public BtreeClusterSp2<KeyPair, Integer> getLengthBtree() {
        return lengthBtree;
    }

    public void printReverseList(Map<Integer, String> idToTokenMap, Map<Integer, String> recordMap) {
//      for (Iterator<Map.Entry<Integer, LinkList>> iter = reverseList.entrySet().iterator(); iter.hasNext();) {
//          Map.Entry<Integer, LinkList> e = iter.next();
//          LinkList list = e.getValue();
//          System.out.println(e.getKey() + "---" + idToTokenMap.get(e.getKey()));
//          for (int i = 0; i < list.size(); i++) {
//              // System.out.println(list.get(i).getId() + ":" + list.get(i).getLen() + ":" + list.get(i).getPos());
//              // System.out.println(recordMap.get(list.get(i).getId()));
//          }
//
//      }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        recordBtree.close();
        tokenBtree.close();
        reverseBtree.close();
        lengthBtree.close();
    }

    public static IdfPrefIndex open(String path, boolean allMemory) {
        IdfPrefIndex index = new IdfPrefIndex(path, allMemory);
        return index;
    }

    @Override
    public BtreeCluster<Integer, PrefNodeTuple> getReverseBtree() {
        // TODO Auto-generated method stub
        return reverseBtree;
    }

    @Override
    public BtreeCluster<Integer, IdfHeadTuple> getTokenBtree() {
        // TODO Auto-generated method stub
        return tokenBtree;
    }
}
