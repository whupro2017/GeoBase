package whu.textbase.tfidf.process;

import whu.textbase.btree.api.iSelect;
import whu.textbase.tfidf.api.iDataReader;
import whu.textbase.tfidf.storage.reader.TfTokenPair;

import java.util.*;

public class ForceSelect implements iSelect {

    private List<List<TfTokenPair>> recordList;
    private Map<Integer, Double> idfMap;

    public ForceSelect(iDataReader reader) {
        this.idfMap = reader.getIdfMap();
        this.recordList = reader.getRecordList();
        // TODO Auto-generated constructor stub
    }

    @Override public List<Integer> find(List<Integer> query, double threshold) {
        // TODO Auto-generated method stub
        List<Integer> resultList = new ArrayList<Integer>();

        Map<Integer, Integer> qTfMap = new HashMap<Integer, Integer>();
        Set<Integer> qSet = new HashSet<Integer>();
        for (int token : query) {
            if (!qTfMap.containsKey(token)) {
                qTfMap.put(token, 1);
                qSet.add(token);
            } else {
                qTfMap.put(token, qTfMap.get(token) + 1);
            }
        }
        double lenq = 0.0;
        for (Map.Entry<Integer, Integer> entry : qTfMap.entrySet()) {
            double idf = idfMap.get(entry.getKey());
            lenq += idf * idf * entry.getValue() * entry.getValue();
        }
        lenq = Math.sqrt(lenq);
        for (int i = 0; i < recordList.size(); i++) {
            List<TfTokenPair> record = recordList.get(i);
            double lenr = 0.0;
            double simTotal = 0.0;
            for (int j = 0; j < record.size(); j++) {
                TfTokenPair pair = (TfTokenPair) record.get(j);
                double idf = idfMap.get(pair.getTid());
                lenr += pair.getTf() * idf * pair.getTf() * idf;
                if (qSet.contains(pair.getTid())) {
                    simTotal += idf * idf * pair.getTf() * qTfMap.get(pair.getTid());
                }
            }
            lenr = Math.sqrt(lenr);

            //            if (i == 408952) {
            //                System.out.println("candidate " + i + " " + simTotal / (lenq * lenr));
            //                record.forEach(pair -> {
            //                    System.out
            //                            .println("[" + pair.getTid() + "-" + pair.getTf() + "-" + idfMap.get(pair.getTid()) + "]");
            //                });
            //                System.out.println();
            //            }
            //
            //            if (i == 408952) {
            //                System.out.println(threshold + " | " + lenr + " | " + lenq);
            //
            //                System.out.println(simTotal + " vs " + threshold * lenq * lenr);
            //            }
            if (simTotal >= threshold * lenq * lenr) {
                resultList.add(i);

                //                if (i == 408952) {
                //                    //                System.out.println("resultId=" + i + " simlarity=" + simTotal * simTotal / (lenq * lenr));
                //                    System.out.println("resultId " + i + " " + String.format("%.5f", simTotal) + " : "
                //                        + String.format("%.2f", Math.sqrt(threshold * lenq * lenr)));
                //                }
                //                System.out.println("result " + i + " " + record);
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

