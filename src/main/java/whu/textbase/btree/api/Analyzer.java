package whu.textbase.btree.api;

import java.util.List;
import java.util.Map;

public abstract class Analyzer {

    protected Map<String, Integer> tokenToIdMap;
    protected iTokenizer tokenizer;

    public Analyzer(Map<String, Integer> tokenToIdMap, iTokenizer tokenizer) {
        this.tokenToIdMap = tokenToIdMap;
        this.tokenizer = tokenizer;
    }

    public abstract List<Integer> analyze(String text);

}
