package whu.textbase.btree.analyzer;

import whu.textbase.btree.api.iTokenizer;

public class GramTokenizer implements iTokenizer {

    int gramNumber;

    public GramTokenizer(int gramNumber) {
        this.gramNumber = gramNumber;
    }

    @Override public String[] tokenize(String text) {
        // TODO Auto-generated method stub
        int n = text.length();
        String[] words = new String[n - gramNumber + 1];
        for (int i = 0; i < words.length; i++) {
            words[i] = text.substring(i, i + gramNumber);
        }
        return words;
    }

}
