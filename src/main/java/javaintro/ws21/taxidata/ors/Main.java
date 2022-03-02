package javaintro.ws21.taxidata.ors;

import static org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import javax.media.jai.TiledImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.vector.HeatmapProcess;
import org.geotools.process.vector.HeatmapSurface;
import org.geotools.process.vector.VectorHelper;
import org.geotools.process.vector.VectorHelper.ClampToGrid;
import org.geotools.process.vector.VectorToRasterProcess;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import javaintro.ws21.taxidata.ors.OrsService.Preference;
import javaintro.ws21.taxidata.ors.OrsService.Route;

public class Main {

	private static final int HEATMAP_KERNEL = 5;
	private static final int HEATMAP_SIZE_X = 1000;
	private static final int HEATMAP_SIZE_Y = 1000;

	private static final GridCoverageFactory gcf = new GridCoverageFactory();

	public static void main(String[] args) throws IOException, InterruptedException, FactoryException,
			MismatchedDimensionException, TransformException {
		Path dataDir = Path.of("/home/rtroilo/IdeaProjects/IntroJavaWS21Geotools/data/taxi");
		FileDataStore zoneStore = FileDataStoreFinder.getDataStore(dataDir.resolve("zones/taxi_zones.shp").toFile());
		SimpleFeatureSource zoneSource = zoneStore.getFeatureSource();
		CoordinateReferenceSystem zoneCRS = zoneSource.getInfo().getCRS();
		ReferencedEnvelope zoneBounds = zoneSource.getBounds();

		MathTransform transform = CRS.findMathTransform(zoneCRS, WGS84, true);
		ReferencedEnvelope zoneBoundsWGS84 = new ReferencedEnvelope(JTS.transform(zoneBounds, transform), WGS84);
		HeatmapSurface heatmap = new HeatmapSurface(HEATMAP_KERNEL, zoneBoundsWGS84, HEATMAP_SIZE_X, HEATMAP_SIZE_Y);

		OrsService ors = new OrsService("http://129.206.7.141:8080/ors");

		for (var pref : Preference.values()) {

			Route route = ors.request(-73.866234, 40.762602, -73.988361, 40.743242, pref);
			System.out.println(route);
			LineString geometry = (LineString) route.getGeometry();

			var coordinates = geometry.getCoordinates();

			for (var coord : coordinates) {
				heatmap.addPoint(coord.x, coord.y, 1);
			}
		}

		writeTiff(zoneBoundsWGS84, flipXY(heatmap.computeSurface()), "heatmap_pickup_6");

//        HeatmapProcess heatmapProcess;
//        VectorToRasterProcess vectorToRasterProcess;
//
//        GridCoverage2D grid;
	}

	private static void writeTiff(ReferencedEnvelope zoneBoundsWGS84, float[][] data, String url) throws IOException {
		GridCoverage2D gc = gcf.create("name", data, zoneBoundsWGS84);
		File file = new File(url);
		GeoTiffWriter writer = new GeoTiffWriter(file);
		writer.write(gc, null);
		writer.dispose();
	}

	private static float[][] flipXY(float[][] grid) {
		int xsize = grid.length;
		int ysize = grid[0].length;
		float[][] grid2 = new float[ysize][xsize];

		for (int ix = 0; ix < xsize; ++ix) {
			for (int iy = 0; iy < ysize; ++iy) {
				int iy2 = ysize - iy - 1;
				grid2[iy2][ix] = grid[ix][iy];
			}
		}
		return grid2;
	}
}
