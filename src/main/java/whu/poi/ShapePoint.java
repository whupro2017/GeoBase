package whu.poi;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Point;
import whu.path.ShapeFileManager;
import whu.utils.ConsoleColors;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShapePoint implements Iterator<String> {
    protected static final Logger LOGGER = Logging.getLogger(ShapePoint.class);

    private final static boolean preprint = false;

    private String path = "./resources/shapes/sz_shp/医疗_point.shp";

    private SimpleFeatureSource source;

    private SimpleFeatureCollection fc;

    SimpleFeatureIterator iterator;

    public ShapePoint() {
        try {
            build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }

    public ShapePoint(String path) {
        this.path = path;
        try {
            build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }

    private void build() throws IOException {
        source = ShapeFileManager.getFeatureSource(path);
        fc = source.getFeatures();
        if (preprint) {
            try (SimpleFeatureIterator iter = fc.features()) {
                while (iter.hasNext()) {
                    SimpleFeature feature = iter.next();
                    Attribute proptery = (Attribute) feature.getProperty("NAME");
                    Point point = (Point) feature.getDefaultGeometry();
                    System.out.println(ConsoleColors.RED + proptery.getValue().toString() + ConsoleColors.RESET + "\t"
                            + ConsoleColors.BLUE + point.getX() + "," + point.getY() + ConsoleColors.RESET);
                }
            }
        }
        iterator = fc.features();
    }

    public void close() {
        ShapeFileManager.disposeFeatureSource(source);
    }

    @Override public boolean hasNext() {
        return iterator != null && iterator.hasNext();
    }

    @Override public String next() {
        SimpleFeature feature = iterator.next();
        Attribute name = (Attribute) feature.getProperty("NAME");
        Attribute kind = (Attribute) feature.getProperty("KIND");
        Point point = (Point) feature.getDefaultGeometry();
        String output = "";
        if (point != null) {
            output += point.getX() + "," + point.getY();
        }
        if (name != null) {
            output += "," + name.getValue().toString();
        }
        if (kind != null) {
            output += "," + kind.getValue().toString();
        }
        return output;
    }
}
