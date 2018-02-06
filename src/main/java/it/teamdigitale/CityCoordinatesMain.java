package it.teamdigitale;

import com.google.common.collect.Collections2;
import scala.Tuple2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Used to check how many not
 */
public class CityCoordinatesMain {

    static String header = "pIva, nome, type_piva, indirizzo, cap, citta, provincia, ragioneSociale, url_detail, cciaarea";

    public static void fixIssueCommaPIVAComunitarie(String filename, String output ) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(output);
        pw.println(header);

        Scanner s = new Scanner(new File(filename));
        System.out.println("Analizing file "+ filename);
        int counter = 0;

        //piva, name, NON COMUNITARIO, Via,,
        String exp = "(\\d*),COMUNITARIO,([[-a-zA-Z0-9+&@#/%$?=~_|!:,.;)(]*\\s']*),([[-a-zA-Z0-9+&@#/%?=~_|!:,.;)(]*\\s']*),\\s*(\\d{5})\\s*,([[-a-zA-Z0-9+&@#/%?=~_|!:,.;)(]*\\s']*),(\\D+)\n";
        String exp2 = "(\\d*),COMUNITARIO,([[-a-zA-Z0-9+&@#/$%?=~_|!:,.;)(]*\\s']*),\\s*([VIA|CORSO [-a-zA-Z0-9+&@#/%?=~_|!:,.;)(]*\\s']*),,*\\s*(\\d{5})\\s*([[-a-zA-Z0-9+&@#/%?=~_|!:,.;)(]*\\s']*),(\\D+)\n";
        Matcher m2 = Pattern.compile(exp2).matcher("");
        Matcher m = Pattern.compile(exp).matcher("");

        int count = 0;

        while (s.hasNextLine()) {
            String line = s.nextLine() +"\n";

            if (m.reset(line).find()){
                String piva = m.group(1);
                String name = m.group(2).replaceAll(",", " ");
                String via = m.group(3).replaceAll(",", " ");
                String cap = m.group(4);
                String citta = m.group(5);
                String pv = m.group(6);

                String newStr = String.format("%s,%s,%s,%s,%s,%s,%s,Nan,Nan,Nan", piva, name, "COMUNITARIO", via, cap,citta, pv);
                //String res = m.replaceFirst(  );

                if(newStr.split(",").length != 10){
                    System.out.println("ERRORE size:"+line.split(",").length+"\n"+ line);
                }else{
                    pw.println(newStr);
                }

            } else if(m2.reset(line).find()){
                if (m.reset(line).find()) {
                    String piva = m.group(1);
                    String name = m.group(2).replaceAll(",", " ");
                    String via = m.group(3).replaceAll(",", " ");
                    String cap = m.group(4);
                    String citta = m.group(5);
                    String pv = m.group(6);

                    String newStr = String.format("%s,%s,%s,%s,%s,%s,%s,Nan,Nan,Nan", piva, name, "COMUNITARIO", via, cap, citta, pv);
                    //String res = m.replaceFirst(  );

                    if (newStr.split(",").length != 10) {
                        System.out.println("ERRORE size:" + line.split(",").length + "\n" + line);
                    } else {
                        pw.println(newStr);
                    }
                }
            } else {
                count ++;
                System.out.print(line);
            }
        }

        System.out.println(count);
        pw.close();

    }


//    public static void fixIssueComma(String filename, String output ) throws FileNotFoundException {
//        PrintWriter pw = new PrintWriter(output);
//
//        Scanner s = new Scanner(new File(filename));
//        System.out.println("Analizing file "+ filename);
//        int counter = 0;
//
//        //piva, name, NON COMUNITARIO, Via,,
//        String exp = "(\\d*),([[-a-zA-Z0-9+&@#/%?=~_|!:,.;)(]*\\s']*),NON COMUNITARIO,(.*?),,";
//        Matcher m = Pattern.compile(exp).matcher("");
//
//        String exp2 = "/it/scheda-azienda/([[-a-zA-Z0-9+&@#/%?=~_|!:,.;)(]*\\s']*),";
//        Matcher m2 = Pattern.compile(exp2).matcher("");
//
//        while (s.hasNextLine()) {
//            String line = s.nextLine();
//            String res1 = "";
//            if (m.reset(line).find()){
//                String vat = m.group(1);
//                String name = m.group(2).replaceAll(","," ");
//                String via = m.group(3).replaceAll(","," ");
//                //String res = line.replaceAll(exp, " ");
//                res1 = m.replaceFirst(vat + "," + name+",NON COMUNITARIO," + via + ",,");
//            }
//
//            String res2 = null;
//            if (m2.reset(res1).find()){
//                String url = m2.group(1).replaceAll(",","comma");;
//                res2 = m2.replaceFirst("/it/scheda-azienda/" + url + ",");
//            }
//
//            if(res2.split(",").length != 10){
//                    System.out.println("ERRORE size:"+res2.split(",").length+"\n"+ res2);
//            }else{
//                pw.println(res2);
//            }
//
//        }
//        pw.close();
//        System.out.println(counter);
//    }
//

    public  static Stream<String> readFromFile(String inputFile) throws FileNotFoundException {

        Scanner scanner = new Scanner(new File(inputFile));
        Stream<String> stream = StreamSupport
                .stream(
                        Spliterators.spliteratorUnknownSize(scanner.useDelimiter("\n"),
                                Spliterator.ORDERED)
                        , false);

        return stream;
    }


    public static Tuple2<Boolean, String> checkCities(String s){

        String exp = ",,([^0-9]*),(\\D{2}),";

        Matcher m = Pattern.compile(exp).matcher("");

        if(m.reset(s).find()){
            String citta = m.group(1);
            String provincia = m.group(2);
            //System.out.println(citta + " " + provincia);
            return new Tuple2<Boolean, String>(true, s);
        }
        else {
            String[] tokens = s.split(",");
            System.out.println(tokens[0] + " "+tokens[5] + " " +tokens[6]);
            return new Tuple2<Boolean, String>(false, s);
        }

    }


    public static void main(String[] args) throws FileNotFoundException {
     //fixIssueComma("/Users/fabiana/git/wsdlclient/copia_piva_non_comunitarie_correct2.csv", "/Users/fabiana/git/wsdlclient/copia_piva_non_comunitarie_correct.csv");
     //fixIssueCommaPIVAComunitarie("./piva_comunitarie.csv", "./piva_comunitarie2.csv");

// Alcune ennuple del file nn comunitario restituito ha delle citta sbagliate questo codice le filtra
        String ng = "RSANDR77E01B354P,LABORATORIO DOLCIARIO DI ANDREA RAIS,C.F.,MONASTIR KM. 4,,800 SNC,CA,RAIS,/it/scheda-azienda/CA271860_LABORATORIO_DOLCIARIO_DI_ANDREA_RAIS,CA271860\n";
        String gg ="MROMRZ64T08D612L,MORI MAURIZIO,C.F.,BASSA 2H,,FIRENZE,FI,MAURIZIO,/it/scheda-azienda/FI492754_MORI_MAURIZIO,FI492754\n";


        Stream<Tuple2<Boolean, String>> r = readFromFile("generated_data/piva_non_comunitarie_faster1515688249205.csv").map(x -> checkCities(x));

        PrintWriter pw = new PrintWriter("generated_data/piva_non_comunitarie_filtered1515688249205.csv");
        r.filter(x -> x._1).forEach(x -> {
            pw.println(x._2);
        });
        //int res2 = r.filter(x-> !x._1).collect((Collectors.toList())).size();

       pw.close();
    }
}
