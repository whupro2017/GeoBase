package whu.textbase.idf.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import whu.textbase.btree.api.iSelect;
import whu.textbase.idf.storage.index.prefix.IdfPrefMergeIndex;

public class IdfExPrefTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        if (args.length != 13) {
            System.out.println(
                    "indexPath queryPath threshold qExtendedNum rExtendedNum useLengthIndex allmemory tokenCacheSize reverseCacheSize recordCacheSize recordDataCacheSize lengthCacheSize orderedCand");
            System.exit(0);
        }
        int pos = 0;
        String indexPath = args[pos++].trim(); //"g:\\simfind\\imdb\\pref\\index"; index position
        String queryPath = args[pos++].trim(); //"g:\\simfind\\imdb\\queryList";
        double threshold = Double.valueOf(args[pos++]);
        final int qExtendedNum = Integer.valueOf(args[pos++]);
        final int rExtendedNum = Integer.valueOf(args[pos++]);
        final boolean useLengthIndex = Integer.valueOf(args[pos++]) == 1 ? true : false;
        final boolean allMemory = Integer.valueOf(args[pos++]) == 1 ? true : false;
        final int tokenCacheSize = Integer.parseInt(args[pos++]);
        final int reverseCacheSize = Integer.parseInt(args[pos++]);
        final int recordCacheSize = Integer.parseInt(args[pos++]);
        final int recordDataCacheSize = Integer.parseInt(args[pos++]);
        final int lengthCacheSize = Integer.parseInt(args[pos++]);
        final boolean orderedCand = Boolean.parseBoolean(args[pos++]);
        long begin = System.currentTimeMillis();
        IdfPrefMergeIndex index = IdfPrefMergeIndex.open(indexPath, allMemory, tokenCacheSize, reverseCacheSize,
                recordCacheSize, recordDataCacheSize, lengthCacheSize);
        long end = System.currentTimeMillis();
        System.out.println("index cost time " + (end - begin));
        System.out.println("blockcache : token:" + index.getTokenBtree().getCacheSize() + "  reverse:"
                + index.getReverseBtree().getCacheSize() + "  record:"
                + ((IdfPrefMergeIndex) index).getRecordBtree().getCacheSize() + "  length:"
                + ((IdfPrefMergeIndex) index).getLengthBtree().getCacheSize());
        System.out.println("nodecache : token:" + index.getTokenBtree().getNodeCacheSize() + "  reverse:"
                + index.getReverseBtree().getNodeCacheSize() + "  record:"
                + ((IdfPrefMergeIndex) index).getRecordBtree().getNodeCacheSize() + "  length:"
                + ((IdfPrefMergeIndex) index).getLengthBtree().getNodeCacheSize());
        iSelect select = new IdfPrefEx(index, qExtendedNum, rExtendedNum, useLengthIndex, orderedCand);
        List<List<Integer>> queryList = new ArrayList<List<Integer>>();
        BufferedReader queryReader;
        try {
            queryReader = new BufferedReader(new FileReader(queryPath));
            String line = null;
            while ((line = queryReader.readLine()) != null) {
                String[] temp = line.split(" ");
                List<Integer> qtemp = new ArrayList<Integer>();
                for (int i = 0; i < temp.length; i++) {
                    qtemp.add(Integer.valueOf(temp[i].trim()));
                }
                queryList.add(qtemp);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int findTimes = queryList.size();
        List<Integer> resultList = new ArrayList<Integer>();
        System.out.println("Pruning ext threshold=" + threshold + " qExtendedNum=" + qExtendedNum + " rExtendedNum="
                + rExtendedNum + " querySize=" + findTimes);
        int resultCount = 0;
        ((IdfPrefEx) select).setCandiSize(0);
        begin = System.currentTimeMillis();
        for (int i = 0; i < findTimes; i++) {
            // System.out.println(reader.getQueryList().get(i));
            resultList = select.find(queryList.get(i), threshold);
            resultCount += resultList.size();
            //            System.out.println(resultList.size());
        }
        end = System.currentTimeMillis();
        System.out.println("length = " + ((IdfPrefEx) select).getScanLength() * 1.0 / findTimes + " : "
                + ((IdfPrefEx) select).getTotalLength() * 1.0 / findTimes + "   details = "
                + ((IdfPrefEx) select).getLowerLength() * 1.0 / findTimes + " : "
                + ((IdfPrefEx) select).getUpperLength() * 1.0 / findTimes);
        System.out.println("io: token=" + index.getTokenBtree().getReadNo() * 1.0 / findTimes + "  reverse="
                + index.getReverseBtree().getReadNo() * 1.0 / findTimes + "  record="
                + ((IdfPrefMergeIndex) index).getRecordBtree().getReadNo() * 1.0 / findTimes + "  length="
                + ((IdfPrefMergeIndex) index).getLengthBtree().getReadNo() * 1.0 / findTimes);
        System.out.println("candidate = " + ((IdfPrefEx) select).getCandiSize() * 1.0 / findTimes + "   result = "
                + resultCount * 1.0 / findTimes + "   prefix = "
                + ((IdfPrefEx) select).getPrefixTokens() * 1.0 / findTimes + " : "
                + ((IdfPrefEx) select).getTotalTokens() * 1.0 / findTimes);

        //        System.out.println(
        //                "prefix sim = " + ((IdfPrefEx) select).getCountSim() * 1.0 / ((IdfPrefEx) select).getCountCandidate());

        System.out.println("time = " + (end - begin) * 1.0 / findTimes);
        System.out.println();
    }

}
