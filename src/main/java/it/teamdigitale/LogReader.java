package it.teamdigitale;


import scala.Tuple2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * C'è stato un errore di formattazione degli Address nella classe ClientEuropeanVATCheck, per cui gli indirizzi risultano tutti monchi
 * Quindi si è preso il log e si sono estratte tutte le informazioni utili dal file
 * "logs/copia_INFO.vatConverter.20180109-155849.log" e salvate nel "file piva_comunitarie.csv"
 */
public class LogReader {
    String header = "pIva, nome, type_piva, indirizzo, cap, citta, provincia, ragioneSociale, url_detail, cciaarea";

    public  static Stream<String> readFromFile(String inputFile) throws FileNotFoundException {

        Scanner scanner = new Scanner(new File(inputFile));
        Stream<String> stream = StreamSupport
                .stream(
                        Spliterators.spliteratorUnknownSize(scanner.useDelimiter("\n"),
                                Spliterator.ORDERED)
                        , false);

        return stream;
    }

    static String exp = "Extracted info for vat (\\w*): <name: (.*?)> <address: (.*?), (\\d*) (.*?) (\\w*)>";

    public static void runForClientEuropeanVatCheck(String inputFile, String outputFile){

        Scanner scanner = null;
        PrintWriter pw = null;

        try {
            scanner = new Scanner(new File(inputFile));
            pw = new PrintWriter(new File(outputFile));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                Pattern p = Pattern.compile(exp);
                Matcher m = p.matcher(line);


                if (m.find()) {
                    String vat = m.group(1).trim();
                    String name = m.group(2).trim();
                    String via = m.group(3).trim();
                    String cap = m.group(4).trim();
                    String citta = m.group(5).trim();
                    String provincia = m.group(6).trim();

                    System.out.println(vat);
                    System.out.println(name);
                    System.out.println(via);
                    System.out.println(cap);
                    System.out.println(citta);
                    System.out.println(provincia);

                    String csv = String.format("%s,%s,%s,%s,%s,%s,%s",vat,"COMUNITARIO",name, via, cap, citta, provincia);

                    pw.println(csv);

                }

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
            pw.close();
        }
    }

    public static void runForClientNotEuropeanVatCheck(String inputFile, String outputFile){

        Scanner scanner = null;
        PrintWriter pw = null;

        try {
            scanner = new Scanner(new File(inputFile));
            pw = new PrintWriter(new File(outputFile));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                Pattern p = Pattern.compile(exp);
                Matcher m = p.matcher(line);


                if (m.find()) {
                    String vat = m.group(1).trim();
                    String name = m.group(2).trim();
                    String via = m.group(3).trim();
                    String cap = m.group(4).trim();
                    String citta = m.group(5).trim();
                    String provincia = m.group(6).trim();

                    System.out.println(vat);
                    System.out.println(name);
                    System.out.println(via);
                    System.out.println(cap);
                    System.out.println(citta);
                    System.out.println(provincia);

                    String csv = String.format("%s,%s,%s,%s,%s,%s,%s",vat,"NON COMUNITARIO",name, via, cap, citta, provincia);

                    pw.println(csv);

                }

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
            pw.close();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {


//        String outputFile = "piva_comunitarie.csv";
//        String inputFile = "logs/copia_INFO.vatConverter.20180109-155849.log";
//
//
//        LogReader.run(inputFile, outputFile);

    }
}
