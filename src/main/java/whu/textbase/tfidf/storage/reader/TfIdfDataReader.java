package whu.textbase.tfidf.storage.reader;

import whu.textbase.btree.api.iTokenizer;
import whu.textbase.btree.common.Tools;
import whu.textbase.tfidf.api.iDataReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class TfIdfDataReader implements iDataReader {

    private Map<Integer, String> recordMap;
    private Map<Integer, String> idToTokenMap;
    private List<List<TfTokenPair>> recordList;
    private double avglen;
    private Map<Integer, Double> idfMap;
    private Map<String, Integer> tokenToIdMap;
    private Map<Integer, Integer> freqMap;
    protected Map<Integer, Short> tfmaxMap; // token -> tfmax
    //    protected Map<Short, List<Integer>> tfmaxToTokenListMap; // tfmax -> tokenList
    //    protected Map<Integer, List<Integer>> tokenTfmaxToRecordListMap; // (token, tfmax) -> recordList

    public TfIdfDataReader(String path, int maxLine, iTokenizer tokenizer) {
        read(path, maxLine, tokenizer);
        this.idfMap = Tools.idfCompute(freqMap, recordList.size());
    }

    public void read(String path, int maxLine, iTokenizer tokenizer) {
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), "utf-8"));
            String line = "";
            int recordCount = 0, tokenCount = 0;
            tokenToIdMap = new HashMap<String, Integer>();
            recordList = new ArrayList<List<TfTokenPair>>();
            idToTokenMap = new HashMap<Integer, String>();
            freqMap = new HashMap<Integer, Integer>();
            recordMap = new HashMap<Integer, String>();
            tfmaxMap = new HashMap<Integer, Short>();
            //            tokenTfmaxToRecordListMap = new HashMap<Integer, List<Integer>>();
            //            tfmaxToTokenListMap = new HashMap<Short, List<Integer>>();
            int lenSum = 0, maxlen = 0;
            BufferedWriter bwsc = new BufferedWriter(new FileWriter("record.str"));
            BufferedWriter bwic = new BufferedWriter(new FileWriter("record.int"));
            while ((line = reader.readLine()) != null) {
                recordMap.put(recordCount, line);
                String[] token = tokenizer.tokenize(line);
                Map<String, Short> tfMap = new HashMap<String, Short>();
                List<Integer> strlist = new ArrayList<Integer>();
                if (maxlen < token.length) {
                    maxlen = token.length;
                }
                String strrec = "";
                String intrec = "";
                for (int i = 0; i < token.length; i++) {
                    if (tfMap.containsKey(token[i])) {
                        tfMap.put(token[i], (short) (tfMap.get(token[i]) + 1));
                        continue;
                    }
                    lenSum++;
                    if (!tokenToIdMap.containsKey(token[i])) {
                        tokenToIdMap.put(token[i], tokenCount);
                        idToTokenMap.put(tokenCount, token[i]);
                        freqMap.put(tokenCount, 1);
                        tokenCount++;
                    } else {
                        if (!tfMap.containsKey(token[i])) {
                            freqMap.put(tokenToIdMap.get(token[i]), freqMap.get(tokenToIdMap.get(token[i])) + 1);
                        }
                    }
                    strrec += token[i];
                    strrec += " ";
                    intrec += tokenToIdMap.get(token[i]);
                    intrec += " ";
                    tfMap.put(token[i], (short) 1);
                    strlist.add(tokenToIdMap.get(token[i]));
                }
                bwsc.write(strrec + "\n");
                bwic.write(intrec + "\n");
                List<TfTokenPair> record =
                        strlist.stream().map(elem -> new TfTokenPair(elem, tfMap.get(idToTokenMap.get(elem))))
                                .collect(Collectors.toList());
                //                final int recordId = recordCount;
                tfMap.forEach((k, v) -> {
                    int tokenId = tokenToIdMap.get(k);
                    //                    List<Integer> recordListTemp = null;
                    if (tfmaxMap.containsKey(tokenId)) {
                        //                        recordListTemp = tokenTfmaxToRecordListMap.get(tokenId);
                        double tokentfmax = tfmaxMap.get(tokenId);

                        if (tokentfmax < v) {
                            tfmaxMap.put(tokenId, v);
                            //                            recordListTemp.clear();
                            //                            recordListTemp.add(recordId);
                        } else if (tokentfmax == v) {
                            //                            recordListTemp.add(recordId);
                        } else {

                        }
                    } else {
                        tfmaxMap.put(tokenId, v);
                        //                        recordListTemp = new ArrayList<Integer>();
                        //                        recordListTemp.add(recordId);
                        //                        tokenTfmaxToRecordListMap.put(tokenId, recordListTemp);
                    }
                });
                recordList.add(record);
                recordCount++;
                if (recordCount >= maxLine) {
                    break;
                }

            }
            bwsc.close();
            bwic.close();
            //            System.out.println("maxlen " + maxlen);
            avglen = lenSum * 1.0 / recordCount;
            //            tfmaxMap.forEach((k, v) -> {
            //                List<Integer> tfmaxTokenList = null;
            //                if (tfmaxToTokenListMap.containsKey(v)) {
            //                    tfmaxTokenList = tfmaxToTokenListMap.get(v);
            //                } else {
            //                    tfmaxTokenList = new ArrayList<Integer>();
            //                    tfmaxToTokenListMap.put(v, tfmaxTokenList);
            //                }
            //                tfmaxTokenList.add(k);
            //            });
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void printTfMaxMap() {
        List<Map.Entry<Integer, Short>> list = new ArrayList<Entry<Integer, Short>>(tfmaxMap.entrySet());
        list.sort((a, b) -> (a.getValue().compareTo(b.getValue())));
        list.forEach(entry -> {
            System.out.println(entry.getKey() + "-" + idToTokenMap.get(entry.getKey()) + "-" + entry.getValue());
        });

    }

    public void printIdToTokenMap() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("idtotokenMap.txt"));
            idToTokenMap.forEach((k, v) -> {
                try {
                    writer.write(k + " " + v + "\n");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    //    public void printTfmaxStatistics() {
    //
    //        List<Map.Entry<Short, List<Integer>>> list = new ArrayList<Entry<Short, List<Integer>>>(
    //                tfmaxToTokenListMap.entrySet());
    //        list.sort((a, b) -> (b.getKey().compareTo(a.getKey())));
    //        List<Integer> recordListTotal = new ArrayList<Integer>();
    //        list.forEach(entry -> {
    //            System.out.print(entry.getKey() + "   " + entry.getValue().size() + "   ");
    //            List<Integer> recordList = new ArrayList<Integer>();
    //            entry.getValue().forEach((elem) -> {
    //                recordList.addAll(tokenTfmaxToRecordListMap.get(elem));
    //            });
    //            recordListTotal.addAll(recordList);
    //            System.out.println(recordList.stream().distinct().count() + "    " + entry.getValue().get(0) + "-"
    //                    + idToTokenMap.get(entry.getValue().get(0)));
    //        });
    //        System.out.println("recordListTotal " + recordListTotal.stream().distinct().count());
    //        recordListTotal.stream().distinct().sorted().forEach(System.out::println);
    //        list.stream().filter(e -> (e.getKey() > 10)).forEach(entry -> {
    //
    //            System.out.println("tfmax: " + entry.getKey());
    //            entry.getValue().forEach((elem) -> {
    //                System.out.print("[" + idToTokenMap.get(elem) + "]  ");
    //                List<Integer> recordList = tokenTfmaxToRecordListMap.get(elem);
    //                recordList.forEach((e) -> {
    //                    System.out.println(recordMap.get(e));
    //                });
    //            });
    //            System.out.println();
    //        });
    //    }

    public Map<Integer, Short> getTfMaxMap() {
        return tfmaxMap;
    }

    //    public Map<Integer, List<Integer>> getTfmaxToRecordMap() {
    //        return tokenTfmaxToRecordListMap;
    //    }

    public Map<Integer, Integer> getFreqMap() {
        return this.freqMap;
    }

    public Map<String, Integer> getTokenToIdMap() {
        return this.tokenToIdMap;
    }

    public Map<Integer, String> getRecordMap() {
        return this.recordMap;
    }

    public Map<Integer, String> getIdToTokenMap() {
        return this.idToTokenMap;
    }

    public List<List<TfTokenPair>> getRecordList() {
        return recordList;
    }

    public void printRecordList() {
        for (List<TfTokenPair> list : recordList) {
            for (TfTokenPair pair : list) {
                System.out.print(pair.toString() + " ");
            }
            System.out.println();
        }
    }

    public Map<Integer, Double> getIdfMap() {
        return idfMap;
    }

    public void printFreqMap() {
        List<Map.Entry<Integer, Integer>> list = new ArrayList<Entry<Integer, Integer>>(freqMap.entrySet());
        list.sort((a, b) -> (a.getValue().compareTo(b.getValue())));
        for (Map.Entry<Integer, Integer> e : list) {
            System.out.println(e.getValue());
        }
    }

    public double getAvglen() {
        return avglen;
    }

    public void printIdfMap() {
        List<Map.Entry<Integer, Double>> list = new ArrayList<Entry<Integer, Double>>(idfMap.entrySet());
        list.sort((a, b) -> (a.getValue().compareTo(b.getValue())));
        for (Map.Entry<Integer, Double> e : list) {
            System.out.println(e.getKey() + "-" + idToTokenMap.get(e.getKey()) + "-" + e.getValue());
        }
    }

}
