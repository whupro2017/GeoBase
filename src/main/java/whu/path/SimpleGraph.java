package whu.path;

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
import org.opengis.feature.simple.SimpleFeature;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleGraph {
    protected static final Logger LOGGER = Logging.getLogger(SimpleGraph.class);
    private Graph graph;

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

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
