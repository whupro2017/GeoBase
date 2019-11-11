package whu.poi.accessor;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CSVNameTo2DPOIAccessor implements NameTo2DPOIAccessor {
    public CSVNameTo2DPOIAccessor(String basepath) {

    }

    @Override public Point2D.Double nameToBestPoint(String name) {
        return null;
    }

    @Override public Point2D.Double fuzzyMatchingBestPoint(String words) {
        return null;
    }

    @Override public Point2D.Double keywordsMatchingBestPoint(Collection<String> keywords) {
        return null;
    }

    @Override public List<Map.Entry<Point2D.Double, Double>> nameToTopKPoint(String name, int k) {
        return null;
    }

    @Override public List<Map.Entry<Point2D.Double, Double>> fuzzyMatchingTopKPoint(String words, int k) {
        return null;
    }

    @Override public List<Map.Entry<Point2D.Double, Double>> keywordsMatchingTopKPoint(Collection<String> keywords,
            int k) {
        return null;
    }
}
