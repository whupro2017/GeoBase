package whu.textbase.btree.analyzer;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;
import whu.textbase.btree.api.iTokenizer;

import java.util.List;

public class JiebaTokenizer implements iTokenizer {
    @Override public String[] tokenize(String text) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<SegToken> segs = segmenter.process(text, SegMode.INDEX);
        String[] tokens = new String[segs.size()];
        int idx = 0;
        for (SegToken tk : segs) {
            tokens[idx] = tk.word;
            idx++;
        }
        return tokens;
    }
}
