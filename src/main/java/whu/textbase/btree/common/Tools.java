package whu.textbase.btree.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import whu.textbase.btree.analyzer.IMDBGramTokenizer;
import whu.textbase.btree.api.iTokenizer;
import whu.textbase.btree.core.BtreeCluster;
import whu.textbase.idf.storage.index.IdfHeadTuple;
import whu.textbase.tfidf.storage.reader.TfTokenPair;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class Tools {
    public enum FuncType {
        l2,
        wc,
        wj,
        wd
    }

    public static FuncType func = FuncType.l2;

    public static Map<Integer, Double> idfCompute(Map<Integer, Integer> freqMap, int totalDocNum) {
        Map<Integer, Double> idfMap = new HashMap<Integer, Double>();
        for (Iterator<Map.Entry<Integer, Integer>> iter = freqMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Integer, Integer> entry = iter.next();
            idfMap.put(entry.getKey(), Math.log(1 + totalDocNum * 1.0 / entry.getValue()) / Math.log(2));
        }
        return idfMap;
    }

    public static Map<Integer, Double> bm25IdfCompute(Map<Integer, Integer> freqMap, int totalDocNum) {
        Map<Integer, Double> idfMap = new HashMap<Integer, Double>();
        for (Iterator<Map.Entry<Integer, Integer>> iter = freqMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Integer, Integer> entry = iter.next();
            idfMap.put(entry.getKey(), Math.log((totalDocNum - entry.getValue() + 0.5) / (entry.getValue() + 0.5)));
        }
        return idfMap;
    }

    public static Double l2Compute(List<Integer> strList, BtreeCluster<Integer, Double> idfBtree) {
        Double accu = 0.0;
        for (int i = 0; i < strList.size(); i++) {
            double idf = idfBtree.find(strList.get(i));
            //            System.out.println(idf);
            accu += idf * idf;
        }
        return Math.sqrt(accu);
    }

    public static Double l2Compute(List<Integer> strList, Map<Integer, Double> idfMap) {
        Double accu = 0.0;
        for (int i = 0; i < strList.size(); i++) {
            double idf = idfMap.get(strList.get(i));
            accu += idf * idf;
        }
        return Math.sqrt(accu);
    }

    public static Double wCompute(List<Integer> strList, BtreeCluster<Integer, Double> idfBtree) {
        Double accu = .0;
        for (int i = 0; i < strList.size(); i++) {
            accu += idfBtree.find(strList.get(i));
        }
        return accu;
    }

    public static Double wCompute(List<Integer> strList, Map<Integer, Double> idfMap) {
        Double accu = .0;
        for (int i = 0; i < strList.size(); i++) {
            accu += idfMap.get(strList.get(i));
        }
        return accu;
    }

    public static Double awCompute(List<Integer> strList, BtreeCluster<Integer, Double> idfBtree) {
        switch (func) {
            case l2:
                return l2Compute(strList, idfBtree);
            case wc:
            case wj:
            case wd:
                return wCompute(strList, idfBtree);
            default:
                System.err.println("Not support type: " + func);
                return -1.0;
        }
    }

    public static Double awCompute(List<Integer> strList, Map<Integer, Double> idfMap) {
        switch (func) {
            case l2:
                return l2Compute(strList, idfMap);
            case wc:
            case wj:
            case wd:
                return wCompute(strList, idfMap);
            default:
                System.err.println("Not support type: " + func);
                return -1.0;
        }
    }

    public static Double cbCompute(double tokenWeight) {
        switch (func) {
            case l2:
                return tokenWeight * tokenWeight;
            case wc:
            case wj:
            case wd:
                return tokenWeight;
            default:
                System.err.println("Not support type: " + func);
                return -1.0;
        }
    }

    public static Double twCompute(double totalWeight) {
        switch (func) {
            case l2:
                return Math.sqrt(totalWeight);
            case wc:
            case wj:
            case wd:
                return totalWeight;
            default:
                System.err.println("Not support type: " + func);
                return -1.0;
        }
    }

    public static Double tfIdfLengthCompute(List<TfTokenPair> strList, Map<Integer, Double> idfMap) {
        Double accu = 0.0;
        for (int i = 0; i < strList.size(); i++) {
            TfTokenPair tokenPair = strList.get(i);
            double idf = idfMap.get(tokenPair.getTid());
            accu += idf * tokenPair.getTf();
        }
        return accu;
    }

    public static Double tfIdfLengthCompute2(List<TfTokenPair> strList, Map<Integer, Double> idfMap) {
        Double accu = 0.0;
        for (int i = 0; i < strList.size(); i++) {
            TfTokenPair tokenPair = strList.get(i);
            double idf = idfMap.get(tokenPair.getTid());
            accu += idf * idf * tokenPair.getTf() * tokenPair.getTf();
        }
        return Math.sqrt(accu);
    }

    public static Double partialContributeCompute(int token, Double lens, Double lenq, Map<Integer, Double> idfMap) {
        double idf = idfMap.get(token);
        return idf * idf / (lens * lenq);
    }

    public static Double partialContributeCompute(int token, Double lens, Double lenq,
                                                  BtreeCluster<Integer, IdfHeadTuple> tokenBtree) {
        double idf = tokenBtree.find(token).idf;
        return idf * idf / (lens * lenq);
    }

    public static void sortByFreq(List<Integer> record, Map<Integer, Integer> freqMap) {
        Collections.sort(record, new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                Integer idfa = freqMap.get(a), idfb = freqMap.get(b);
                if (idfa.equals(idfb)) {
                    return a.compareTo(b);
                } else {
                    return idfa.compareTo(idfb);
                }
            }
        });
    }

    public static void sortByIdf(List<Integer> record, Map<Integer, Double> idfMap) {
        Collections.sort(record, new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                Double idfa = idfMap.get(a), idfb = idfMap.get(b);
                if (idfa.equals(idfb)) {
                    return a.compareTo(b);
                } else {
                    return idfb.compareTo(idfa);
                }
            }
        });
    }

    public static void takeUpMemory(int size) {
        int[][] arr = new int[size][268435456];
    }

    public static int compareToken(int token1, int token2, double idf1, double idf2) {
        int cmp = Double.compare(idf2, idf1);
        if (cmp == 0) {
            return Integer.compare(token1, token2);
        } else {
            return cmp;
        }
    }

    public static void stanfordDataFormat(String srcPath, String destPath, int recordNum) {
        try {
            BufferedReader srcReader = new BufferedReader(new FileReader(srcPath));
            BufferedWriter destWriter = new BufferedWriter(new FileWriter(destPath));
            String line = null;
            int count = 0;
            while ((line = srcReader.readLine()) != null && count < recordNum) {
                destWriter.write(line.substring(20).trim() + "\n");
                count++;
            }
            destWriter.close();
            srcReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void imdbDataFormat(String moviePath, String actorPath, String destPath, int gramNum, int recordNum,
                                      double ratio) {
        try {
            BufferedReader movieReader = new BufferedReader(new FileReader(moviePath));
            BufferedReader actorReader = new BufferedReader(new FileReader(actorPath));
            int mNum = (int) (recordNum * ratio);
            int aNum = recordNum - mNum;
            System.out.println("acotrs=" + aNum + " movies=" + mNum + " gram=" + gramNum);
            BufferedWriter destWriter = new BufferedWriter(new FileWriter(destPath));
            iTokenizer tokenizer = new IMDBGramTokenizer(gramNum);
            int minlen = Integer.MAX_VALUE, maxlen = Integer.MIN_VALUE, mcount = 0, acount = 0;
            long avglen = 0;
            String line = null;
            while ((line = movieReader.readLine()) != null) {
                String[] split = line.trim().split("\t");
                if (mcount == 0) {
                    mcount++;
                    continue;
                }
                String[] str = tokenizer.tokenize(split[2].trim());
                mcount++;
                if (str.length > maxlen) {
                    maxlen = str.length;
                }
                if (str.length < minlen) {
                    minlen = str.length;
                }
                avglen += str.length;
                for (int i = 0; i < str.length - 1; i++) {
                    destWriter.write(str[i] + " ");
                }
                destWriter.write(str[str.length - 1] + "\n");
                if (mcount > mNum) {
                    break;
                }
            }

            while ((line = actorReader.readLine()) != null) {
                String[] split = line.trim().split("\t");
                if (acount == 0) {
                    acount++;
                    continue;
                }
                String[] str = tokenizer.tokenize(split[1].trim());
                acount++;
                if (str.length > maxlen) {
                    maxlen = str.length;
                }
                if (str.length < minlen) {
                    minlen = str.length;
                }
                avglen += str.length;
                for (int i = 0; i < str.length - 1; i++) {
                    destWriter.write(str[i] + " ");
                }
                destWriter.write(str[str.length - 1] + "\n");
                if (acount > aNum) {
                    break;
                }
            }
            System.out.println(
                    "maxlen = " + maxlen + " minlen = " + minlen + " avglen = " + avglen * 1.0 / (mcount + acount));
            destWriter.close();
            actorReader.close();
            movieReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void pubmedDataFormat(String srcPath, String destPath) {
        try {
            File dir = new File(srcPath);
            int count = 0;
            BufferedWriter destWriter = new BufferedWriter(new FileWriter(destPath));
            for (File file : dir.listFiles()) {
                BufferedReader srcReader = new BufferedReader(new FileReader(file));
                System.out.println(file);
                String line = null;
                while ((line = srcReader.readLine()) != null) {
                    if (line.trim().startsWith("<ArticleTitle>")) {
                        destWriter.write(
                                line.trim().replaceAll("(?:<ArticleTitle>|</ArticleTitle>|\\.|,|\\?|!)", "") + "\n");
                        count++;
                    }
                    //                    if (line.trim().startsWith("<AbstractText>")) {
                    //                        destWriter.write(
                    //                                line.trim().replaceAll("(?:<AbstractText>|</AbstractText>|\\.|,|\\?|!)", "") + "\n");
                    //                        count++;
                    //                    }
                }
                srcReader.close();
            }
            System.out.println("RecordNum " + count);
            destWriter.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void amazonDataFormat(String srcPath, String destPath, String field) {
        JsonParser parse = new JsonParser();
        try {
            BufferedReader srcReader = new BufferedReader(new FileReader(srcPath));
            BufferedWriter destWriter = new BufferedWriter(new FileWriter(destPath));
            String line = null;
            while ((line = srcReader.readLine()) != null) {
                JsonObject json = (JsonObject) parse.parse(line);
                String str = json.get(field).getAsString();
                destWriter.write(str + "\n");
            }
            srcReader.close();
            destWriter.close();

        } catch (JsonIOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void twitterDataFormat(String srcPath, String destPath) {
        try {
            File file = new File(srcPath);
            int count = 0;
            BufferedWriter destWriter = new BufferedWriter(new FileWriter(destPath));
            BufferedReader srcReader = new BufferedReader(new FileReader(file));
            String line = null;
            Pattern pattern = Pattern.compile(".*\"text\":\"(.*?)\",\".*$");
            long wordCount = 0;
            while ((line = srcReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.trim());
                if (matcher.matches()) {
                    String temp = matcher.group(1);
                    String[] httpTemp = temp.split("\\s+");
                    StringBuilder httpRe = new StringBuilder();
                    for (int i = 0; i < httpTemp.length; i++) {
                        if (!httpTemp[i].toLowerCase().matches(".*http.*:.*")) {
                            httpRe.append(httpTemp[i] + " ");
                        }
                    }

                    String temp2 = httpRe.toString().trim().replaceAll("null", " ")
                            .replaceAll("[0-9]| '|' |,|\\.|!|\\?|@|#|:", " ").trim();
                    String[] temp3 = temp2.split("\\s+|-|_");
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < temp3.length; i++) {
                        boolean flag = false;
                        for (int j = 0; j < temp3[i].length(); j++) {
                            if (!(temp3[i].charAt(j) <= 'Z' && temp3[i].charAt(j) >= 'A'
                                    || temp3[i].charAt(j) <= 'z' && temp3[i].charAt(j) >= 'a'
                                    || temp3[i].charAt(j) == '\'')) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            if (i < temp3.length - 1) {
                                builder.append(temp3[i] + " ");
                            } else {
                                builder.append(temp3[i]);
                            }
                        }
                    }
                    String str = builder.toString().trim();
                    String[] temp4 = builder.toString().split("\\s+");

                    if (!str.equals("")) {
                        wordCount += temp4.length;
                        destWriter.write(builder.toString() + "\n");
                        count++;
                    }
                }
            }
            srcReader.close();
            System.out.println("RecordNum " + count);
            System.out.println("avg len " + wordCount * 1.0 / count);
            destWriter.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void distinct(String srcPath, String destPath, int num) {
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(new File(srcPath)), "utf-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {

            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void stopWordsDel(String srcPath, String destPath, String swPath, String regex, int num,
                                    int lengthMin) {
        try {
            BufferedReader srcReader = new BufferedReader(new FileReader(srcPath));
            BufferedReader swReader = new BufferedReader(new FileReader(swPath));
            BufferedWriter destWriter = new BufferedWriter(new FileWriter(destPath));
            Set<String> set = new HashSet<String>();
            String line = null;
            while ((line = swReader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    set.add(words[i].trim());
                }
                for (int i = 0; i < 26; i++) {
                    set.add(String.valueOf((char) ('a' + i)));
                }
            }
            long count = 0, lineCount = 0;
            while ((line = srcReader.readLine()) != null && lineCount < num) {
                String[] words = line.toLowerCase().split("\\s+");
                StringBuffer destStr = new StringBuffer();

                for (int i = 0; i < words.length; i++) {
                    if (!set.contains(words[i].trim())) {
                        destStr.append(words[i].trim() + " ");
                    }
                }
                String str = destStr.toString().trim();
                String[] temp = str.split("\\s+");
                if (temp.length > lengthMin) {
                    lineCount++;
                    count += temp.length;
                    destWriter.write(str + "\n");
                }
            }
            System.out.println("word count " + count);
            System.out.println("line count " + lineCount);
            System.out.println("avg len " + count * 1.0 / lineCount);
            destWriter.close();
            srcReader.close();
            swReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
