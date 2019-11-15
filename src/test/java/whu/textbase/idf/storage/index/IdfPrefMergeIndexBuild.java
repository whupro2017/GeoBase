package whu.textbase.idf.storage.index;

import whu.textbase.btree.analyzer.WordTokenizer;
import whu.textbase.btree.api.iTokenizer;
import whu.textbase.btree.common.Tools;
import whu.textbase.idf.storage.index.prefix.IdfPrefConfig;
import whu.textbase.idf.storage.index.prefix.IdfPrefMergeIndex;
import whu.textbase.idf.storage.reader.MergeDataReader;

public class IdfPrefMergeIndexBuild {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        if (args.length != 16) {
            System.out.println(
                    "recordNum dataPath indexPath tokenCacheSize  tokenNodeCacheSize reverseCacheSize  reverseNodeCacheSize recordCacheSize recordDataCacheSize  recordNodeCacheSize lengthCacheSize  lengthNodeCacheSize rExtendedNum LengthIndexGap Part PartNodeCacheSize");
            System.exit(0);
        }
        System.out.println("IdfPrefIndexBuild start");
        Tools.func = Tools.FuncType.l2;
        int pos = 0;
        final int recordNum = Integer.valueOf(args[pos++]);
        final String dataPath = args[pos++];
        final String indexPath = args[pos++]; //"g:\\bplustree\\data.txt";// index position
        final int tokenCacheSize = Integer.valueOf(args[pos++]); // Total size for token index, to be fully maintained in memory.
        final int tokenBlockSize = 4096;
        final int tokenNodeNumOfBlock = 2; // Nodes per Block, set as 1 by default.
        final int tokenNodeCacheSize = Integer.valueOf(args[pos++]); // Cache number for all of the nodes in token index, for avoiding deserialization.
        final int reverseCacheSize = Integer.valueOf(args[pos++]); // Cache number for inverted index.
        final int reverseBlockSize = 4096;
        final int reverseNodeNumOfBlock = 1; // Nodes per Block, set as 1 by default.
        final int reverseNodeCacheSize = Integer.valueOf(args[pos++]); // Cache number for all of the nodes in inverted index, for avoiding deserialization.
        final int recordCacheSize = Integer.valueOf(args[pos++]); // Cache number for record index.
        final int recordDataCacheSize = Integer.valueOf(args[pos++]); // Cache number for record corpus.
        final int recordBlockSize = 4096;
        final int recordNodeNumOfBlock = 1; // Nodes per Block, set as 1 by default.
        final int recordNodeCacheSize = Integer.valueOf(args[pos++]); // Cache number for all of the nodes in record index, for avoiding deserialization.
        final int lengthCacheSize = Integer.valueOf(args[pos++]); // Cache number for length index.
        final int lengthBlockSize = 4096;
        final int lengthNodeNumOfBlock = 1; // Nodes per Block, set as 1 by default.
        final int lengthNodeCacheSize = Integer.valueOf(args[pos++]); // Cache number of all of the nodes in length index, for avoiding deserialization.
        final int rExtendedNum = Integer.valueOf(args[pos++]); // Lookahead steps.
        final int lengthIndexGap = Integer.parseInt(args[pos++]); // Interval corresponding to a certain number of entries.
        final int part = Integer.valueOf(args[pos++]); // Number of local slices.
        final int partNodeCacheSize = Integer.valueOf(args[pos++]); // Specified parameter for local construction, other parameters have been fixed in the underlying structures.
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
        MergeDataReader reader = new MergeDataReader(dataPath, recordNum, tokenizer);
        long begin = System.currentTimeMillis();
        IdfPrefMergeIndex index = new IdfPrefMergeIndex(dataPath, reader.getIdfMap(), conf, rExtendedNum,
                lengthIndexGap, part, partNodeCacheSize);
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
