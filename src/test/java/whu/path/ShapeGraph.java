package whu.path;

import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.graph.build.line.BasicLineGraphGenerator;
import org.geotools.graph.build.line.LineGraphGenerator;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.GraphVisitor;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.traverse.GraphIterator;
import org.geotools.graph.traverse.GraphTraversal;
import org.geotools.graph.traverse.basic.BasicGraphTraversal;
import org.geotools.graph.traverse.basic.SimpleGraphWalker;
import org.geotools.graph.traverse.standard.BreadthFirstIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ShapeGraph {
    private static class OrphanVisitor implements GraphVisitor {
        private int count = 0;

        public int getCount() {
            return count;
        }

        public int visit(Graphable component) {
            Iterator related = component.getRelated();
            if (related.hasNext() == false) {
                // no related components makes this an orphan
                count++;
            }
            return GraphTraversal.CONTINUE;
        }
    }

    public static void main(String[] argv) throws IOException {

        File file = new File("./resources/shapes/bj_shp/市区道路_polyline.shp");
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

        final LineGraphGenerator generator = new BasicLineGraphGenerator();
        SimpleFeatureCollection fc = new SimpleFeatureCollection() {
            @Override
            public SimpleFeatureIterator features() {
                return null;
            }

            @Override
            public SimpleFeatureCollection subCollection(Filter filter) {
                return null;
            }

            @Override
            public SimpleFeatureCollection sort(SortBy sortBy) {
                return null;
            }

            @Override
            public SimpleFeatureType getSchema() {
                return null;
            }

            @Override
            public String getID() {
                return null;
            }

            @Override
            public void accepts(FeatureVisitor featureVisitor, ProgressListener progressListener) throws IOException {

            }

            @Override
            public ReferencedEnvelope getBounds() {
                return null;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <O> O[] toArray(O[] os) {
                return null;
            }
        };

        fc.accepts(
                new FeatureVisitor() {
                    public void visit(Feature feature) {
                        generator.add(feature);
                    }
                },
                null);
        Graph graph = generator.getGraph();


        OrphanVisitor graphVisitor = new OrphanVisitor();

        SimpleGraphWalker sgv = new SimpleGraphWalker(graphVisitor);
        GraphIterator iterator = new BreadthFirstIterator();
        BasicGraphTraversal bgt = new BasicGraphTraversal(graph, sgv, iterator);

        bgt.traverse();

        System.out.println("Found orphans: " + graphVisitor.getCount());

    }
}
