package whu.textbase.tfidf.process;

import whu.textbase.btree.api.iSelect;
import whu.textbase.btree.core.Btree;
import whu.textbase.btree.core.BtreeCluster;
import whu.textbase.tfidf.api.iIndex;
import whu.textbase.tfidf.storage.index.TfIdfHeadTuple;
import whu.textbase.tfidf.storage.index.TfTokenSet;
import whu.textbase.tfidf.storage.reader.TfTokenPair;

import java.util.*;

public class RecordScan implements iSelect {

    private BtreeCluster<Integer, TfIdfHeadTuple> tokenBtree;
    private Btree<Integer, TfTokenSet> recordBtree;

    public RecordScan(iIndex index) {
        this.tokenBtree = index.getTokenBtree();
        this.recordBtree = index.getRecordBtree();
        // TODO Auto-generated constructor stub
    }

    @Override public List<Integer> find(List<Integer> queryString, double threshold) {
        // TODO Auto-generated method stub
        List<Integer> resultList = new ArrayList<Integer>();
        //        Iterator<Entry<Integer, TfTokenSet>> it = recordBtree.iterator();
        Map<Integer, Integer> qTfMap = new HashMap<Integer, Integer>();
        Set<Integer> qSet = new HashSet<Integer>();
        for (int token : queryString) {
            if (!qTfMap.containsKey(token)) {
                qTfMap.put(token, 1);
                qSet.add(token);
            } else {
                qTfMap.put(token, qTfMap.get(token) + 1);
            }
        }
        double lenq = 0.0;
        for (Map.Entry<Integer, Integer> entry : qTfMap.entrySet()) {
            double idf = tokenBtree.find(entry.getKey()).getIdf();
            lenq += idf * idf * entry.getValue() * entry.getValue();
        }
        lenq = Math.sqrt(lenq);
        for (int i = 0; i < recordBtree.getSize(); i++) {
            //            Entry<Integer, TfTokenSet> entry = it.next();
            List<TfTokenPair> record = recordBtree.find(i).getRecord();
            double lenr = 0.0;
            double simTotal = 0.0;
            for (int j = 0; j < record.size(); j++) {
                TfTokenPair pair = record.get(j);
                double idf = tokenBtree.find(pair.getTid()).getIdf();
                lenr += pair.getTf() * idf * pair.getTf() * idf;
                if (qSet.contains(pair.getTid())) {
                    simTotal += idf * idf * pair.getTf() * qTfMap.get(pair.getTid());
                }
            }
            lenr = Math.sqrt(lenr);

            //            if (i == 618186) {
            //                System.out.println("candidate " + i + " " + simTotal / (lenq * lenr));
            //                record.forEach(e -> System.out.print(idfMap.get(e.getTid()) + " "));
            //                System.out.println();
            //            }
            //
            //            if (i == 2579) {
            //                System.out.println(threshold + " | " + lenr + " | " + lenq);
            //            }
            //            System.out.println(simTotal + " vs " + Math.sqrt(threshold * lenq * lenr));
            if (simTotal >= threshold * lenq * lenr) {
                resultList.add(i);
                //                System.out.println("resultId=" + i + " simlarity=" + simTotal * simTotal / (lenq * lenr));
                //                System.out.println("resultId " + i + " " + String.format("%.5f", simTotal) + " : "
                //                        + String.format("%.2f", Math.sqrt(threshold * lenq * lenr)));
                //                                System.out.println("result " + i + " " + record);
            }

        }
        //        System.out.println("query " + query);
        //        System.out.println("query tf " + qTfMap);
        //        query.forEach(e -> System.out.print(idfMap.get(e) + " "));
        //        System.out.println();

        //        resultList.stream().sorted().forEach(e -> System.out.println("result id " + e));
        return resultList;
    }
}

