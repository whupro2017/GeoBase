package whu.path;

import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ShapeFileInformation {
    private String shpPath = "./resources/shapes/sz_shp/乡镇村道_polyline.shp";
    private String dbfPath = "./resources/shapes/sz_shp/乡镇村道_polyline.dbf";

    public ShapeFileInformation() {
    }

    public ShapeFileInformation(String shpPath, String dbfPath) {
        this.shpPath = shpPath;
        this.dbfPath = dbfPath;
    }

    public String info() {
        return shpPath + "<->" + dbfPath;
    }

    public void readFeatures() throws IOException {
        File file = new File(shpPath);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());
        map.put("charset", "GB2312");

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                System.out.print(feature.getID());
                System.out.print(": ");
                System.out.println(feature.getDefaultGeometryProperty().getValue());
            }
        }
        dataStore.dispose();
    }

    public void readDBF() throws IOException {
        SimpleFeatureSource source = ShapeFileManager.getFeatureSource(shpPath);
        SimpleFeatureType schema = source.getSchema();

        Query query = new Query(schema.getTypeName());
        query.setMaxFeatures(source.getFeatures().size());

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(query);
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                System.out.println(feature.getID() + ": ");
                for (Property attribute : feature.getProperties()) {
                    System.out.println("\t" + attribute.getName() + ":" + attribute.getValue());
                }
            }
        }
        ShapeFileManager.disposeFeatureSource(source);
    }

    public void infoDBF() throws IOException {
        FileInputStream fis = new FileInputStream(dbfPath);
        DbaseFileReader dbfReader = new DbaseFileReader(fis.getChannel(), false, Charset.forName("GB2312"));
        int fid = 0;
        while (dbfReader.hasNext()) {
            final Object[] fields = dbfReader.readEntry();
            int oid = 0;
            for (Object obj : fields) {
                System.out.println(fid + " DBF field " + oid++ + " value is: " + (String) obj);
            }
            fid++;
        }

        dbfReader.close();
        fis.close();
    }
}
