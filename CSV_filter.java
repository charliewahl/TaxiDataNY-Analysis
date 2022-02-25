import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CSV_filter {

    static Map<String, String> map = new HashMap<String, String>();

    public static void main(String[] args) throws IOException {

        Path DataDir = Path.of("src", "main","resources","Taxidatensatz.csv");
        String line = "";
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(String.valueOf(DataDir)))){

            while ((line = br.readLine()) !=null) {

                String[] Time = line.split(csvSplitBy);
                for (int i = 0; i < Time.length ; i++) {
                    map.put(Time[1], Time [2]);


                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String,String> entry : map.entrySet()){
            String PickupTime = entry.getKey();
            String DropoffTime = entry.getValue();
            System.out.println("Time [Pickuptime= " + PickupTime + " , DropoffTime=" + DropoffTime + "]");
        }




    }
}
