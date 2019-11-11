package whu.textbase.tfidf.storage.index;

import whu.textbase.btree.common.KeyPair;
import whu.textbase.btree.common.TfidfConfig;
import whu.textbase.btree.common.Tools;
import whu.textbase.btree.core.Btree;
import whu.textbase.btree.core.BtreeCluster;
import whu.textbase.btree.serialize.iSerializable;
import whu.textbase.btree.utils.btree.BtreeClusterSp;
import whu.textbase.btree.utils.btree.BtreeClusterSp2;
import whu.textbase.idf.storage.index.LinkList;
import whu.textbase.idf.storage.index.ListNode;
import whu.textbase.tfidf.api.iIndex;
import whu.textbase.tfidf.storage.reader.TfIdfDataReader;
import whu.textbase.tfidf.storage.reader.TfTokenPair;

import java.io.File;
import java.util.*;

public class TfidfPrefIndex implements iIndex {

    private Btree<Integer, TfTokenSet> recordBtree;
    private BtreeClusterSp2<KeyPair, Integer> lengthBtree;
    private BtreeClusterSp<KeyPair, Short> tfmaxBtree;
    private BtreeCluster<Integer, iSerializable> reverseBtree;
    private BtreeCluster<Integer, TfIdfHeadTuple> tokenBtree;

    public TfidfPrefIndex(TfIdfDataReader dataReader, TfidfConfig conf, int rExtendedNum, int lengthIndexGap) {

        List<List<TfTokenPair>> recordList = dataReader.getRecordList();
        Map<Integer, Double> idfMap = dataReader.getIdfMap();
        Map<Integer, Short> tfMaxMap = dataReader.getTfMaxMap();
        String recordPath =
                conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1) + "recordBtree";
        recordBtree = new Btree<Integer, TfTokenSet>(conf.getRecordNodeNumOfBlock(), recordPath,
                (int) (conf.getRecordCacheSize()), conf.getRecordBlockSize(), (int) (conf.getRecordDataCacheSize()),
                conf.getCachefac());
        recordBtree.setNodecachesize(conf.getRecordNodeCacheSize());
        String reversePath =
                conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1) + "reverseBtree";
        reverseBtree = new BtreeCluster<Integer, iSerializable>(conf.getReverseNodeNumOfBlock(), reversePath,
                conf.getReverseCacheSize(), conf.getReverseBlockSize(), conf.getCachefac());
        reverseBtree.setNodeCacheSize(conf.getReverseNodeCacheSize());
        tokenBtree = new BtreeCluster<Integer, TfIdfHeadTuple>(conf.getTokenNodeNumOfBlock(), conf.getIndexPath(),
                conf.getTokenCacheSize(), conf.getTokenBlockSize(), conf.getCachefac());
        tokenBtree.setNodeCacheSize(conf.getTokenNodeCacheSize());
        String lengthPath =
                conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1) + "lengthBtree";
        lengthBtree = new BtreeClusterSp2<>(conf.getLengthNodeNumOfBlock(), lengthPath, conf.getLengthCacheSize(),
                conf.getLengthBlockSize(), conf.getCachefac());
        lengthBtree.setNodeCacheSize(conf.getLengthNodeCacheSize());
        String tfmaxPath =
                conf.getIndexPath().substring(0, conf.getIndexPath().lastIndexOf(File.separator) + 1) + "tfmaxBtree";
        tfmaxBtree = new BtreeClusterSp<>(conf.getTfmaxNodeNumOfBlock(), tfmaxPath, conf.getTfmaxCacheSize(),
                conf.getTfmaxBlockSize(), conf.getCachefac());
        tfmaxBtree.setNodeCacheSize(conf.getTfmaxNodeCacheSize());

        Map<Integer, LinkList> reverseList = new HashMap<Integer, LinkList>();
        int wordCount = 0;
        for (int i = 0; i < recordList.size(); i++) {

            List<TfTokenPair> strList = recordList.get(i);
            strList.sort((a, b) -> {
                int cmp = Double.compare(idfMap.get(b.getTid()), idfMap.get(a.getTid()));
                if (cmp == 0) {
                    int cmp2 = Double.compare(tfMaxMap.get(b.getTid()), tfMaxMap.get(a.getTid()));
                    if (cmp2 == 0) {
                        return a.compareTo(b);
                    } else {
                        return cmp2;
                    }
                } else {
                    return cmp;
                }
            });
            recordBtree.insert(i, new TfTokenSet(strList));
            double tfidfLength = Tools.tfIdfLengthCompute2(strList, idfMap);
            Set<Integer> tmp = new HashSet<Integer>();
            // double prefix = (1 - threshold) * l2length * l2length;
            double accu = 0.0;
            for (int j = 0; j < strList.size(); j++) {
                TfTokenPair pair = strList.get(j);
                wordCount += pair.getTf();
                TfTokenPair[] tokenEx = new TfTokenPair[rExtendedNum];
                for (int k = 0; k < rExtendedNum; k++) {
                    if (j + k + 1 < strList.size()) {
                        tokenEx[k] = new TfTokenPair(strList.get(j + k + 1).getTid(), strList.get(j + k + 1).getTf());
                    } else {
                        tokenEx[k] = new TfTokenPair(-1, (short) -1);
                    }
                }
                ListNode temp = new ListNode(new TfPrefPair(i, tfidfLength, accu, tokenEx, pair.getTf()));
                if (!tmp.contains(pair.getTid())) {
                    if (reverseList.containsKey(pair.getTid())) {
                        LinkList tokenList = reverseList.get(pair.getTid());
                        tokenList.add(temp);
                    } else {
                        LinkList tokenList = new LinkList();
                        tokenList.add(temp);
                        reverseList.put(pair.getTid(), tokenList);
                    }
                    tmp.add(pair.getTid());
                }
                double idf = idfMap.get(pair.getTid());
                accu += idf * pair.getTf() * idf * pair.getTf();

            }
        }
        System.out.println("WordCount " + wordCount);
        int addr = 0;
        TfPrefPair pair = null;
        for (Iterator<Map.Entry<Integer, LinkList>> iter = reverseList.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Integer, LinkList> e = iter.next();
            short tfmaxPre = 0;
            boolean isTfmaxChanged = false;
            LinkList list = e.getValue();
            tokenBtree.insert(e.getKey(), new TfIdfHeadTuple(addr, idfMap.get(e.getKey()), tfMaxMap.get(e.getKey())));
            ListNode it = list.head.next;
            pair = (TfPrefPair) it.pair;
            while (it != null) {
                pair = (TfPrefPair) it.pair;
                if (pair.getTf() > tfmaxPre) {
                    if (it.pre.pair != null) {
                        isTfmaxChanged = true;
                        break;
                    }
                    tfmaxPre = pair.getTf();
                }
                it = it.next;
            }

            it = list.head.next;
            tfmaxPre = 0;
            int listCount = 0;
            while (it.next != null) {
                pair = (TfPrefPair) it.pair;
                TfPrefNodeTuple node = new TfPrefNodeTuple(pair);
                node.next = addr + 1;
                reverseBtree.insert(addr, node);
                //                System.out.println(addr + " " + reverseBtree.find(2553534));
                if (isTfmaxChanged && pair.getTf() > tfmaxPre) {
                    tfmaxPre = pair.getTf();
                    if (it.pre.pair == null || it.pre.pair.getLen() != pair.getLen()) {
                        tfmaxBtree.insert(new KeyPair(e.getKey(), pair.getLen()), tfmaxPre);
                    }
                }
                //                if (listLength >= lengthIndexGap) {
                if (listCount != 0 && listCount % lengthIndexGap == 0) {
                    if (it.pre.pair == null || it.pre.pair.getLen() != it.pair.getLen()) {
                        lengthBtree.insert(new KeyPair(e.getKey(), pair.getLen()), addr);
                        listCount++;
                    }
                } else {
                    listCount++;
                }
                //                }

                addr++;
                it = it.next;
            }
            pair = (TfPrefPair) it.pair;
            TfPrefNodeTuple node = new TfPrefNodeTuple(pair);
            node.next = -1;
            reverseBtree.insert(addr, node);
            //            System.out.println(addr + " " + reverseBtree.find(2553534));
            if (isTfmaxChanged && pair.getTf() > tfmaxPre) {
                tfmaxPre = pair.getTf();
                if (it.pre.pair == null || it.pre.pair.getLen() != pair.getLen()) {
                    tfmaxBtree.insert(new KeyPair(e.getKey(), pair.getLen()), tfmaxPre);
                }
            }
            //            if (listLength >= lengthIndexGap) {
            if (listCount != 0 && listCount % lengthIndexGap == 0) {
                if (it.pre.pair == null || it.pre.pair.getLen() != it.pair.getLen()) {
                    lengthBtree.insert(new KeyPair(e.getKey(), pair.getLen()), addr);
                    listCount++;
                }
            } else {
                listCount++;
            }
            //            }
            addr++;
        }

        double avgIdf = 0.0;
        Map<Integer, Double> avgtfMap = new HashMap<Integer, Double>();
        Map<Integer, Integer> freqMap = dataReader.getFreqMap();
        for (int i = 0; i < idfMap.size(); i++) {
            avgIdf += idfMap.get(i);

        }
        avgIdf /= idfMap.size();

        double sum_idf = 0;
        for (int i = 0; i < idfMap.size(); i++) {
            sum_idf += (idfMap.get(i) - avgIdf) * (idfMap.get(i) - avgIdf);
        }
        sum_idf = Math.sqrt(sum_idf / idfMap.size());

        //        for (int i = 0; i < recordList.size(); i ++){
        //            List<TfTokenPair> record = recordBtree.find(i).getRecord();
        //            for (int j = 0; j < record.size(); j ++){
        //                TfTokenPair token = record.get(j);
        //                if (avgtfMap.containsKey(token.getTid())){
        //                    avgtfMap.put(token.getTid(), avgtfMap.get(token.getTid()) + token.getTf());
        //                } else {
        //                    avgtfMap.put(token.getTid(), (double)token.getTf());
        //                }
        //            }
        //        }
        //        avgtfMap.forEach((key, value) -> {
        //            avgtfMap.put(key, value * 1.0 / freqMap.get(key));
        //        });

        //        double tf_sum = 0, idf_sum = 0, cw_sum = 0;
        //        for (int i = 0; i < recordList.size(); i ++){
        //            List<TfTokenPair> record = recordBtree.find(i).getRecord();
        //            double avg_tf_local = 0, avg_idf_local = 0, avg_cw_local = 0;
        //            for (int j = 0; j < record.size(); j ++){
        //                TfTokenPair token = record.get(j);
        //                avg_tf_local += token.getTf();
        //                avg_idf_local += idfMap.get(token.getTid());
        //                avg_cw_local += token.getTf() * idfMap.get(token.getTid());
        //
        //            }
        //            System.out.println("avg_tf_local " + avg_tf_local + " idf " + avg_idf_local + " cw " + avg_cw_local);
        //            avg_tf_local /= record.size();
        //            avg_idf_local /= record.size();
        //            avg_cw_local /= record.size();
        //            double sum_tf_local = 0, sum_idf_local = 0, sum_cw_local = 0;
        //            for (int j = 0; j < record.size(); j ++){
        //                TfTokenPair token = record.get(j);
        //                sum_tf_local += (token.getTf() - avg_tf_local) * (token.getTf() - avg_tf_local);
        //                double idf = idfMap.get(token.getTid());
        //                sum_idf_local += (idf - avg_idf_local) * (idf - avg_idf_local);
        //                sum_cw_local += (idf * token.getTf() - avg_cw_local) * (idf * token.getTf() - avg_cw_local);
        //            }
        //            tf_sum += Math.sqrt(sum_tf_local / record.size());
        //            idf_sum += Math.sqrt(sum_idf_local / record.size());
        //            cw_sum += Math.sqrt(sum_cw_local / record.size());
        //        }
        //        System.out.println("tf diff " + tf_sum / recordList.size());
        //        System.out.println("idf diff " + idf_sum / recordList.size());
        //        System.out.println("cw diff " + cw_sum / recordList.size());
        //        System.out.println("idf total diff " + sum_idf);
        //        reverseBtree.printTree();
        //        for (int i = 0; i < addr; i++) {
        //            if (reverseBtree.find(i) == null) {
        //                System.out.println(i + " null");
        //            }
        //        }

        System.out.println("idfmap size " + idfMap.size());
        double tf_list_sum = 0, tflen_list_sum = 0, tflenabs_list_sum = 0;
        for (int i = 0; i < idfMap.size(); i++) {
            TfIdfHeadTuple hTuple = tokenBtree.find(i);
            TfPrefNodeTuple curTuple = (TfPrefNodeTuple) reverseBtree.find(hTuple.getHead());
            double avg_tf_sum = 0;
            int count = 0;
            double lenmax = 0;
            int tfmax = 0;
            while (curTuple != null) {
                avg_tf_sum += curTuple.pair.getTf();

                if (curTuple.pair.getLen() > lenmax) {
                    lenmax = curTuple.pair.getLen();

                }
                if (curTuple.pair.getTf() > tfmax) {
                    tfmax = curTuple.pair.getTf();
                }
                count++;
                curTuple = (TfPrefNodeTuple) reverseBtree.find(curTuple.next);
            }
            avg_tf_sum /= count;
            //            System.out.println("count " + count + " freq " + freqMap.get(i));

            curTuple = (TfPrefNodeTuple) reverseBtree.find(hTuple.getHead());
            double local_sum_tf = 0;
            double local_sum_tflen = 0;
            double local_sum_tflenabs = 0;
            while (curTuple != null) {
                local_sum_tf += (curTuple.pair.getTf() - avg_tf_sum) * (curTuple.pair.getTf() - avg_tf_sum);
                local_sum_tflen += Math.pow((curTuple.pair.getLen() / lenmax - curTuple.pair.getTf() / tfmax), 2);
                local_sum_tflenabs += Math.abs(curTuple.pair.getLen() / lenmax - curTuple.pair.getTf() / tfmax);
                curTuple = (TfPrefNodeTuple) reverseBtree.find(curTuple.next);
            }
            tf_list_sum += Math.sqrt(local_sum_tf / count);
            tflen_list_sum += Math.sqrt(local_sum_tflen / count);
            tflenabs_list_sum += local_sum_tflenabs;

        }
        System.out.println("tf list diff " + tf_list_sum / idfMap.size());
        System.out.println("tflen abs list diff " + tflenabs_list_sum / idfMap.size());
        System.out.println("tflen square list diff " + tflen_list_sum / idfMap.size());
    }

    private TfidfPrefIndex(String path, boolean allMemory) {
        String recordPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "recordBtree";
        recordBtree = new Btree<Integer, TfTokenSet>();
        recordBtree.open(recordPath);
        String reversePath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "reverseBtree";
        reverseBtree = new BtreeCluster<Integer, iSerializable>();
        reverseBtree.open(reversePath);
        tokenBtree = new BtreeCluster<Integer, TfIdfHeadTuple>();
        tokenBtree.open(path);
        String lengthPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "lengthBtree";
        lengthBtree = new BtreeClusterSp2<KeyPair, Integer>();
        lengthBtree.open(lengthPath);
        String tfmaxPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "tfmaxBtree";
        tfmaxBtree = new BtreeClusterSp<KeyPair, Short>();
        tfmaxBtree.open(tfmaxPath);
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
        tfmaxBtree.setReadNo(0);
        tfmaxBtree.setWriteNo(0);
    }

    public void loadAll() {
        tokenBtree.loadAll();
        reverseBtree.loadAll();
        recordBtree.loadAll();
        lengthBtree.loadAll();
        tfmaxBtree.loadAll();
    }

    public void loadPart() {
        tokenBtree.loadAll();
        reverseBtree.loadPart();
        recordBtree.loadPart();
        lengthBtree.loadAll();
        tfmaxBtree.loadAll();
    }

    public Btree<Integer, TfTokenSet> getRecordBtree() {
        return recordBtree;
    }

    public BtreeClusterSp<KeyPair, Short> getTfmaxBtree() {
        return tfmaxBtree;
    }

    @Override public BtreeClusterSp2<KeyPair, Integer> getLengthBtree() {
        // TODO Auto-generated method stub
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

    @Override public void close() {
        // TODO Auto-generated method stub
        recordBtree.close();
        tokenBtree.close();
        reverseBtree.close();
        lengthBtree.close();
        tfmaxBtree.close();
    }

    public static iIndex open(String path, boolean allMemory) {
        iIndex index = new TfidfPrefIndex(path, allMemory);
        return index;
    }

    @Override public BtreeCluster<Integer, iSerializable> getReverseBtree() {
        // TODO Auto-generated method stub
        return reverseBtree;
    }

    @Override public BtreeCluster<Integer, TfIdfHeadTuple> getTokenBtree() {
        // TODO Auto-generated method stub
        return tokenBtree;
    }

    public void printTfidfDetails() {
    }
}
