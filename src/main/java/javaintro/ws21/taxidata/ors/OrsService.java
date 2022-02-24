package javaintro.ws21.taxidata.ors;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.LinkedHashMap;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OrsService use the http://openrouteservice to get a route between two coordinates
 * 
 * <pre>
 * OrsService ors = new OrsService("http://localhost:8080/ors");
 * Route route = ors.request(-73.866234, 40.762602, -73.988361, 40.743242, Preference.FASTEST);
 * </pre>
 *
 */
public class OrsService {
	public enum Preference {
		FASTEST, SHORTEST;
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	private final ObjectMapper jackson = new ObjectMapper();
	private final GeoJSONReader geoJSON = new GeoJSONReader();

	private final HttpClient client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2).build();
	private final URI endpoint;
	
	public OrsService(String endpoint) {
		this.endpoint = URI.create(endpoint + "/v2/directions/driving-car/geojson");
	}


	public Route request(Coordinate start, Coordinate end, Preference preference)
			throws IOException, InterruptedException {
		return request(start.x, start.y, end.x, end.y, preference);
	}

	public Route request(double startLon, double startLat, double endLon, double endLat, Preference preference)
			throws IOException, InterruptedException {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("coordinates", new double[][] { { startLon, startLat }, { endLon, endLat } });
		body.put("geometry_simplify", true);
		body.put("instructions", false);
		body.put("preference", preference.toString());
		return request(jackson.writeValueAsString(body));
	}

	private Route request(String body) throws IOException, InterruptedException {
		// System.out.println(body);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(endpoint)
				.header("Content-Type", "application/json")
				.header("charset", "utf-8")
				.header("Accept", "application/geo+json")
				.POST(BodyPublishers.ofString(body)).build();
		
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

		int statusCode = response.statusCode();

		Route route = new Route(statusCode);
		if (route.isValid()) {
			JsonNode json = jackson.readTree(response.body());
			JsonNode feature = json.get("features").get(0);
			JsonNode properties = feature.get("properties");
			JsonNode summary = properties.get("summary");
			route.setDistance(summary.get("distance").asDouble(-1.0));
			route.setDuration(summary.get("duration").asDouble(-1.0));
			Geometry geometry = geoJSON.read(feature.get("geometry").toString());
			route.setGeometry(geometry);
		}

		return route;
	}

	public static class Route {
		private static final Geometry empty = new GeometryFactory().createLineString();
		private final int statusCode;

		private double duration = -1.0;
		private double distance = -1.0;
		private Geometry geometry = empty;

		public Route(int statusCode) {
			this.statusCode = statusCode;
		}

		public double getDuration() {
			return duration;
		}

		public double getDistance() {
			return distance;
		}

		public Geometry getGeometry() {
			return geometry;
		}

		public boolean isValid() {
			return statusCode == 200;
		}

		public void setGeometry(Geometry geometry) {
			this.geometry = geometry;
		}

		public void setDuration(double duration) {
			this.duration = duration;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}

		@Override
		public String toString() {
			return String.format("Route [statusCode=%s, duration=%s, distance=%s, geometry=%s]", statusCode, duration,
					distance, geometry);
		}
	}
}
