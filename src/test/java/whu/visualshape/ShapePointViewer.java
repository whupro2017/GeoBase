package whu.visualshape;

import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import whu.poi.ShapePoint;
import whu.poi.VoronoiDiagram;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShapePointViewer extends JComponent {
    protected static final Logger LOGGER = Logging.getLogger(ShapePointViewer.class);
    private static int windowWidth = 1280;
    private static int windowHeight = 800;
    private final ShapePoint shapePoint;

    public ShapePointViewer(ShapePoint shapePoint) {
        this.shapePoint = shapePoint;
    }

    @Override
    public void paint(Graphics g) {
        Collection<Point> points = shapePoint.getPointList();
        double minLongitude = Double.MAX_VALUE;
        double minLatitude = Double.MAX_VALUE;
        double maxLongitude = -0.1;
        double maxLatitude = -0.1;
        for (Point point : points) {
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

        Graphics2D g1 = (Graphics2D) g;
        g1.setStroke(new BasicStroke(1.0f));
        g1.setPaint(Color.GREEN);
        VoronoiDiagram vd = new VoronoiDiagram(shapePoint.getSimpleFeatureCollection());
        Iterator<Geometry> iter = vd.getGeometries().iterator();
        while (iter.hasNext()) {
            Geometry geometry = iter.next();
            Coordinate[] coords = geometry.getCoordinates();
            int x[] = new int[coords.length];
            int y[] = new int[coords.length];
            for (int i = 0; i < coords.length; i++) {
                x[i] = (int) (windowWidth * (coords[i].getX() - minLongitude) / (maxLongitude - minLongitude));
                y[i] = (int) (windowHeight * (coords[i].getY() - minLatitude) / (maxLatitude - minLatitude));
            }
            g.drawPolygon(x, y, coords.length);
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(4.0f));
        g2.setPaint(Color.RED);
        for (Point point : points) {
            int x = (int) (windowWidth * (point.getX() - minLongitude) / (maxLongitude - minLongitude));
            int y = (int) (windowHeight * (point.getY() - minLatitude) / (maxLatitude - minLatitude));
            g2.drawLine(x, y, x, y);
        }
        LOGGER.log(Level.INFO, "Nodes drawn.");
    }

    public static void main(String[] args) throws IOException {
        ShapePoint spoint;
        if (args.length == 0)
            spoint = new ShapePoint();
        else
            spoint = new ShapePoint(args[0]);
        JFrame frame = new JFrame("Draw GeneralPath Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ShapePointViewer(spoint));
        frame.pack();
        frame.setSize(new Dimension(windowWidth, windowHeight));
        frame.setVisible(true);
    }
}