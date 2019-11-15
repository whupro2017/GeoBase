package whu.poi;

import org.junit.Test;
import whu.utils.ConsoleColors;

import java.io.File;

public class ShapePointTest {

    private static int cnt = 0;

    @Test
    public void NameTest() {
        ShapePoint sp = new ShapePoint();
        while (sp.hasNext()) {
            System.out.println(cnt++ + ":" + sp.next());
        }
        sp.close();
    }

    @Test
    public void AllPointsTest() {
        for (File file : new File("./resources/shapes/sz_shp").listFiles()) {
            if (file.getAbsolutePath().contains("point") && file.getAbsolutePath().endsWith(".shp")) {
                System.out.println(ConsoleColors.RED + file.getAbsolutePath() + ConsoleColors.RESET);

                ShapePoint sp = new ShapePoint(file.getAbsolutePath());
                int cnt = 0;
                while (sp.hasNext()) {
                    System.out.println("\t" + cnt++ + ":" + sp.next());
                }
                sp.close();
            }
        }
    }
}
