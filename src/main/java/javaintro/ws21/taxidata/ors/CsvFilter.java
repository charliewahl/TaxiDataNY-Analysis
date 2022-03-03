package javaintro.ws21.taxidata.ors;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.vector.HeatmapSurface;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class CsvFilter {

	private static final GridCoverageFactory gcf = new GridCoverageFactory();

	static Map<String, String> map = new HashMap<String,String>();


	public static void main(String[] args) throws IOException, FactoryException, TransformException {

		Path csvPath = Path.of("yellow_tripdata_2016-04.csv");
		String line = "";
		String csvSplitBy = ",";

		FileDataStore store = FileDataStoreFinder.getDataStore(csvPath.resolve("C:\\Users\\charl\\OneDrive\\Dokumente\\Uni\\Geo\\Java\\taxi_zones\\taxi_zones.shp").toFile());
		SimpleFeatureSource zonesSource = store.getFeatureSource();       //"store" wie Datenbankverbindung
		ReferencedEnvelope zonesBounds = zonesSource.getBounds();
		CoordinateReferenceSystem wgs84 = CRS.decode("epsg:4326");
		MathTransform transform = CRS.findMathTransform(zonesBounds.getCoordinateReferenceSystem(), wgs84, true);  //transformation

		zonesBounds = new ReferencedEnvelope(JTS.transform(zonesBounds, transform), wgs84);

		HeatmapSurface heatmap = new HeatmapSurface(10, zonesBounds, 1000, 1000);



		int[] pickupPagengersPerHour = new int[24];
		Map<Integer, Integer> histoPassengerRides = new TreeMap<>();
		Map<String, String> Tripdata = new HashMap<String, String>();


		LocalDateTime start = LocalDateTime.of(2016, 04, 4, 14, 36, 32);
		LocalDateTime end = LocalDateTime.of(2016, 04, 4, 15, 14, 00);


		try (BufferedReader br = new BufferedReader(new FileReader(csvPath.toFile()))) {
			while ((line = br.readLine()) != null) {
				String[] csv = line.split(csvSplitBy);
				TaxiRecord row;
				try {
					row = new TaxiRecord(csv);
				} catch (Exception e) {
					continue;
				}



				if (row.getPickupTime().isAfter(start) && row.getPickupTime().isBefore(end)) {
					// do something here
					System.out.println("testzeitraum");
					System.out.println("Longitude: " + row.getPickupLongitude() + "Latitude: " +  row.getPickupLatitude());

					double pickupLatitude = row.getPickupLatitude();
					double pickupLongitude = row.getPickupLongitude();
					double dropoffLatitude = row.getDropoffLatitude();
					double dropoffLongitude = row.getDropoffLongitude();
					heatmap.addPoint(pickupLatitude,pickupLongitude,1);

					/*Tripdata.put(csv[5], csv[6])
					for (Map.Entry<String, String> entry : Tripdata.entrySet()) {
						String PickupLongitude = entry.getKey();
						String DropoffLatitude = entry.getValue();
						System.out.println("Time [PiLongitude= " + PickupLongitude + " , Dropofflatitude=" + DropoffLatitude + "]"); */
					}


					int pickupHour = row.getPickupTime().getHour();
					if (pickupHour >= 5 && pickupHour <= 18) {

					}
					pickupPagengersPerHour[pickupHour] += row.getPassengers();
					Integer rides = histoPassengerRides.getOrDefault(row.getPassengers(), 0);
					histoPassengerRides.put(row.getPassengers(), rides + 1);
				}
			}
			float[][] data = heatmap.computeSurface();
			GridCoverage2D gc = gcf.create("name", data, zonesBounds);
			File file = new File("example.tiff");
			GeoTiffWriter writer = new GeoTiffWriter(file);
			writer.write(gc, null);
			writer.dispose();

			System.out.println("histo passengers:");
			System.out.println("passengers,rides");
			for (Map.Entry<Integer, Integer> entry : histoPassengerRides.entrySet()) {
				System.out.printf("%d,%d%n", entry.getKey(), entry.getValue().intValue());
			}
			System.out.println();
			System.out.println("aggregate pickup passengers per hour");
			System.out.println("hour,passengers");
			for (int h = 0; h < pickupPagengersPerHour.length; h++) {
				System.out.printf("%2d,%d%n", h, pickupPagengersPerHour[h]);
			}


		}
	}

