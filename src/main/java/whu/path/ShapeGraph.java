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
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShapeGraph {
    protected static final Logger LOGGER = Logging.getLogger(ShapeGraph.class);

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
        SimpleFeatureSource source = ShapeFileManager.getFeatureSource(path);
        SimpleFeatureCollection fc = source.getFeatures();

        //create a linear graph generate
        LineStringGraphGenerator lineStringGen = new LineStringGraphGenerator();

        //wrap it in a feature graph generator
        FeatureGraphGenerator featureGen = new FeatureGraphGenerator(lineStringGen);

        //throw all the features into the graph generator
        FeatureIterator iter = fc.features();
        try {
            while (iter.hasNext()) {
                Feature feature = iter.next();
                featureGen.add(feature);
            }
        } finally {
            iter.close();
        }
        graph = featureGen.getGraph();

        /*LineStringGraphGenerator lineStringGen = new LineStringGraphGenerator();
        FeatureGraphGenerator featureGen = new FeatureGraphGenerator(lineStringGen);
        FeatureIterator iter = fc.features();

        while (iter.hasNext()) {
            featureGen.add(iter.next());
        }

        iter.close();

        graph = featureGen.getGraph();*/
        ShapeFileManager.disposeFeatureSource(source);
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public void traverse() {
        OrphanVisitor graphVisitor = new OrphanVisitor();

        SimpleGraphWalker sgv = new SimpleGraphWalker(graphVisitor);
        GraphIterator iterator = new BreadthFirstIterator();
        BasicGraphTraversal bgt = new BasicGraphTraversal(graph, sgv, iterator);
        for (Node node : graph.getNodes()) {
            ((BreadthFirstIterator) iterator).setSource(node);
            break;
        }
        bgt.traverse();

        LOGGER.log(Level.INFO, "Found orphans: " + graphVisitor.getCount());
    }

    public Path shortestPath(Node start, Node end) {
        DijkstraIterator.EdgeWeighter weighter = new DijkstraIterator.EdgeWeighter() {
            public double getWeight(Edge e) {
                SimpleFeature feature = (SimpleFeature) e.getObject();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                return geometry.getLength();
            }
        };

        DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(graph, start, weighter);
        pf.calculate();
        return pf.getPath(end);
    }
}
