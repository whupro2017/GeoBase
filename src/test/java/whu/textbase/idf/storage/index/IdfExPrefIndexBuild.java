package whu.textbase.idf.storage.index;

import whu.textbase.btree.analyzer.WordTokenizer;
import whu.textbase.btree.api.iTokenizer;
import whu.textbase.idf.api.iDataReader;
import whu.textbase.idf.api.iIndex;
import whu.textbase.idf.storage.index.prefix.IdfPrefConfig;
import whu.textbase.idf.storage.index.prefix.IdfPrefIndex;
import whu.textbase.idf.storage.reader.DataReader;

public class IdfExPrefIndexBuild {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        if (args.length != 22) {
            System.out.println(
                    "recordNum dataPath indexPath tokenCacheSize tokenBlockSize tokenNodeNumOfBlock tokenNodeCacheSize reverseCacheSize reverseBlockSize reverseNodeNumOfBlock reverseNodeCacheSize recordCacheSize recordDataCacheSize recordBlockSize recordNodeNumOfBlock recordNodeCacheSize lengthCacheSize lengthBlockSize lengthNodeNumOfBlock lengthNodeCacheSize rExtendedNum LengthIndexGap");
            System.exit(0);
        }
        System.out.println("IdfPrefIndexBuild start");

        int pos = 0;
        final int recordNum = Integer.valueOf(args[pos++]);
        final String dataPath = args[pos++];
        final String indexPath = args[pos++]; //"g:\\bplustree\\data.txt";// index position
        final int tokenCacheSize = Integer.valueOf(args[pos++]); // 100;
        final int tokenBlockSize = Integer.valueOf(args[pos++]);
        final int tokenNodeNumOfBlock = Integer.valueOf(args[pos++]);
        final int tokenNodeCacheSize = Integer.valueOf(args[pos++]);
        final int reverseCacheSize = Integer.valueOf(args[pos++]);
        final int reverseBlockSize = Integer.valueOf(args[pos++]);
        final int reverseNodeNumOfBlock = Integer.valueOf(args[pos++]);
        final int reverseNodeCacheSize = Integer.valueOf(args[pos++]);
        final int recordCacheSize = Integer.valueOf(args[pos++]);
        final int recordDataCacheSize = Integer.valueOf(args[pos++]);
        final int recordBlockSize = Integer.valueOf(args[pos++]);
        final int recordNodeNumOfBlock = Integer.valueOf(args[pos++]);
        final int recordNodeCacheSize = Integer.valueOf(args[pos++]);
        final int lengthCacheSize = Integer.valueOf(args[pos++]);
        final int lengthBlockSize = Integer.valueOf(args[pos++]);
        final int lengthNodeNumOfBlock = Integer.valueOf(args[pos++]);
        final int lengthNodeCacheSize = Integer.valueOf(args[pos++]);
        final int rExtendedNum = Integer.valueOf(args[pos++]);
        final int lengthIndexGap = Integer.parseInt(args[pos++]);
        final float cachefac = 0.6f;
        IdfPrefConfig conf = new IdfPrefConfig(indexPath, tokenCacheSize, tokenBlockSize, tokenNodeNumOfBlock,
                reverseCacheSize, reverseBlockSize, reverseNodeNumOfBlock, recordCacheSize, recordDataCacheSize,
                recordBlockSize, recordNodeNumOfBlock,
                lengthCacheSize, lengthBlockSize, lengthNodeNumOfBlock, cachefac);
        conf.setTokenNodeCacheSize(tokenNodeCacheSize);
        conf.setReverseNodeCacheSize(reverseNodeCacheSize);
        conf.setRecordNodeCacheSize(recordNodeCacheSize);
        conf.setLengthNodeCacheSize(lengthNodeCacheSize);
        System.out.println("Num " + recordNum);
        iTokenizer tokenizer = new WordTokenizer();
        iDataReader reader = new DataReader(dataPath, recordNum, tokenizer);
        long begin = System.currentTimeMillis();
        iIndex index = new IdfPrefIndex(reader.getRecordList(), reader.getIdfMap(), conf, rExtendedNum, lengthIndexGap);
        long end = System.currentTimeMillis();
        System.out.println("index build time " + (end - begin));
        System.out.println("TokenCount " + index.getTokenBtree().getSize());
        System.out.println("Token: KeyNum = " + index.getTokenBtree().getKeyNum() + " Height = "
                + index.getTokenBtree().getHeight() + " InterNum = " + index.getTokenBtree().getInterNum()
                + " LeafNum = " + index.getTokenBtree().getLeafNum() + " InterSize = "
                + index.getTokenBtree().getInternsize() + " LeafSize = " + index.getTokenBtree().getLeafnsize());
        System.out.println("Reverse: KeyNum = " + index.getReverseBtree().getKeyNum() + " Height = "
                + index.getReverseBtree().getHeight() + " InterNum = " + index.getReverseBtree().getInterNum()
                + " LeafNum = " + index.getReverseBtree().getLeafNum() + " InterSize = "
                + index.getReverseBtree().getInternsize() + " LeafSize = " + index.getReverseBtree().getLeafnsize());
        System.out.println("Record: KeyNum = " + index.getRecordBtree().getKeyNum() + " Height = "
                + index.getRecordBtree().getHeight() + " InterNum = " + index.getRecordBtree().getInterNum()
                + " LeafNum = " + index.getRecordBtree().getLeafNum() + " InterSize = "
                + index.getRecordBtree().getInternsize() + " LeafSize = " + index.getRecordBtree().getLeafnsize()
                + " DataBlockNum " + index.getRecordBtree().getDataBlockNum());
        System.out.println("Length: KeyNum = " + index.getLengthBtree().getKeyNum() + " Height = "
                + index.getLengthBtree().getHeight() + " InterNum = " + index.getLengthBtree().getInterNum()
                + " LeafNum = " + index.getLengthBtree().getLeafNum() + " InterSize = "
                + index.getLengthBtree().getInternsize() + " LeafSize = " + index.getLengthBtree().getLeafnsize());
        index.close();
    }

}
