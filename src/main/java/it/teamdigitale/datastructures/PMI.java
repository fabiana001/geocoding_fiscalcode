package it.teamdigitale.datastructures;

public class PMI {
    public String name;
    public Address address;
    public String vat;
    public String url;
    public String type;

    public PMI(String vat, String name, String url, String type, Address address) {
        this.vat = vat;
        this.address = address;
        this.name = name;
        this.url = url;
        this.type = type;
    }

    public String toString() {
        return String.format("%s %s %s %s", vat, name, url, address.toString());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }
}
