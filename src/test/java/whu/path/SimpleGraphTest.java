package whu.path;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.geotools.graph.structure.*;
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
        Node node = new BasicNode();
        node.setID(0);
        DirectPosition position = new DirectPosition2D(1, 1);
        Point point = new PointImpl(position);
        node.setObject(point);
        nodes.add(node);

        Graph graph = new BasicGraph();
        ((BasicGraph) graph).setNodes(nodes);
    }
}
