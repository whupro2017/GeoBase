package whu.path;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.util.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShapeFileManager {
    protected static final Logger LOGGER = Logging.getLogger(ShapeFileManager.class);

    private static Map<SimpleFeatureSource, FileDataStore> mapSourceStore = new HashMap<>();

    public static SimpleFeatureSource getFeatureSource(String path) throws IOException {
        File file = new File(path);
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        ((ShapefileDataStore) store).setCharset(Charset.forName("GB2312"));
        SimpleFeatureSource source = store.getFeatureSource();
        mapSourceStore.put(source, store);
        LOGGER.log(Level.INFO, "Open " + source.getName().toString());
        return source;
    }

    public static void disposeFeatureSource(SimpleFeatureSource source) {
        mapSourceStore.get(source).dispose();
        mapSourceStore.remove(source);
        LOGGER.log(Level.INFO, "Dispose " + source.getName().toString());
    }
}
