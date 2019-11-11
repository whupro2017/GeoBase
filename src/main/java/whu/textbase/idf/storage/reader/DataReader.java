package whu.textbase.idf.storage.reader;

import whu.textbase.btree.api.iTokenizer;
import whu.textbase.btree.common.Tools;
import whu.textbase.idf.api.iDataReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DataReader implements iDataReader {

    private List<List<Integer>> recordList;
    private double avglen;
    private Map<Integer, Double> idfMap;

    public DataReader(String path, int maxLine, iTokenizer tokenizer) {
        read(path, maxLine, tokenizer);

    }

    public void read(String path, int maxLine, iTokenizer tokenizer) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = "";
            int recordCount = 0, tokenCount = 0;
            Map<String, Integer> tokenToIdMap = new HashMap<String, Integer>();
            recordList = new ArrayList<List<Integer>>();
            Map<Integer, String> idToTokenMap = new HashMap<Integer, String>();
            Map<Integer, Integer> freqMap = new HashMap<Integer, Integer>();
            while ((line = reader.readLine()) != null) {
                String str = line.trim();
                String[] token = tokenizer.tokenize(str);
                Set<String> tmp = new HashSet<String>();
                List<Integer> strlist = new ArrayList<Integer>();
                for (int i = 0; i < token.length; i++) {
                    if (tmp.contains(token[i])) {
                        continue;
                    }
                    if (!tokenToIdMap.containsKey(token[i])) {
                        tokenToIdMap.put(token[i], tokenCount);
                        idToTokenMap.put(tokenCount, token[i]);
                        freqMap.put(tokenCount, 1);
                        tokenCount++;
                    } else {
                        if (!tmp.contains(token[i])) {
                            freqMap.put(tokenToIdMap.get(token[i]), freqMap.get(tokenToIdMap.get(token[i])) + 1);
                        }
                    }

                    strlist.add(tokenToIdMap.get(token[i]));
                    tmp.add(token[i]);
                }
                recordList.add(strlist);
                recordCount++;
                if (recordCount >= maxLine) {
                    break;
                }

            }
            this.idfMap = Tools.idfCompute(freqMap, recordList.size());
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Map<Integer, Integer> getFreqMap() {
        return null;
    }

    public Map<String, Integer> getTokenToIdMap() {
        return null;
    }

    public Map<Integer, String> getIdToTokenMap() {
        return null;
    }

    public List<List<Integer>> getRecordList() {
        return this.recordList;
    }

    public Map<Integer, Double> getIdfMap() {
        return idfMap;
    }

    //    public void printFreqMap() {
    //        List<Map.Entry<Integer, Integer>> list = new ArrayList<Entry<Integer, Integer>>(freqMap.entrySet());
    //        list.sort((a, b) -> (a.getValue().compareTo(b.getValue())));
    //        for (Map.Entry<Integer, Integer> e : list) {
    //            System.out.println(e.getValue());
    //        }
    //    }

    public double getAvglen() {
        return avglen;
    }

    //    public void printIdfMap() {
    //        List<Map.Entry<Integer, Double>> list = new ArrayList<Entry<Integer, Double>>(idfMap.entrySet());
    //        list.sort((a, b) -> (a.getValue().compareTo(b.getValue())));
    //        for (Map.Entry<Integer, Double> e : list) {
    //            System.out.println(e.getKey() + "-" + idToTokenMap.get(e.getKey()) + "-" + e.getValue());
    //        }
    //    }

    @Override public Map<Integer, String> getRecordMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public void printFreqMap() {
        // TODO Auto-generated method stub

    }

    @Override public void printIdfMap() {
        // TODO Auto-generated method stub

    }

}
