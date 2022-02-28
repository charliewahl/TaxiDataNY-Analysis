import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class pickupDate {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int passengers;                   //kein Zugriff von außen durch "private"
    private final LocalDateTime pickupTime;         //getter und setter nötig
    private final LocalDateTime dropoffTime;
    private final double pickupLongitude;
    private final double pickupLatitude;
    private final double dropoffLongitude;
    private final double dropoffLatitude;

    public pickupDate(int passengers, LocalDateTime pickupTime, LocalDateTime dropoffTime, double pickupLongitude, double pickupLatitude, double dropoffLongitude, double dropoffLatitude) {
        this.passengers = passengers;
        this.pickupTime = pickupTime;
        this.dropoffTime = dropoffTime;
        this.pickupLongitude = pickupLongitude;
        this.pickupLatitude = pickupLatitude;
        this.dropoffLongitude = dropoffLongitude;
        this.dropoffLatitude = dropoffLatitude;
    }

    public pickupDate(String[] row) {
        passengers = Integer.parseInt(row[3]);
        pickupTime = LocalDateTime.parse(row[1], formatter);
        dropoffTime = LocalDateTime.parse(row[2], formatter);
        pickupLongitude = Double.parseDouble(row[5]);
        pickupLatitude = Double.parseDouble(row[6]);
        dropoffLongitude = Double.parseDouble(row[9]);
        dropoffLatitude = Double.parseDouble(row[10]);
        pickupTime.getHour();

    }

    public int getPassengers() {
        return passengers;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public LocalDateTime getDropoffTime() {
        return dropoffTime;
    }

    public double getPickupLongitude() {
        return pickupLongitude;
    }

    public double getPickupLatitude() {
        return pickupLatitude;
    }

    public double getDropoffLongitude() {
        return dropoffLongitude;
    }

    public double getDropoffLatitude() {
        return dropoffLatitude;
    }
}

