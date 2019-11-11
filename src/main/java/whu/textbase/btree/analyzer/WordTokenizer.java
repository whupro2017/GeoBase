package whu.textbase.btree.analyzer;

import whu.textbase.btree.api.iTokenizer;

public class WordTokenizer implements iTokenizer {

    @Override public String[] tokenize(String text) {
        // TODO Auto-generated method stub
        return text.split(" ");
    }

}
