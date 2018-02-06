package it.teamdigitale;

import it.teamdigitale.clients.ClientEuropeanVATCheck;
import it.teamdigitale.datastructures.Address;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import javax.xml.ws.Holder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;


public class VatAnalyzer {

    private static final Logger logger = LogManager.getLogger(VatAnalyzer.class.getName());

//TODO rimuovere mapdb, non serve

    DB dbVatAddress = DBMaker
            .fileDB("vat_address")
            .fileMmapEnable()
            .make();
    ConcurrentMap<String,String> mapVatAddress = dbVatAddress
            .hashMap("mapVatAddress", Serializer.STRING, Serializer.STRING)
            .createOrOpen();


    DB dbAddressCoordinates = DBMaker
            .fileDB("address_coordinates")
            .fileMmapEnable()
            .make();
    ConcurrentMap<String,String> mapAddressCoordinate = dbAddressCoordinates
            .hashMap("mapAddressCoordinate", Serializer.STRING, Serializer.STRING)
            .createOrOpen();

    ClientEuropeanVATCheck cvc = new ClientEuropeanVATCheck();


    public void run() {

        Set<String> set = VatAnalyzer.getVatFromTSV();
        PrintWriter pwAddress = null;
        PrintWriter pwCoordinates = null;

        try {

            pwAddress = new PrintWriter(new FileOutputStream("./vatAddress.txt"));
            pwCoordinates = new PrintWriter(new FileOutputStream("./addressLocation.txt"));

            for(String vat: set){

                logger.info("Analyzing vat {}", vat);


                Holder<String> holderVat = new  Holder<String>(vat);
                Optional<Address> ad = cvc.getAddress(holderVat);
                //Optional<Coordinates> c = ad.flatMap(x -> ggc.getCoordinates(x));

                if(ad.isPresent()){
                    pwAddress.println(vat + "," + ad.get().toString());
                    pwAddress.flush();
                    mapVatAddress.put(vat, ad.get().toString());
                }

//                if(c.isPresent()){
//                    pwCoordinates.println(vat + "," + c.get().toString());
//                    pwCoordinates.flush();
//                    mapAddressCoordinate.put(vat, ad.get().toString());
//                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            pwAddress.close();
            pwCoordinates.close();
            dbAddressCoordinates.close();
            dbVatAddress.close();
        }
    }

    public static Set<String> getVatFromTSV() {
        File file = new File(VatAnalyzer.class.getClassLoader().getResource("MappingRagioneSocialeIvaCodFisc.csv").getFile());
        Scanner scanner = null;
        Set<String> set = new HashSet<>();
        try {
            scanner = new Scanner(file);

            //remove header
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split("\t");
                String vat = tokens[2].trim();
                set.add(vat);

            }
            scanner.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }


        return set;

    }

    public static void main(String[] args) throws FileNotFoundException {

        VatAnalyzer va = new VatAnalyzer();
        va.run();

    }
}
