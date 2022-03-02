package javaintro.ws21.taxidata.ors;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;

public class CsvFilter {

	public static void main(String[] args) throws IOException {

		Path csvPath = Path.of("yellow_tripdata_2016-04.csv");
		String line = "";
		String csvSplitBy = ",";

		int[] pickupPagengersPerHour = new int[24];
		Map<Integer, Integer> histoPassengerRides = new TreeMap<>();

		LocalDateTime start = LocalDateTime.of(2016, 04, 9, 23, 59, 59);
		LocalDateTime end = LocalDateTime.of(2016, 04, 11, 0, 0);

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
				}

				int pickupHour = row.getPickupTime().getHour();
				if (pickupHour >= 5 && pickupHour <= 18) {
					// what ever
				}
				pickupPagengersPerHour[pickupHour] += row.getPassengers();
				Integer rides = histoPassengerRides.getOrDefault(row.getPassengers(), 0);
				histoPassengerRides.put(row.getPassengers(), rides + 1);
			}
		}

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
