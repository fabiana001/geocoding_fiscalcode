package it.teamdigitale.aggregators;

import com.google.common.collect.Lists;
import it.teamdigitale.VatAnalyzer;
import it.teamdigitale.datastructures.PMI;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import scala.Tuple2;
import scala.Tuple3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class City{
    String name;
    String capital;
    double lat;
    double lng;
    String istatCode;

    City(String name, String capital, String istatCode, double lat, double lng){
        this.name = name;
        this.capital = capital;
        this.lat = lat;
        this.lng = lng;
        this.istatCode = istatCode;

    }

    public boolean equals(Object o){
        if(! (o instanceof City)) return false; //a City can't be equal to a non-city

        City c = (City) o;

        return (name == null && c.name == null && capital == null && c.capital == null) || (name.equals(c.name) && capital.equals(c.capital));
    }

}

class ComparatorTuple2 implements Comparable<ComparatorTuple2>{
    int distance;
    City city;
    ComparatorTuple2(int distance, City city){
        this.distance = distance;
        this.city = city;
    }

    @Override
    public int compareTo(@NotNull ComparatorTuple2 o) {
        return Integer.compare(distance, o.distance);
    }
}
public class CoordiantesAggregator {

    File file = new File(VatAnalyzer.class.getClassLoader().getResource("comuniitaliani24102017.csv").getFile());
    String separator = "#";
    LevenshteinDistance ld = new LevenshteinDistance();

    private final Logger logger = LogManager.getLogger(this.getClass().getName());


    Map<String, City> city_capital_map = new HashMap<>();
    Map<String, List<City>> city_map = new HashMap<>();
    Map<String, List<City>> capital_map = new HashMap<>();


    private void init() throws FileNotFoundException {

        Scanner scanner = new Scanner(file);


        //remove header
        scanner.nextLine();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] tokens = line.split(";");

            String cityName = tokens[0];
            String istatCode = tokens[1];
            String capital = tokens[3];
            double lat = Double.valueOf(tokens[tokens.length - 1].replaceAll(",", "."));
            double lng = Double.valueOf(tokens[tokens.length - 2].replaceAll(",", "."));

            City c = new City(cityName, capital, istatCode, lat, lng);
            String key_city_capital_map = c.name.toLowerCase() + separator + c.capital.toLowerCase();
            String key_city_map = c.name.toLowerCase();
            String key_capital_map = c.capital.trim().toLowerCase();

            city_capital_map.put(key_city_capital_map, c);

            if(city_map.containsKey(key_city_map)){
                List<City> l = city_map.get(key_city_map);
                l.add(c);
                city_map.put(key_city_map, l);
            } else{
                List<City> l =new ArrayList<>();
                l.add(c);
                city_map.put(key_city_map, l);
            }

            if(capital_map.containsKey(key_capital_map)){
                List<City> l = capital_map.get(key_capital_map);
                l.add(c);
                capital_map.put(key_capital_map, l);
            } else{
                List<City> l =new ArrayList<>();
                l.add(c);
                capital_map.put(key_capital_map, l);
            }

        }
        System.out.println("city_map size " + city_map.keySet().size());
        System.out.println("city_capital_map size " + city_capital_map.keySet().size());
        scanner.close();
    }

    private Optional<City> search(String city, String capital){

        String key_city_capital= city.trim().toLowerCase() + separator + capital.trim().toLowerCase();
        String key_city = city.trim().toLowerCase();
        String key_capital = capital.trim().toLowerCase();

        if(city_capital_map.containsKey(key_city_capital)) {
            return Optional.of(city_capital_map.get(key_city_capital));
        } else if (city_map.containsKey(key_city) && city_map.get(key_city).size()==1){
           return Optional.of(city_map.get(key_city).get(0));
        } else if (capital_map.containsKey(key_capital)){
            List<City> cities = capital_map.get(key_capital);
            List<ComparatorTuple2> res = cities.stream().map(c -> new ComparatorTuple2(ld.apply(city.toLowerCase(), c.name.toLowerCase()), c)).sorted().collect(Collectors.toList());

            City bestcity = res.get(0).city;
            int score = res.get(0).distance;
            float bigger = Math.max(city.length(), bestcity.name.length());
            double pct = (bigger - score) / bigger;

            if(pct > 0.40) {
                //logger.info("The most similar to "+ city +" - " + capital+  " is the city " + res.get(0).city.name +" - "  + res.get(0).city.capital);
                return Optional.of(bestcity);
            }
            else{
                logger.error("Score:"+ pct +"The most similar to "+ city +" - " + capital+  " is the city " + res.get(0).city.name +" - "  + res.get(0).city.capital);
                return Optional.of(bestcity);
            }


        } else {
            logger.error("city "+key_city+" does not exists in the dictionary");
            return Optional.empty();
        }

    }

    private void enrichFile(String file, boolean hasHeader, String output ) throws FileNotFoundException {

        PrintWriter pw = new PrintWriter(output);
        Scanner scanner = new Scanner(new File(file));
        String header = "";


        //remove header
        if (hasHeader) {
            header = scanner.nextLine()+ ",ISTAT,lat,lng";
            pw.println(header);
        }

        Stream<String> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(scanner.useDelimiter("\n"), Spliterator.ORDERED), false)
                .map(line -> {
                    String[] tokens = line.split(",");
                    String vat = tokens[0];
                    String name = tokens[1];
                    String type = tokens[2];
                    String address = tokens[3];
                    String cap = tokens[4];
                    String city = tokens[5];
                    String capital = tokens[6];
                    String typeVat = tokens[7];
                    String url = tokens[8];
                    String cciaarea = tokens[9];

                    Optional<City> c = search(city, capital);

                    return new Tuple2<>(line, c);
                })
                //.flatMap(o -> o._2.isPresent() ? Stream.of(new Tuple2<String, City>(o._1, o._2.get())) : Stream.empty())
                //.map(line_city -> String.format("%s,%s,%s,%s", line_city._1, line_city._2.istatCode, line_city._2.lat, line_city._2.lng));
                .map(line_city ->{
                    if(line_city._2.isPresent()){
                        City c = line_city._2.get();
                        return String.format("%s,%s,%s,%s", line_city._1, c.istatCode, c.lat, c.lng);
                    } else {
                        return line_city._1 + "Nan,Nan,Nan";
                    }
                });
//        System.out.println(stream.collect(Collectors.toList()).size());

        stream.forEach(s -> {
            pw.println(s);
            pw.flush();

        });
    }

    public void run(String inputfile, boolean hasHeader, String output ) throws FileNotFoundException {
        init();
        enrichFile(inputfile,hasHeader,output);
    }


    public static void main(String[] args) throws FileNotFoundException {

        CoordiantesAggregator ca = new CoordiantesAggregator();

        ca.run("generated_data/piva_non_comunitarie_filtered1515688249205.csv", true, "./pluto.txt");
//
//        String s1 = "POSTAL .BURGSTALL.";
//        String s2 = "reggio emilia";
//        LevenshteinDistance ld = new LevenshteinDistance();
//        int d = ld.apply(s1, s2);
//        //float d = levenshteinDistance(s1, s2);
//        float bigger = Math.max(s1.length(), s2.length());
//        double pct = (bigger - d) / bigger;
//        System.out.println(pct);

    }


}
