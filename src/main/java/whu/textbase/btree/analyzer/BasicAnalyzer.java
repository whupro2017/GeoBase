package whu.textbase.btree.analyzer;

import whu.textbase.btree.api.Analyzer;
import whu.textbase.btree.api.iTokenizer;

import java.util.*;

public class BasicAnalyzer extends Analyzer {

    public BasicAnalyzer(Map<String, Integer> tokenToIdMap, iTokenizer tokenizer) {
        super(tokenToIdMap, tokenizer);
        // TODO Auto-generated constructor stub
    }

    @Override public List<Integer> analyze(String query) {
        String[] token = tokenizer.tokenize(query);
        List<Integer> q = new ArrayList<Integer>();
        Set<String> tmp = new HashSet<String>();
        for (int i = 0; i < token.length; i++) {
            if (tmp.contains(token[i])) {
                continue;
            }
            q.add(tokenToIdMap.get(token[i]));
            tmp.add(token[i]);
        }
        return q;
    }

}
