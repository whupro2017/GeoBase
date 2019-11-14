package whu.textbase.btree.analyzer;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class KMLToCSV {
    private static final boolean pt = false;
    private static final String path = "./resources/texts/sz_poi/housingtencent.csv";

    public static void main(String[] args) throws IOException {
        SAXReader reader = new SAXReader();
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        try {
            Document document = reader.read(new File("C:/Users/Michael/Downloads/房产小区-tencent.kml"));
            Element root = document.getRootElement();
            Element doc = root.element("Document");
            Iterator it = doc.elementIterator();
            while (it.hasNext()) {
                Element mark = (Element) it.next();
                Element desc = mark.element("description");
                Element card = mark.element("Point").element("coordinates");
                String[] fields = desc.getText().split("\n");
                bw.write(card.getText() + ",");
                bw.write(fields[0].replaceAll("名称:", "").trim() + ",");
                bw.write(fields[1].replaceAll("地址:", "").trim() + ",");
                bw.write(fields[2].replaceAll("电话:", "").trim() + ",");
                bw.write(fields[3].replaceAll("类别:", "").trim());
                if (pt) {
                    System.out.print(card.getText() + ",");
                    for (int i = 0; i < fields.length; i++) {
                        System.out.print(fields[i] + ",");
                    }
                    System.out.println();
                }
                bw.write("\n");
            }
            int i = 0;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        bw.close();
    }
}
