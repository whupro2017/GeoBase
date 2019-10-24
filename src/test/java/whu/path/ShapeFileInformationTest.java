package whu.path;

import java.io.IOException;

public class ShapeFileInformationTest {

    public static void main(String[] argv) throws IOException {
        ShapeFileInformation sfi;
        if (argv.length >= 1) {
            sfi = new ShapeFileInformation(argv[0] + ".shp", argv[0] + ".dbf");
        } else {
            sfi = new ShapeFileInformation();
        }
        System.out.println(sfi.info());
        sfi.readFeatures();
        sfi.readDBF();
        sfi.infoDBF();
    }
}
