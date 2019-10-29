package whu.path;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.basic.BasicEdge;
import org.geotools.graph.structure.basic.BasicGraph;
import org.geotools.graph.structure.basic.BasicNode;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.primitive.Point;

import java.util.Collection;
import java.util.Vector;

public class SimpleGraphTest {
    public static void main(String[] args) {
        SimpleGraph sg = new SimpleGraph();
        Collection<Node> nodes = new Vector<>();
        Collection<Edge> edges = new Vector<>();
        Node node0 = new BasicNode();
        node0.setID(0);
        DirectPosition pos = new DirectPosition2D(0.5, 0.5);
        System.out.println(0.5 + "," + 0.5);
        Point p = new PointImpl(pos);
        node0.setObject(p);
        nodes.add(node0);

        for (int i = 1; i < 5; i++) {
            Node node = new BasicNode();
            node.setID(i);
            DirectPosition position = new DirectPosition2D(i & 0x1, (i & 0x2) >> 1);
            System.out.println((i & 0x1) + "," + ((i & 0x2) >> 1));
            Point point = new PointImpl(position);
            node.setObject(point);
            node.setVisited(true);
            nodes.add(node);

            Edge edge = new BasicEdge(node0, node);
            edge.setID(0);
            edge.setVisited(true);
            edges.add(edge);
        }

        Graph graph = new BasicGraph();
        ((BasicGraph) graph).setNodes(nodes);
        ((BasicGraph) graph).setEdges(edges);
        sg.setGraph(graph);
        sg.traverse();
    }
}
