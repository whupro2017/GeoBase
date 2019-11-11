package whu.textbase.idf.storage.reader;

import whu.textbase.btree.api.iTokenizer;
import whu.textbase.btree.common.Tools;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class MergeDataReader {

    private Map<Integer, String> idToTokenMap;
    private double avglen;
    private Map<Integer, Double> idfMap;
    private Map<String, Integer> tokenToIdMap;
    private Map<Integer, Integer> freqMap;

    public MergeDataReader(String path, int maxLine, iTokenizer tokenizer) {
        int recordCount = read(path, maxLine, tokenizer);
        this.idfMap = Tools.idfCompute(freqMap, recordCount);
    }

    public int read(String path, int maxLine, iTokenizer tokenizer) {
        int recordCount = 0;
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), "utf-8"));
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path + ".tmp")), "utf-8"));
            String line = "";
            int tokenCount = 0;
            tokenToIdMap = new HashMap<String, Integer>();
            idToTokenMap = new HashMap<Integer, String>();
            freqMap = new HashMap<Integer, Integer>();
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
                for (int t : strlist) {
                    writer.write(t + " ");
                }
                writer.write("\n");
                recordCount++;
                if (recordCount >= maxLine) {
                    break;
                }

            }
            writer.close();
            reader.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return recordCount;
    }

    public Map<Integer, Integer> getFreqMap() {
        return this.freqMap;
    }

    public Map<String, Integer> getTokenToIdMap() {
        return this.tokenToIdMap;
    }

    public Map<Integer, String> getIdToTokenMap() {
        return this.idToTokenMap;
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
