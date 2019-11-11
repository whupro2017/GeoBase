package whu.textbase.btree.analyzer;

import whu.textbase.btree.api.iTokenizer;

public class IMDBGramTokenizer implements iTokenizer {

    int gramNumber;

    public IMDBGramTokenizer(int gramNumber) {
        this.gramNumber = gramNumber;
    }

    @Override public String[] tokenize(String text) {
        // TODO Auto-generated method stub
        int n = text.length();
        text = text.replaceAll(" ", "_");
        String[] words;
        if (n < gramNumber) {
            char t[] = new char[gramNumber];
            for (int i = 0; i < n; i++) {
                t[i] = text.charAt(i);
            }
            for (int i = n; i < gramNumber; i++) {
                t[i] = '_';
            }
            words = new String[1];
            words[0] = String.valueOf(t);
        } else {
            words = new String[n - gramNumber + 1];
            for (int i = 0; i < words.length; i++) {
                words[i] = text.substring(i, i + gramNumber);
            }
        }
        return words;
    }

}
