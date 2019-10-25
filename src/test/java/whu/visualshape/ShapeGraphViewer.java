package whu.visualshape;

import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicEdge;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Point;
import whu.path.ShapeFileManager;
import whu.path.ShapeGraph;

import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShapeGraphViewer extends JComponent {
    protected static final Logger LOGGER = Logging.getLogger(ShapeGraphViewer.class);
    private static int windowWidth = 1280;
    private static int windowHeight = 800;
    private final ShapeGraph graph;

    public ShapeGraphViewer(ShapeGraph graph) {
        this.graph = graph;
    }

    @Override public void paint(Graphics g) {
        Graph sg = graph.getGraph();
        Collection<Node> nodes = sg.getNodes();
        double minLongitude = Double.MAX_VALUE;
        double minLatitude = Double.MAX_VALUE;
        double maxLongitude = -0.1;
        double maxLatitude = -0.1;
        for (Node node : nodes) {
            Point point = (Point) node.getObject();
            if (point.getX() > maxLongitude)
                maxLongitude = point.getX();
            if (point.getX() < minLongitude)
                minLongitude = point.getX();
            if (point.getY() > maxLatitude)
                maxLatitude = point.getY();
            if (point.getY() < minLatitude)
                minLatitude = point.getY();
        }
        LOGGER.log(Level.INFO,
                "Point range : (" + minLatitude + "," + maxLatitude + "," + minLongitude + "," + maxLongitude + ")");

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(4.0f));
        g2.setPaint(Color.GREEN);
        int count = 0;
        for (Node node : nodes) {
            Point point = (Point) node.getObject();
            int x = (int) (windowWidth * (point.getX() - minLongitude) / (maxLongitude - minLongitude));
            int y = (int) (windowHeight * (point.getY() - minLatitude) / (maxLatitude - minLatitude));
            g2.drawLine(x, y, x, y);
        }
        LOGGER.log(Level.INFO, "Nodes drawn.");

        g2.setPaint(Color.RED);
        g2.setStroke(new BasicStroke(2.0f));
        Collection<Edge> edges = sg.getEdges();
        int emptyCount = 0;
        for (Edge e : edges) {
            GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            Iterator iter = e.getRelated();
            int tick = 0;
            if (!iter.hasNext()) {
                LOGGER.log(Level.WARNING, "\tShould be non-empty: " + (emptyCount++) + "<->" + e.getObject());
            }
            while (iter.hasNext()) {
                BasicEdge be = (BasicEdge) iter.next();
                Point end = (Point) be.getNodeB().getObject();
                int x = (int) (windowWidth * (end.getX() - minLongitude) / (maxLongitude - minLongitude));
                int y = (int) (windowHeight * (end.getY() - minLatitude) / (maxLatitude - minLatitude));
                if (tick++ == 0) {
                    Point start = (Point) be.getNodeB().getObject();
                    int sx = (int) (windowWidth * (start.getX() - minLongitude) / (maxLongitude - minLongitude));
                    int sy = (int) (windowHeight * (start.getY() - minLatitude) / (maxLatitude - minLatitude));
                    path.moveTo(sx, sy);
                }
                path.lineTo(x, y);
            }
            g2.draw(path);
        }
        LOGGER.log(Level.INFO, "Edges drawn.");
    }

    public static void main(String[] args) throws IOException {
        ShapeGraph sg = new ShapeGraph();
        sg.build();
        JFrame frame = new JFrame("Draw GeneralPath Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ShapeGraphViewer(sg));
        frame.pack();
        frame.setSize(new Dimension(windowWidth, windowHeight));
        frame.setVisible(true);
    }
}