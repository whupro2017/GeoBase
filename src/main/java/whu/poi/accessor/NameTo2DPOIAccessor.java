package whu.poi.accessor;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

public interface NameTo2DPOIAccessor {
    Point2D.Double nameToBestPoint(String name);

    Point2D.Double fuzzyMatchingBestPoint(String words);

    Point2D.Double keywordsMatchingBestPoint(Collection<String> keywords);

    List<Entry<Point2D.Double, Double>> nameToTopKPoint(String name, int k);

    List<Entry<Point2D.Double, Double>> fuzzyMatchingTopKPoint(String words, int k);

    List<Entry<Point2D.Double, Double>> keywordsMatchingTopKPoint(Collection<String> keywords, int k);
}
