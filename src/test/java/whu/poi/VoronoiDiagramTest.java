package whu.poi;

import org.geotools.xml.gml.GMLComplexTypes;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.Collection;
import java.util.Iterator;

public class VoronoiDiagramTest {
    private static int cnt = 0;

    @Test public void ProcessTest() {
        ShapePoint sp = new ShapePoint();
        VoronoiDiagram vd = new VoronoiDiagram(sp.getSimpleFeatureCollection());
        Collection<Geometry> geometries = vd.getGeometries();
        Iterator<Geometry> iterator = geometries.iterator();
        while (iterator.hasNext()) {
            Geometry geometry = iterator.next();
            Coordinate[] coords = geometry.getCoordinates();
            int i = 0;
        }
        sp.close();
    }
}
