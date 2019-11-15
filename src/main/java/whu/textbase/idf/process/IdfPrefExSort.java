package whu.textbase.idf.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import whu.textbase.btree.api.iSelect;
import whu.textbase.btree.common.KeyPair;
import whu.textbase.btree.core.Btree;
import whu.textbase.btree.core.BtreeCluster;
import whu.textbase.btree.utils.btree.BtreeClusterSp2;
import whu.textbase.idf.storage.index.IdfHeadTuple;
import whu.textbase.idf.storage.index.prefix.IdfPrefMergeIndex;
import whu.textbase.idf.storage.index.prefix.IdfTokenSet;
import whu.textbase.idf.storage.index.prefix.PrefNodeTuple;
import whu.textbase.idf.storage.index.prefix.PrefPair;

@Deprecated
public class IdfPrefExSort implements iSelect {

    private Btree<Integer, IdfTokenSet> recordBtree;
    private BtreeCluster<Integer, PrefNodeTuple> reverseBtree;
    private BtreeCluster<Integer, IdfHeadTuple> tokenBtree;
    private BtreeClusterSp2<KeyPair, Integer> lengthBtree;
    private int candiSize, qExtendedNum, rExtendedNum, prefixTokens, totalTokens, upperLength, lowerLength, scanLength;
    private boolean useLengthIndex;
    private int countSim, countCandidate;
    private boolean orderedCand;

    public IdfPrefExSort(IdfPrefMergeIndex index, int qExtendedNum, int rExtendedNum, boolean useLengthIndex,
                         boolean orderedCand) {
        this.recordBtree = index.getRecordBtree();
        this.reverseBtree = index.getReverseBtree();
        this.tokenBtree = index.getTokenBtree();
        this.lengthBtree = index.getLengthBtree();
        this.candiSize = prefixTokens = totalTokens = scanLength = upperLength = lowerLength = 0;
        this.countSim = this.countCandidate = 0;
        this.qExtendedNum = qExtendedNum;
        this.rExtendedNum = rExtendedNum;
        this.useLengthIndex = useLengthIndex;
        this.orderedCand = orderedCand;
    }

    @Override
    public List<Integer> find(List<Integer> queryString, double threshold) {
        // TODO Auto-generated method stub

        double accu = 0.0;
        List<Integer> query = queryString.stream().distinct().collect(Collectors.toList());
        query.sort((a, b) -> {
            int cmp = Double.compare(tokenBtree.find(b).idf, tokenBtree.find(a).idf);
            if (cmp == 0) {
                return a.compareTo(b);
            } else {
                return cmp;
            }
        });
        IdfHeadTuple[] headInfo = new IdfHeadTuple[query.size()];
        for (int i = 0; i < query.size(); i++) {
            IdfHeadTuple temp = tokenBtree.find(query.get(i));
            accu += temp.idf * temp.idf;
            headInfo[i] = temp;
        }
        double lenq = Math.sqrt(accu);

        Set<Integer> resultSet = new HashSet<Integer>();
        double prefix = (1 - threshold * threshold) * lenq * lenq;
        List<Double> qsuf = new ArrayList<Double>();
        double total = lenq * lenq;
        boolean lenflag = false;
        int tokenNum = query.size(), prefixNum;
        for (int i = 0; i < query.size(); i++) {
            double idf = headInfo[i].idf;
            double idfpow = idf * idf;
            qsuf.add(total);
            total -= idfpow;
            if (!lenflag) {
                if (prefix - idfpow < 0) {
                    tokenNum = i + 1;
                    lenflag = true;
                }
                prefix -= idfpow;
            }
        }
        for (int i = 0; i <= qExtendedNum; i++) {
            qsuf.add(0.0);
        }
        double qleft = threshold * lenq;
        double qright = lenq / threshold;
        Map<Integer, Cpair> candidate = new HashMap<Integer, Cpair>();
        prefixNum = tokenNum;
        tokenNum += qExtendedNum;
        if (tokenNum > query.size()) {
            tokenNum = query.size();
        }
        //        System.out.println("tokenNum=" + tokenNum + " total=" + query.size());
        prefixTokens += tokenNum;
        totalTokens += query.size();
        for (int i = 0; i < tokenNum; i++) {
            if (i >= query.size()) {
                break;
            }
            int token = query.get(i);
            double idf = headInfo[i].idf;
            PrefNodeTuple curTuple = null;
            if (useLengthIndex) {
                Integer addr = lengthBtree.find(new KeyPair(token, qleft));
                if (addr == null) {
                    curTuple = (PrefNodeTuple) reverseBtree.find(headInfo[i].head);
                } else {
                    curTuple = (PrefNodeTuple) reverseBtree.find(addr);
                }
            } else {
                curTuple = (PrefNodeTuple) reverseBtree.find(headInfo[i].head);
            }
            while (curTuple != null) {

                if (curTuple.pair.getLen() >= qleft) {
                    break;
                }
                lowerLength++;
                scanLength++;
                curTuple = (PrefNodeTuple) reverseBtree.find(curTuple.next);
            }
            while (curTuple != null) {
                scanLength++;
                if (curTuple.pair.getLen() > qright) {
                    /*while (curTuple != null) {
                        curTuple = (PrefNodeTuple) reverseBtree.find(curTuple.next);
                        upperLength++;
                    }*/
                    break;
                }
                int id = curTuple.pair.getId();
                Cpair cpair;
                if (candidate.containsKey(id)) {
                    cpair = candidate.get(id);
                } else {
                    cpair = new Cpair(threshold * curTuple.pair.getLen() * lenq);
                }
                double rLap = curTuple.pair.getLen() * curTuple.pair.getLen() - ((PrefPair) curTuple.pair).getAccu();
                double qLap = qsuf.get(i);
                double minsuf = Math.min(rLap, qLap);
                if (cpair.accu + minsuf >= cpair.overlapBound) {
                    cpair.rLap = rLap - idf * idf;
                    cpair.token = token;
                    cpair.count++;
                    cpair.tokenEx = new int[rExtendedNum];
                    System.arraycopy(((PrefPair) curTuple.pair).getTokenEx(), 0, cpair.tokenEx, 0, rExtendedNum);
                    cpair.accu += idf * idf;
                    if (!candidate.containsKey(id) && i < prefixNum) {
                        candidate.put(id, cpair);
                    }
                } else {
                    if (candidate.containsKey(id)) {
                        candidate.remove(id);
                    }
                }
                curTuple = (PrefNodeTuple) reverseBtree.find(curTuple.next);
            }
        }
        countCandidate += candidate.size();
        Iterator<Entry<Integer, Cpair>> it = candidate.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, Cpair> entry = it.next();
            Cpair cpair = entry.getValue();
            countSim += cpair.count;
            //            System.out.println("cpair id " + entry.getKey());
            int posq = tokenNum, posEx = 0;
            int[] tokenEx = cpair.tokenEx;
            double qLap = qsuf.get(tokenNum), rLap = cpair.rLap;

            while (posq < query.size() && posEx < tokenEx.length && tokenEx[posEx] != -1) {
                int tokenq = query.get(posq);
                IdfHeadTuple qTuple = headInfo[posq];
                IdfHeadTuple rTuple = tokenBtree.find(tokenEx[posEx]);
                int cmp = compareToken(tokenq, tokenEx[posEx], qTuple, rTuple);
                if (cmp > 0) {
                    rLap -= rTuple.idf * rTuple.idf;
                    posEx++;
                } else if (cmp < 0) {
                    qLap -= qTuple.idf * qTuple.idf;
                    posq++;
                } else {
                    double idfqpow = qTuple.idf * qTuple.idf;
                    cpair.accu += idfqpow;
                    cpair.token = tokenq;
                    rLap -= idfqpow;
                    qLap -= idfqpow;
                    posq++;
                    posEx++;
                }
            }
            double minsuf = Math.min(rLap, qLap);
            if (cpair.accu + minsuf < cpair.overlapBound) {
                it.remove();
            } else if (cpair.accu >= cpair.overlapBound) {
                resultSet.add(entry.getKey());
                it.remove();
            }
        }
        if (orderedCand) {
            List<Entry<Integer, Cpair>> candidateList = new ArrayList<Entry<Integer, Cpair>>(candidate.entrySet());
            candidateList.sort((a, b) -> {
                return a.getKey().compareTo(b.getKey());
            });
            it = candidateList.iterator();
            candiSize += candidateList.size();
        } else {
            it = candidate.entrySet().iterator();
            candiSize = candidate.size();
        }
        while (it.hasNext()) {
            Entry<Integer, Cpair> entry = it.next();
            Cpair cpair = entry.getValue();
            int id = entry.getKey();
            List<Integer> record = recordBtree.find(id).getRecord();
            int posq = query.size() - 1, posr = record.size() - 1;
            while (posq >= tokenNum) {
                int tokenr = record.get(posr);
                if (tokenr == cpair.token) {
                    break;
                }
                int tokenq = query.get(posq);
                IdfHeadTuple qTuple = headInfo[posq];
                int cmp = compareToken(tokenq, tokenr, qTuple, tokenBtree.find(tokenr));
                if (cmp > 0) {
                    posq--;
                } else if (cmp < 0) {
                    posr--;
                } else {
                    cpair.accu += qTuple.idf * qTuple.idf;
                    posq--;
                    posr--;
                }
            }
            if (cpair.accu >= cpair.overlapBound) {
                resultSet.add(id);
            }
        }
        return new ArrayList<Integer>(resultSet);
    }

    private int compareToken(int tokenq, int tokenr, IdfHeadTuple qTuple, IdfHeadTuple rTuple) {
        int cmp = Double.compare(rTuple.idf, qTuple.idf);
        if (cmp == 0) {
            return Integer.compare(tokenq, tokenr);
        } else {
            return cmp;
        }
    }

    public int getCandiSize() {
        return candiSize;
    }

    public void setCandiSize(int candiSize) {
        this.candiSize = candiSize;
    }

    public void setqExtendedNum(int qExtendedNum) {
        this.qExtendedNum = qExtendedNum;
    }

    private class Cpair {
        double overlapBound, accu, rLap;
        int tokenEx[], token;
        int count;

        public Cpair(double threshold) {
            this.accu = 0.0;
            this.count = 0;
            this.overlapBound = threshold;
        }

        @Override
        public String toString() {
            return "Cpair [overlapBound=" + overlapBound + ", accu=" + accu + ", rLap=" + rLap + "]";
        }
    }

    public long getPrefixTokens() {
        return prefixTokens;
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

    public int getCountCandidate() {
        return countCandidate;
    }

    public int getCountSim() {
        return countSim;
    }
}
