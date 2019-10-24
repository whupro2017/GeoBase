package whu.path;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.graph.build.line.BasicLineGraphGenerator;
import org.geotools.graph.build.line.LineGraphGenerator;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.structure.*;
import org.geotools.graph.traverse.GraphIterator;
import org.geotools.graph.traverse.GraphTraversal;
import org.geotools.graph.traverse.basic.BasicGraphTraversal;
import org.geotools.graph.traverse.basic.SimpleGraphWalker;
import org.geotools.graph.traverse.standard.BreadthFirstIterator;
import org.geotools.graph.traverse.standard.DijkstraIterator;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

public class ShapeGraph {
    private class OrphanVisitor implements GraphVisitor {
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

    private String path = "./resources/shapes/sz_shp/乡镇村道_polyline.shp";

    private Graph graph;

    public ShapeGraph() {
    }

    public ShapeGraph(String path) {
        this.path = path;
    }

    public void build() throws IOException {
        /*File file = new File("./resources/shapes/sz_shp/乡镇村道_polyline.shp");
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

        final LineGraphGenerator generator = new BasicLineGraphGenerator();
        SimpleFeatureCollection fc = source.getFeatures();*/

        //File file = new File("./resources/shapes/bj_shp/市区杂路_polyline.shp");

        SimpleFeatureSource source = ShapeFileManager.getFeatureSource(path);
        SimpleFeatureCollection fc = source.getFeatures();
        SimpleFeatureIterator iter = fc.features();
        int fid = 0;
        while (iter.hasNext()) {
            SimpleFeature f = iter.next();
            int uid = 0;
            FeatureType type = f.getFeatureType();
            System.out.println(fid + "<->" + type.toString() + "<->" + f.getValue());
            if (fid++ > 100)
                break;
        }
        final LineGraphGenerator generator = new BasicLineGraphGenerator();
        fc.accepts(new FeatureVisitor() {
            public void visit(Feature feature) {
                generator.add(feature);
            }
        }, null);
        graph = generator.getGraph();
        ShapeFileManager.disposeFeatureSource(source);
    }

    public void traverse() {

        OrphanVisitor graphVisitor = new OrphanVisitor();

        SimpleGraphWalker sgv = new SimpleGraphWalker(graphVisitor);
        GraphIterator iterator = new BreadthFirstIterator();
        BasicGraphTraversal bgt = new BasicGraphTraversal(graph, sgv, iterator);

        bgt.traverse();

        System.out.println("Found orphans: " + graphVisitor.getCount());

    }

    public void shortestPath() {
        Node start = null;

        DijkstraIterator.EdgeWeighter weighter = new DijkstraIterator.EdgeWeighter() {
            public double getWeight(Edge e) {
                SimpleFeature feature = (SimpleFeature) e.getObject();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                return geometry.getLength();
            }
        };

        DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(graph, start, weighter);
    }
}
