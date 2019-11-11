package whu.textbase.idf.api;

import whu.textbase.btree.api.iTokenizer;

import java.util.List;
import java.util.Map;

public interface iDataReader {

    public abstract void read(String path, int maxLine, iTokenizer tokenizer);

    public abstract Map<Integer, Integer> getFreqMap();

    public abstract Map<String, Integer> getTokenToIdMap();

    public abstract Map<Integer, String> getRecordMap();

    public abstract Map<Integer, String> getIdToTokenMap();

    public abstract List<List<Integer>> getRecordList();

    public abstract Map<Integer, Double> getIdfMap();

    public abstract void printFreqMap();

    public abstract double getAvglen();

    public abstract void printIdfMap();
}
