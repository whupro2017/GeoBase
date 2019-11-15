package whu.poi;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VoronoiDiagram {
    protected static final Logger LOGGER = Logging.getLogger(VoronoiDiagram.class);
    private final SimpleFeatureCollection sfCollection;

    private final VoronoiDiagramBuilder vdBuilder;

    private List<Geometry> geosList;

    public VoronoiDiagram(SimpleFeatureCollection fc) {
        sfCollection = fc;
        vdBuilder = new VoronoiDiagramBuilder();
    }

    public void process() {
        /*if (sfCollection.getSchema().getGeometryDescriptor().getType()) {
            LOGGER.log(Level.SEVERE, sfCollection.getSchema().getGeometryDescriptor().getName().toString());
            throw new InvalidGridGeometryException(
                    sfCollection.getSchema().getGeometryDescriptor().getName().toString());
        }*/
        FeatureIterator iter = sfCollection.features();
        CoordinateReferenceSystem crs = sfCollection.getBounds().getCoordinateReferenceSystem();
        List<Coordinate> cList = new ArrayList<>();
        try {
            while (iter.hasNext()) {
                SimpleFeature feature = (SimpleFeature) iter.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                cList.add(geometry.getCoordinate());
            }
        } finally {
            LOGGER.log(Level.SEVERE, "iter failed: " + sfCollection.getSchema());
            iter.close();
        }
        vdBuilder.setSites(cList);
        geosList = new ArrayList<>();
        Geometry geometry = vdBuilder.getDiagram(new GeometryFactory());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry subgeo = geometry.getGeometryN(i);
            Coordinate[] coordinates = subgeo.getCoordinates();
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (Coordinate coordinate : coordinates) {
                min = Math.min(min, coordinate.z);
                max = Math.max(max, coordinate.z);
            }
            subgeo.setUserData(new String[] { "" + min, "" + max });
            geosList.add(subgeo);
        }
    }

    public Collection<Geometry> getGeometries() {
        if (geosList == null)
            process();
        return geosList;
    }
}
