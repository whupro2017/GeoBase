package whu.textbase.tfidf.process;

import whu.textbase.btree.api.iSelect;
import whu.textbase.btree.common.KeyPair;
import whu.textbase.btree.core.Btree;
import whu.textbase.btree.core.BtreeCluster;
import whu.textbase.btree.serialize.iSerializable;
import whu.textbase.btree.utils.btree.BtreeClusterSp;
import whu.textbase.btree.utils.btree.BtreeClusterSp2;
import whu.textbase.tfidf.api.iIndex;
import whu.textbase.tfidf.storage.index.TfIdfHeadTuple;
import whu.textbase.tfidf.storage.index.TfPrefNodeTuple;
import whu.textbase.tfidf.storage.index.TfTokenSet;
import whu.textbase.tfidf.storage.reader.TfTokenPair;

import java.util.*;
import java.util.Map.Entry;

public class TfidfPrefForward implements iSelect {

    private Btree<Integer, TfTokenSet> recordBtree;
    private BtreeCluster<Integer, iSerializable> reverseBtree;
    private BtreeCluster<Integer, TfIdfHeadTuple> tokenBtree;
    private BtreeClusterSp2<KeyPair, Integer> lengthBtree;
    private BtreeClusterSp<KeyPair, Short> tfmaxBtree;
    private long candiSize, falsePrefix, actualPrefix, totalTokens, upperLength, lowerLength, scanLength;
    private int qExtendedNum, rExtendedNum;
    private boolean useLengthIndex;

    public TfidfPrefForward(iIndex index, int qExtendedNum, int rExtendedNum, boolean useLengthIndex) {
        this.recordBtree = index.getRecordBtree();
        this.reverseBtree = index.getReverseBtree();
        this.tokenBtree = index.getTokenBtree();
        this.lengthBtree = index.getLengthBtree();
        this.tfmaxBtree = index.getTfmaxBtree();
        candiSize = falsePrefix = actualPrefix = totalTokens = scanLength = upperLength = lowerLength = 0;
        this.qExtendedNum = qExtendedNum;
        this.rExtendedNum = rExtendedNum;
        this.useLengthIndex = useLengthIndex;
        // TODO Auto-generated constructor stub
    }

    @Override public List<Integer> find(List<Integer> queryString, double threshold) {
        // TODO Auto-generated method stub

        Map<Integer, Short> qTfMap = new HashMap<Integer, Short>();
        List<Integer> query = new ArrayList<Integer>();
        Double lenq = 0.0;
        for (int token : queryString) {
            if (!qTfMap.containsKey(token)) {
                qTfMap.put(token, (short) 1);
                query.add(token);
            } else {
                qTfMap.put(token, (short) (qTfMap.get(token) + 1));
            }
        }
        query.sort((a, b) -> {
            TfIdfHeadTuple temp_a = tokenBtree.find(a);
            TfIdfHeadTuple temp_b = tokenBtree.find(b);
            return compareToken(a, b, temp_a, temp_b);
        });
        short tfmaxOfQuery = 1;
        short[] qsufTfmax = new short[query.size()];
        TfIdfHeadTuple[] headInfo = new TfIdfHeadTuple[query.size()];
        for (int i = query.size() - 1; i >= 0; i--) {
            int token = query.get(i);
            short q_tf = qTfMap.get(token);
            qsufTfmax[i] = tfmaxOfQuery;
            if (tfmaxOfQuery < q_tf) {
                tfmaxOfQuery = q_tf;
            }
            TfIdfHeadTuple temp = tokenBtree.find(token);
            lenq += temp.getIdf() * q_tf * q_tf * temp.getIdf();
            headInfo[i] = temp;
        }
        lenq = Math.sqrt(lenq);
        double suffix = threshold * threshold * lenq * lenq / tfmaxOfQuery;
        Double[] qsuf = new Double[query.size() + qExtendedNum + 1];

        double sufLengthAccu = 0.0, sufLapAccu = 0.0;
        int tokenNum = query.size();
        boolean lenflag = false;
        for (int i = query.size() - 1; i >= 0; i--) {
            TfIdfHeadTuple temp = headInfo[i];
            int token = query.get(i);
            sufLengthAccu += temp.getIdf() * qTfMap.get(token) * temp.getIdf() * qTfMap.get(token);
            qsuf[i] = sufLengthAccu;

            double tfidfEstimate = temp.getIdf() * temp.getIdf() * qTfMap.get(token) * temp.getTfmax();

            if (!lenflag) {

                if (sufLapAccu + tfidfEstimate >= suffix) {
                    tokenNum = i + 1;
                    lenflag = true;
                }
                sufLapAccu += tfidfEstimate;
            }
            //            System.out.println("accu=" + sufLapAccu + " suffix=" + suffix + " tokenNum=" + tokenNum + " tfmax&idf "
            //                    + qTfMap.get(token) + ":" + temp.getTfmax() + "-" + temp.getIdf());
        }
        for (int i = 0; i <= qExtendedNum; i++) {
            qsuf[query.size() + qExtendedNum] = 0.0;
        }
        double qleft = threshold * lenq / tfmaxOfQuery;
        Map<Integer, Cpair> candidate = new HashMap<Integer, Cpair>();
        tokenNum += qExtendedNum;
        if (tokenNum > query.size()) {
            tokenNum = query.size();
        }
        //        System.out.println("tokenNum =" + tokenNum + "     total = " + qDistinct.size());

        Iterator<Entry<Integer, Cpair>> it = null;
        short[] TfMax = new short[query.size()];
        for (int i = 0; i < TfMax.length; i++) {
            TfMax[i] = headInfo[i].getTfmax();
        }
        double maxlenC = 0.0, maxlenS = 0.0, qUpper, minlenC = Double.MAX_VALUE;
        for (int i = 0; i < query.size(); i++) {
            maxlenS += headInfo[i].getIdf() * headInfo[i].getIdf() * qTfMap.get(query.get(i)) * headInfo[i].getTfmax();
        }
        maxlenS = maxlenS / lenq / threshold;
        qUpper = maxlenS;
        double qright = maxlenS;
        //        System.out.println("qleft = " + qleft + "     qright = " + qright + " qUpper = " + qUpper);
        Set<Integer> resultSet = new HashSet<Integer>();
        int pos = 0;
        boolean endFlag = false;
        for (; pos < query.size(); pos++) {

            if (pos >= tokenNum && candidate.size() == 0) {
                break;
            }

            int token = query.get(pos);
            double idf = headInfo[pos].getIdf();
            short qtf = qTfMap.get(token);
            TfPrefNodeTuple curTuple = null;
            if (useLengthIndex) {
                Integer addr = lengthBtree.find(new KeyPair(token, qleft));
                if (addr == null) {
                    curTuple = (TfPrefNodeTuple) reverseBtree.find(headInfo[pos].getHead());

                } else {
                    curTuple = (TfPrefNodeTuple) reverseBtree.find(addr);

                }
            } else {
                curTuple = (TfPrefNodeTuple) reverseBtree.find(headInfo[pos].getHead());
            }

            while (curTuple != null) {

                if (curTuple.pair.getLen() >= qleft) {
                    break;
                }
                lowerLength++;
                scanLength++;
                curTuple = (TfPrefNodeTuple) reverseBtree.find(curTuple.next);
            }

            while (curTuple != null) {
                scanLength++;
                //                System.out.println(token + " - " + curTuple.pair.getTf() + " " + curTuple.pair.getLen());
                double totalUpperBound = qright < qUpper ? qright : qUpper;

                if (curTuple.pair.getLen() > totalUpperBound) {

                    //                    while (curTuple != null) {
                    //                        //                        System.out.println(token + " upper- " + curTuple.pair.getTf());
                    //                        curTuple = (TfPrefNodeTuple) reverseBtree.find(curTuple.next);
                    //                        upperLength++;
                    //                    }
                    break;
                }

                int id = curTuple.pair.getId();

                if (!resultSet.contains(id)) {
                    Cpair cpair;
                    if (candidate.containsKey(id)) {
                        cpair = candidate.get(id);
                    } else {
                        cpair = new Cpair(curTuple.pair.getLen(), query.size());
                    }
                    double rLap = curTuple.pair.getLen() * curTuple.pair.getLen() - curTuple.pair.getAccu();
                    double qLap = qsuf[pos];

                    double minsuf = Math.sqrt(rLap * qLap);
                    //                    if (id == 408952) {
                    //                        for (int r = 0; r < qsuf.length; r++) {
                    //                            System.out.println("qsuf " + r + " = " + qsuf[r]);
                    //                        }
                    //                        System.out.println("before lap " + rLap + " : " + qLap);
                    //                        System.out.println(cpair.accu + minsuf < cpair.len * lenq * threshold);
                    //                        System.out.println("cut : " + cpair.accu + " " + minsuf + "  " + cpair.len * lenq * threshold);
                    //                        System.out.println("len : " + lenq + " " + cpair.len);
                    //                        recordBtree.find(408952).getRecord().forEach((pair) -> {
                    //                            System.out.println("[" + pair.getTid() + "-" + pair.getTf() + "-"
                    //                                    + tokenBtree.find(pair.getTid()).getIdf() + "]");
                    //                        });
                    //                    }
                    if (cpair.accu + minsuf >= cpair.len * lenq * threshold) {
                        cpair.rLap = rLap - idf * idf * curTuple.pair.getTf() * curTuple.pair.getTf();
                        if (cpair.rLap < 0) {
                            cpair.rLap = 0;
                        }
                        cpair.token = token;
                        cpair.tokenEx = new TfTokenPair[rExtendedNum];
                        System.arraycopy(curTuple.pair.getTokenEx(), 0, cpair.tokenEx, 0, rExtendedNum);
                        cpair.accu += idf * idf * qtf * curTuple.pair.getTf();
                        cpair.tf[pos] = curTuple.pair.getTf();
                        //                        if (!candidate.containsKey(id) && cpair.len > maxlenS) {
                        //                            System.out.println("hello");
                        //                        }
                        if (cpair.accu >= cpair.len * lenq * threshold) {
                            resultSet.add(id);
                            if (candidate.containsKey(id)) {
                                candidate.remove(id);
                            }
                        } else if (!candidate.containsKey(id) && cpair.len <= maxlenS && pos < tokenNum) {
                            candidate.put(id, cpair);
                        }
                    } else {
                        if (candidate.containsKey(id)) {
                            candidate.remove(id);
                        }
                    }

                }
                if (curTuple.next == -1) {
                    break;
                }
                if (pos >= tokenNum && candidate.size() == 0) {
                    endFlag = true;
                    break;
                }
                curTuple = (TfPrefNodeTuple) reverseBtree.find(curTuple.next);
            }
            if (endFlag) {
                break;
            }
            it = candidate.entrySet().iterator();
            short[] tfmax = new short[query.size()];
            Arrays.fill(tfmax, (short) 1);
            while (it.hasNext()) {
                Entry<Integer, Cpair> entry = it.next();
                Cpair cpair = entry.getValue();
                double qLap = qsuf[pos + 1], rLap = cpair.rLap;
                double minsuf = Math.sqrt(rLap * qLap);
                //                if (entry.getKey() == 408952) {
                //                    System.out.println("before lap " + rLap + " : " + qLap);
                //                    System.out.println(cpair.accu + minsuf < cpair.len * lenq * threshold);
                //                    System.out.println("cut : " + cpair.accu + " " + minsuf + "  " + cpair.len * lenq * threshold);
                //                }
                if (cpair.accu + minsuf < cpair.len * lenq * threshold) {
                    it.remove();
                } else {
                    for (int j = 0; j <= pos; j++) {
                        if (cpair.tf[j] > tfmax[j]) {
                            tfmax[j] = cpair.tf[j];
                        }
                    }
                }
            }
            boolean changed = true;
            while (changed) {
                changed = false;
                for (int j = 0; j <= pos; j++) {
                    if (tfmax[j] < TfMax[j]) {
                        TfMax[j] = tfmax[j];
                    }
                }

                qright = 0;
                for (int j = 0; j < query.size(); j++) {
                    qright += headInfo[j].getIdf() * headInfo[j].getIdf() * qTfMap.get(query.get(j)) * TfMax[j];
                }
                qright = qright / lenq / threshold;
                it = candidate.entrySet().iterator();

                maxlenC = 0;
                while (it.hasNext()) {
                    Entry<Integer, Cpair> entry = it.next();
                    Cpair cpair = entry.getValue();
                    if (cpair.len > qright) {
                        it.remove();
                        changed = true;
                    } else {
                        if (cpair.len > maxlenC) {
                            maxlenC = cpair.len;
                        }
                    }
                }

                if (changed) {
                    Arrays.fill(tfmax, (short) 1);
                    it = candidate.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<Integer, Cpair> entry = it.next();
                        Cpair cpair = entry.getValue();
                        for (int j = 0; j <= pos; j++) {
                            if (cpair.tf[j] > tfmax[j]) {
                                tfmax[j] = cpair.tf[j];
                            }
                        }
                    }
                }

            }
            changed = true;
            qUpper = maxlenC > maxlenS ? maxlenC : maxlenS;

            while (changed) {
                changed = false;
                for (int j = 0; j <= pos; j++) {
                    if (tfmax[j] < TfMax[j]) {
                        TfMax[j] = tfmax[j];
                        changed = true;
                    }
                }

                double totalUpperBound = qUpper < qright ? qUpper : qright;
                for (int j = pos + 1; j < query.size(); j++) {
                    Short tfmaxPre = tfmaxBtree.find(new KeyPair(query.get(j), totalUpperBound));
                    if (tfmaxPre != null && tfmaxPre < TfMax[j]) {
                        TfMax[j] = tfmaxPre;
                        changed = true;
                    }

                    //                                        short tfmaxPreTmp = lengthBtree.find(new KeyPair(query.get(j), totalUpperBound)).tfmaxPre;
                    //                    if (tfmaxPre != tfmaxPreTmp){
                    //                            System.out.println(
                    //                            tfmaxPre + " -- " + tfmaxPreTmp);
                    //                    }
                }
                qright = 0;
                for (int j = 0; j <= pos; j++) {
                    qright += headInfo[j].getIdf() * headInfo[j].getIdf() * qTfMap.get(query.get(j)) * TfMax[j];
                }

                maxlenS = 0.0;
                for (int j = pos + 1; j < query.size(); j++) {
                    maxlenS += headInfo[j].getIdf() * headInfo[j].getIdf() * qTfMap.get(query.get(j)) * TfMax[j];
                }
                qright += maxlenS;
                maxlenS = maxlenS / lenq / threshold;
                qright = qright / lenq / threshold;
                it = candidate.entrySet().iterator();
                maxlenC = 0.0;
                minlenC = Double.MAX_VALUE;
                boolean changed2 = false;
                while (it.hasNext()) {
                    Entry<Integer, Cpair> entry = it.next();
                    Cpair cpair = entry.getValue();
                    if (cpair.len > qright) {
                        it.remove();
                        changed2 = true;
                    } else {
                        if (cpair.len > maxlenC) {
                            maxlenC = cpair.len;
                        }
                        if (cpair.len < minlenC) {
                            minlenC = cpair.len;
                        }
                    }
                }
                if (changed2) {
                    Arrays.fill(tfmax, (short) 1);
                    it = candidate.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<Integer, Cpair> entry = it.next();
                        Cpair cpair = entry.getValue();
                        for (int j = 0; j <= pos; j++) {
                            if (cpair.tf[j] > tfmax[j]) {
                                tfmax[j] = cpair.tf[j];
                            }
                        }
                    }
                }

                qUpper = maxlenS > maxlenC ? maxlenS : maxlenC;
            }
            //            System.out.printl=n("maxlenS " + maxlenS + " maxlenC " + maxlenC);
            suffix = threshold * lenq * threshold * lenq / qsufTfmax[pos];
            qleft = threshold * lenq / qsufTfmax[pos];
            qleft = qleft < minlenC ? qleft : minlenC;
            sufLapAccu = 0;

            //            System.out.println("qsufmax=" + qsufTfmax[i] + " suffix=" + suffix);
            int curPos;
            for (curPos = query.size() - 1; curPos >= 0; curPos--) {
                int token_suf = query.get(curPos);
                TfIdfHeadTuple tuple_suf = headInfo[curPos];
                double tfidfEstimate = tuple_suf.getIdf() * tuple_suf.getIdf() * qTfMap.get(token_suf) * TfMax[curPos];
                //                System.out.println(sufLapAccu + " " + tfidfEstimate + " " + suffix);
                //                System.out.println(
                //                        tuple_suf.getIdf() + tuple_suf.getIdf() + " " + qTfMap.get(token_suf) + " " + TfMax[curPos]);
                if (sufLapAccu + tfidfEstimate >= suffix) {
                    if (tokenNum > curPos + 1) {
                        //                        System.out.println(pos + " tokenNum " + tokenNum + " -> " + (curPos + 1));
                    }
                    tokenNum = curPos + 1;
                    break;
                }
                sufLapAccu += tfidfEstimate;
            }
            //            if (curPos < i) {
            //                tokenNum = i + 1;
            //            }
            //                                    System.out.println("changed: " + "qleft = " + qleft + " qright = " + qright + " qUpper = " + qUpper
            //                                            + " tokenNum = " + tokenNum);
            //                                    for (int j = 0; j < query.size(); j++) {
            //                                        System.out.print(TfMax[j] + " ");
            //                                    }
            //                                    System.out.println();

        }
        //        System.out.println("tokenNum = " + tokenNum + "     total = " + query.size());
        falsePrefix += tokenNum;
        actualPrefix += pos;
        //        System.out.println(pos + ":" + query.size() + " " + sslen);
        totalTokens += query.size();
        it = candidate.entrySet().iterator();
        candiSize += candidate.size();
        //        System.out.println(candidate.size());
        while (it.hasNext()) {
            Entry<Integer, Cpair> entry = it.next();
            //                        System.out.println(entry.getKey());
            Cpair cpair = entry.getValue();
            int id = entry.getKey();
            List<TfTokenPair> record = recordBtree.find(id).getRecord();
            int posq = query.size() - 1, posr = record.size() - 1;
            while (posq >= 0) {
                int tokenq = query.get(posq);
                TfTokenPair tokenr = record.get(posr);
                if (tokenq == cpair.token || tokenr.getTid() == cpair.token) {
                    break;
                }

                TfIdfHeadTuple qTuple = headInfo[posq];
                TfIdfHeadTuple rTuple = tokenBtree.find(tokenr.getTid());
                if (compareToken(tokenq, tokenr.getTid(), qTuple, rTuple) > 0) {
                    posq--;
                } else if (compareToken(tokenq, tokenr.getTid(), qTuple, rTuple) < 0) {
                    posr--;
                } else {
                    cpair.accu += qTuple.getIdf() * qTuple.getIdf() * tokenr.getTf() * qTfMap.get(tokenq);
                    posq--;
                    posr--;
                }
            }
            if (cpair.accu >= cpair.len * lenq * threshold && !resultSet.contains(id)) {
                resultSet.add(id);
            }
        }
        //        resultSet.stream().sorted().forEach(e -> System.out.println("result id " + e));

        return new ArrayList<Integer>(resultSet);
    }

    private int compareToken(int tokenq, int tokenr, TfIdfHeadTuple qTuple, TfIdfHeadTuple rTuple) {
        int cmp = Double.compare(rTuple.getIdf(), qTuple.getIdf());
        if (cmp == 0) {
            int cmp2 = Double.compare(rTuple.getTfmax(), qTuple.getTfmax());
            if (cmp2 == 0) {
                return Integer.compare(tokenq, tokenr);
            } else {
                return cmp2;
            }
        } else {
            return cmp;
        }
    }

    public long getCandiSize() {
        return candiSize;
    }

    public void setCandiSize(long candiSize) {
        this.candiSize = candiSize;
    }

    public void setqExtendedNum(int qExtendedNum) {
        this.qExtendedNum = qExtendedNum;
    }

    private class Cpair {
        double accu, rLap, len;
        TfTokenPair tokenEx[];
        int token;
        short[] tf;

        public Cpair(double len, int querySize) {
            this.accu = 0.0;
            this.len = len;
            this.tf = new short[querySize];
            Arrays.fill(tf, (short) 0);
        }

        @Override public String toString() {
            return "Cpair [len=" + len + ", accu=" + accu + ", rLap=" + rLap + "]";
        }
    }

    public long getPrefixTokens() {
        return falsePrefix;
    }

    public long getActualPrefix() {
        return actualPrefix;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public long getScanLength() {
        return scanLength;
    }

    public long getTotalLength() {
        return upperLength + scanLength;
    }

    public long getLowerLength() {
        return lowerLength;
    }

    public long getUpperLength() {
        return upperLength;
    }
}
