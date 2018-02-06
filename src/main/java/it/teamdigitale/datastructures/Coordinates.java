package it.teamdigitale.datastructures;

public class Coordinates {

    Double lat;
    Double lon;

    public Coordinates(Double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public String toString() {
        String res = String.format("%s %s",lat, lon);
        return res;
    }
}
