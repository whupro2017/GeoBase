package whu.textbase.idf.storage.index.prefix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;


import whu.textbase.btree.common.KeyPair;
import whu.textbase.btree.common.Tools;
import whu.textbase.btree.core.Btree;
import whu.textbase.btree.core.BtreeCluster;
import whu.textbase.btree.core.BtreeCluster.IteratorEntry;
import whu.textbase.btree.utils.btree.BtreeClusterSp2;
import whu.textbase.idf.storage.index.IdfHeadTuple;
import whu.textbase.idf.storage.index.LinkList;
import whu.textbase.idf.storage.index.ListNode;

public class IdfPrefMergeIndex {

    private Btree<Integer, IdfTokenSet> recordBtree;
    private BtreeClusterSp2<KeyPair, Integer> lengthBtree;
    private BtreeCluster<Integer, PrefNodeTuple> reverseBtree;
    private BtreeCluster<Integer, IdfHeadTuple> tokenBtree;

    public IdfPrefMergeIndex(String path, Map<Integer, Double> idfMap, IdfPrefConfig conf, int rExtendedNum,
                             int lengthIndexGap, int part, int partNodeCacheSize) {

        String[] tokenPathArray = new String[part];
        List<BtreeCluster<Integer, IdfHeadTuple>> tokenBtreeList = new ArrayList<BtreeCluster<Integer, IdfHeadTuple>>();
        for (int i = 0; i < part; i++) {
            // Specified parameters for local structures. initialization.
            tokenPathArray[i] = conf.getIndexPath() + ".part" + i;
            BtreeCluster<Integer, IdfHeadTuple> temp =
                    new BtreeCluster<Integer, IdfHeadTuple>(conf.getTokenNodeNumOfBlock(), tokenPathArray[i],
                            conf.getTokenCacheSize() / part, conf.getTokenBlockSize(), conf.getCachefac());
            temp.setNodeCacheSize(partNodeCacheSize);
            tokenBtreeList.add(temp);
        }
        tokenBtree = new BtreeCluster<Integer, IdfHeadTuple>(conf.getTokenNodeNumOfBlock(), conf.getIndexPath(),
                conf.getTokenCacheSize(), conf.getTokenBlockSize(), conf.getCachefac());
        tokenBtree.setNodeCacheSize(conf.getTokenNodeCacheSize());
        String recordPath =
                conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1) + "recordBtree";
        recordBtree = new Btree<Integer, IdfTokenSet>(conf.getRecordNodeNumOfBlock(), recordPath,
                (int) (conf.getRecordCacheSize()), conf.getRecordBlockSize(), (int) (conf.getRecordDataCacheSize()),
                conf.getCachefac());
        recordBtree.setNodecachesize(conf.getRecordNodeCacheSize());
        String reversePath =
                conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1) + "reverseBtree";
        String[] reversePathArray = new String[part];
        List<BtreeCluster<Integer, PrefNodeTuple>> reverseBtreeList =
                new ArrayList<BtreeCluster<Integer, PrefNodeTuple>>();
        for (int i = 0; i < part; i++) {
            // Specified parameters for local structures. record index.
            reversePathArray[i] = reversePath + ".part" + i;
            BtreeCluster<Integer, PrefNodeTuple> temp =
                    new BtreeCluster<Integer, PrefNodeTuple>(conf.getReverseNodeNumOfBlock(), reversePathArray[i],
                            conf.getReverseCacheSize() / part, conf.getReverseBlockSize(), conf.getCachefac());
            temp.setNodeCacheSize(partNodeCacheSize);
            reverseBtreeList.add(temp);
        }
        reverseBtree = new BtreeCluster<Integer, PrefNodeTuple>(conf.getReverseNodeNumOfBlock(), reversePath,
                conf.getReverseCacheSize(), conf.getReverseBlockSize(), conf.getCachefac());
        reverseBtree.setNodeCacheSize(conf.getReverseNodeCacheSize());
        String lengthPath =
                conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1) + "lengthBtree";
        lengthBtree = new BtreeClusterSp2<>(conf.getLengthNodeNumOfBlock(), lengthPath, conf.getLengthCacheSize(),
                conf.getLengthBlockSize(), conf.getCachefac());
        lengthBtree.setNodeCacheSize(conf.getLengthNodeCacheSize());
        long start = System.currentTimeMillis();
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(new File(path + ".tmp")), "utf-8"));

            String line = "";
            int recordCount = 0;
            while ((line = reader.readLine()) != null) {
                String[] recordString = line.split(" ");
                List<Integer> record = new ArrayList<Integer>();
                for (int i = 0; i < recordString.length; i++) {
                    record.add(Integer.parseInt(recordString[i]));
                }
                record.sort((a, b) -> {
                    int cmp = idfMap.get(b).compareTo(idfMap.get(a));
                    if (cmp == 0) {
                        return a.compareTo(b);
                    } else {
                        return cmp;
                    }
                });
                recordBtree.insert(recordCount++, new IdfTokenSet(record));
            }
            reader.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("recordBtree cost " + (end - start));
        int gapp = recordBtree.getSize() / part;
        int count_addr = 0;

        start = System.currentTimeMillis();

        for (int curp = 0; curp < part; curp++) {
            // Specified parameters for local structures. inverted index.
            Map<Integer, LinkList> reverseList = new HashMap<Integer, LinkList>();
            int border = (curp != part - 1) ? (curp + 1) * gapp : recordBtree.getSize();
            for (int i = curp * gapp; i < border; i++) {
                List<Integer> strList = recordBtree.find(i).getRecord();
                strList.sort((a, b) -> {
                    int cmp = idfMap.get(b).compareTo(idfMap.get(a));
                    if (cmp == 0) {
                        return a.compareTo(b);
                    } else {
                        return cmp;
                    }
                });
                Double l2length = Tools.awCompute(strList, idfMap);
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
                    accu += Tools.cbCompute(idf);
                }

            }
            int addr = 0;
            for (Iterator<Map.Entry<Integer, LinkList>> iter = reverseList.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<Integer, LinkList> e = iter.next();
                LinkList list = e.getValue();
                ListNode it = list.head.next;
                double idf = idfMap.get(e.getKey());
                tokenBtreeList.get(curp).insert(e.getKey(), new IdfHeadTuple(addr, idf));
                while (it.next != null) {
                    PrefNodeTuple node = new PrefNodeTuple((PrefPair) it.pair);
                    node.next = addr + 1;
                    reverseBtreeList.get(curp).insert(addr, node);
                    addr++;
                    it = it.next;
                }
                PrefNodeTuple node = new PrefNodeTuple((PrefPair) it.pair);
                node.next = -1;
                reverseBtreeList.get(curp).insert(addr, node);
                addr++;
            }
            count_addr += addr;
            //            if (curp == 0) {
            //                System.out.println("first curp");
            //                System.out.println(curp + " hhh");
            //            }
            if (curp == part - 1) {
                System.out.println("last curp");
                System.out.println("cache size " + tokenBtreeList.get(curp).getCacheSize() + " : "
                        + tokenBtreeList.get(0).getCacheSize());
                System.out.println("node cache size " + tokenBtreeList.get(curp).getNodeCacheSize() + " : "
                        + tokenBtreeList.get(0).getNodeCacheSize());
                System.out.println(curp + " hhh");
            }
        }

        end = System.currentTimeMillis();
        System.out.println("part file cost " + (end - start));
        System.out.println("count addr " + count_addr);
        for (int i = 0; i < part; i++) {
            System.out.println("part " + i + " size " + tokenBtreeList.get(i).getSize());
        }

        start = System.currentTimeMillis();

        List<Iterator<IteratorEntry<Integer, IdfHeadTuple>>> iteratorList =
                new ArrayList<Iterator<IteratorEntry<Integer, IdfHeadTuple>>>();
        PriorityQueue<TokenMergeEntry> tokenHeap = new PriorityQueue<TokenMergeEntry>(part);
        for (int i = 0; i < part; i++) {
            // Specified parameters for local structures. tokens
            Iterator<IteratorEntry<Integer, IdfHeadTuple>> it = tokenBtreeList.get(i).iterator();
            if (it.hasNext()) {
                IteratorEntry<Integer, IdfHeadTuple> tempItEntry = it.next();
                tokenHeap.add(new TokenMergeEntry(tempItEntry.getKey(), tempItEntry.getValue(), i));
            }
            iteratorList.add(it);
        }
        int addr = 0, listCount = 0;
        boolean isRepeat = false;
        while (!tokenHeap.isEmpty()) {
            TokenMergeEntry bEntry = tokenHeap.remove();
            Iterator<IteratorEntry<Integer, IdfHeadTuple>> it = iteratorList.get(bEntry.getIndex());
            if (it.hasNext()) {
                IteratorEntry<Integer, IdfHeadTuple> tempItEntry = it.next();
                tokenHeap.add(new TokenMergeEntry(tempItEntry.getKey(), tempItEntry.getValue(), bEntry.getIndex()));
            }
            List<TokenMergeEntry> entryList = new ArrayList<TokenMergeEntry>();
            entryList.add(bEntry);
            tokenBtree.insert(bEntry.getToken(), new IdfHeadTuple(addr, bEntry.getTuple().idf));
            while (!tokenHeap.isEmpty()) {
                TokenMergeEntry eEntry = tokenHeap.peek();
                if (bEntry.compareTo(eEntry) != 0) {
                    break;
                }
                entryList.add(eEntry);
                tokenHeap.remove();
                it = iteratorList.get(eEntry.getIndex());
                if (it.hasNext()) {
                    IteratorEntry<Integer, IdfHeadTuple> tempItEntry = it.next();
                    tokenHeap.add(new TokenMergeEntry(tempItEntry.getKey(), tempItEntry.getValue(), eEntry.getIndex()));
                }
            }
            PriorityQueue<PrefListMergeEntry> listHeap = new PriorityQueue<PrefListMergeEntry>(entryList.size());
            for (int i = 0; i < entryList.size(); i++) {
                TokenMergeEntry tEntry = entryList.get(i);
                PrefNodeTuple prefTuple = reverseBtreeList.get(tEntry.getIndex()).find(tEntry.getTuple().head);
                if (prefTuple != null) {
                    listHeap.add(new PrefListMergeEntry(prefTuple.pair, tEntry.getIndex(), prefTuple.next));
                }
            }
            while (!listHeap.isEmpty()) {
                PrefListMergeEntry tempEntry = listHeap.remove();
                //                System.out.println(bEntry.getToken() + " addr " + addr + " " + tempEntry);
                PrefNodeTuple nextTuple = reverseBtreeList.get(tempEntry.getIndex()).find(tempEntry.getNext());
                PrefNodeTuple tempTuple = new PrefNodeTuple(tempEntry.getLpair());
                //                System.out.println("listCount " + listCount + " " + nextTuple + " " + lengthIndexGap);

                if (nextTuple != null) {
                    listHeap.add(new PrefListMergeEntry(nextTuple.pair, tempEntry.getIndex(), nextTuple.next));
                }

                // Global parameters for length btree. length.
                if (listHeap.isEmpty()) {
                    tempTuple.next = -1;
                    if (listCount != 0 && listCount % lengthIndexGap == 0) {
                        if (!isRepeat) {
                            lengthBtree.insert(new KeyPair(bEntry.getToken(), tempTuple.pair.getLen()), addr);
                        }
                    }
                    listCount = 0;
                    isRepeat = false;
                } else {
                    tempTuple.next = addr + 1;
                    if (listCount != 0 && listCount % lengthIndexGap == 0) {
                        if (!isRepeat) {
                            lengthBtree.insert(new KeyPair(bEntry.getToken(), tempTuple.pair.getLen()), addr);
                            listCount++;
                        }
                    } else {
                        listCount++;
                    }
                    if (listHeap.peek().getLpair().getLen() == tempTuple.pair.getLen()) {
                        isRepeat = true;
                    } else {
                        isRepeat = false;
                    }
                }
                reverseBtree.insert(addr, tempTuple);
                addr++;
            }
        }
        end = System.currentTimeMillis();
        System.out.println("merge cost " + (end - start));
        System.out.println("addr " + addr);
    }

    private IdfPrefMergeIndex(String path, boolean allMemory) {
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

    private IdfPrefMergeIndex(String path, boolean allMemory, int tokenCacheSize, int reverseCacheSize,
                              int recordCacheSize, int recordDataCacheSize, int lengthCacheSize) {
        String recordPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "recordBtree";
        recordBtree = new Btree<Integer, IdfTokenSet>();
        recordBtree.open(recordPath, recordCacheSize, recordDataCacheSize);
        String reversePath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "reverseBtree";
        reverseBtree = new BtreeCluster<Integer, PrefNodeTuple>();
        reverseBtree.open(reversePath, reverseCacheSize);
        tokenBtree = new BtreeCluster<Integer, IdfHeadTuple>();
        tokenBtree.open(path, tokenCacheSize);
        String lengthPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "lengthBtree";
        lengthBtree = new BtreeClusterSp2<KeyPair, Integer>();
        lengthBtree.open(lengthPath, lengthCacheSize);
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

    public void close() {
        // TODO Auto-generated method stub
        recordBtree.close();
        tokenBtree.close();
        reverseBtree.close();
        lengthBtree.close();
    }

    public static IdfPrefMergeIndex open(String path, boolean allMemory) {
        IdfPrefMergeIndex index = new IdfPrefMergeIndex(path, allMemory);
        return index;
    }

    public static IdfPrefMergeIndex open(String path, boolean allMemory, int tokenCacheSize, int reverseCacheSize,
                                         int recordCacheSize, int recordDataCacheSize, int lengthCacheSize) {
        IdfPrefMergeIndex index = new IdfPrefMergeIndex(path, allMemory, tokenCacheSize, reverseCacheSize,
                recordCacheSize, recordDataCacheSize, lengthCacheSize);
        return index;
    }

    public BtreeCluster<Integer, PrefNodeTuple> getReverseBtree() {
        // TODO Auto-generated method stub
        return reverseBtree;
    }

    public BtreeCluster<Integer, IdfHeadTuple> getTokenBtree() {
        // TODO Auto-generated method stub
        return tokenBtree;
    }
}
