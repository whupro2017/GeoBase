package whu.path;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.*;
import org.geotools.graph.traverse.GraphIterator;
import org.geotools.graph.traverse.GraphTraversal;
import org.geotools.graph.traverse.basic.BasicGraphTraversal;
import org.geotools.graph.traverse.basic.SimpleGraphWalker;
import org.geotools.graph.traverse.standard.BreadthFirstIterator;
import org.geotools.graph.traverse.standard.DijkstraIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Geometry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PathUnitTest {
    private String path = "./resources/shapes/sz_shp/allroads.shp";
    SimpleFeatureSource dataSource;
    SimpleFeatureCollection simFeatureCollect;
    Graph graph;

    @Before
    public void initGraph() throws Exception {
        dataSource = ShapeFileManager.getFeatureSource(path);
        simFeatureCollect = dataSource.getFeatures();

        LineStringGraphGenerator lineStringGen = new LineStringGraphGenerator();
        FeatureGraphGenerator featureGen = new FeatureGraphGenerator(lineStringGen);
        FeatureIterator iter = simFeatureCollect.features();
        try {
            while (iter.hasNext()) {
                Feature feature = iter.next();
                featureGen.add(feature);
            }
        } finally {
            iter.close();
        }
        graph = featureGen.getGraph();
    }

    @Test
    public void tracerseTest() throws Exception {
        class OrphanVisitor implements GraphVisitor {
            private int count = 0;

            public int getCount() {
                return count;
            }

            public int visit(Graphable component) {
                Iterator related = component.getRelated();
                if (related.hasNext() == false) {
                    count++;
                }
                return GraphTraversal.CONTINUE;
            }
        }
        OrphanVisitor graphVisitor = new OrphanVisitor();

        SimpleGraphWalker sgv = new SimpleGraphWalker(graphVisitor);
        GraphIterator iterator = new BreadthFirstIterator();
        BasicGraphTraversal bgt = new BasicGraphTraversal(graph, sgv, iterator);
        for (Node node : graph.getNodes()) {
            ((BreadthFirstIterator) iterator).setSource(node);
            break;
        }
        bgt.traverse();
        assert (graphVisitor.getCount() == 0);
    }

    @Test
    public void DijkstraTest() throws Exception {
        DijkstraIterator.EdgeWeighter weighter = new DijkstraIterator.EdgeWeighter() {
            public double getWeight(Edge e) {
                SimpleFeature feature = (SimpleFeature) e.getObject();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                return geometry.getLength();
            }
        };
        List<Node> destinations = new ArrayList<>();

        for (Node node : graph.getNodes()) {
            destinations.add(node);
        }
        for (Node node : graph.getNodes()) {
            Node start = node;
            System.out.println(start.toString() + ": <" + start.getObject().toString() + ">");
            // Create GraphWalker - in this case DijkstraShortestPathFinder
            DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(graph, start, weighter);
            pf.calculate();

            int count = 0;
            for (Iterator d = destinations.iterator(); d.hasNext(); ) {
                Node destination = (Node) d.next();
                Path path = pf.getPath(destination);
                if (path != null)
                    System.out.println("\t" + count++ + "<->" + path.size() + ":" + path.toString());
            }
            break;
        }
    }

    @After
    public void disposeGraph() throws Exception {
        ShapeFileManager.disposeFeatureSource(dataSource);
    }
}
