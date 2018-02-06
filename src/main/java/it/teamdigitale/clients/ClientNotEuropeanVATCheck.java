package it.teamdigitale.clients;

import it.teamdigitale.datastructures.Address;
import it.teamdigitale.datastructures.PMI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import scala.Tuple3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class ClientNotEuropeanVATCheck {

    String serviceVAT = "https://www.icribis.com/it/ricerca-azienda?country=IT&company-search-by=VAT&search=";
    String serviceFiscal = "https://www.icribis.com/it/ricerca-azienda?country=IT&company-search-by=Fiscal&search=";

    private final Logger logger = LogManager.getLogger(this.getClass().getName());
    String exp = "\"address\"\\s*:\\s*\"(.*?)\"," +
            "\\s*\"letter\".*?,"+
            "\\s*\"url\"\\s*:\\s*\"([[-a-zA-Z0-9+&@#/%?=~_|!:,.;)(\\\\]*']*)\"," +
            "\\s*\"name\"\\s*:\\s*\"([[-a-zA-Z0-9+&@#/%?=~_|!:,.;)(\\\\]*\\s']*)\"" + "\\s*"+ "}\\);";

    Matcher m = Pattern.compile(exp).matcher("");


    public static void main(String[] args) throws IOException, InterruptedException {


       ClientNotEuropeanVATCheck p = new ClientNotEuropeanVATCheck();
       Optional<PMI> pmi = p.run("BLDGNN61P21C469M");


        String csv = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",pmi.get().vat, pmi.get().name, "CF", pmi.get().address.via, pmi.get().address.cap, pmi.get().address.citta, pmi.get().address.provincia, pmi.get().type, pmi.get().url, pmi.get().address.cciaarea);
        System.out.println(csv);
        //
//
//        //leggi file contenente tutte le p.iva gia analizzate
//        String outputFile = "piva_non_comunitarie_faster"+ System.currentTimeMillis() +".csv";
//        String inputFile1 = "piva_comunitarie.csv";
//        String inputFile2 = "MappingRagioneSocialeIvaCodFisc.csv";
//
//        PrintWriter pw = new PrintWriter(new File(outputFile));
//
//        Set<String> piva_comunitarie = new HashSet<>();
//        Set<String> piva_tot = new HashSet<>();
//
//        Scanner scanner = new Scanner(new File(inputFile1));
//        while (scanner.hasNextLine()) {
//            String line = scanner.nextLine();
//            piva_comunitarie.add(line.split(",")[0]);
//
//        }
//        //leggi i file contenente tutte le p.iva
//        Scanner scanner2 = new Scanner(new File(inputFile2));
//        while (scanner2.hasNextLine()) {
//            String line = scanner2.nextLine();
//            piva_tot.add(line.split("\t")[2]);
//
//        }
//        piva_tot.removeAll(piva_comunitarie);
//        Stream<String> piva_non_comunitarie = piva_tot.stream();
//
//        //Stream<Tuple2<String, PMI>> res = vats.map(x -> new Tuple2<String, Optional<PMI>>(x, p.run(x))).flatMap(o -> o._2.isPresent() ? Stream.of(new Tuple2<>(o._1, o._2.get())) : Stream.empty());
//        Stream<PMI> res = piva_non_comunitarie.map(x -> p.run(x)).flatMap(o -> o.isPresent() ? Stream.of( o.get()) : Stream.empty());
//
//
//
//        res.forEach(x -> {
//            String cf_or_vat;
//
//            if(isCf(x.vat))
//                cf_or_vat = "C.F.";
//            else
//                cf_or_vat = "NON COMUNITARIO";
//
//            String csv = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",x.vat, x.name, cf_or_vat, x.address.via, x.address.cap, x.address.citta, x.address.provincia, x.type, x.url, x.address.cciaarea);
//            pw.println(csv);
//            pw.flush();
//        });


    }

    private static boolean isCf(String str){
        String exp = "\\D+\\d+";
        Pattern p = Pattern.compile(exp);
        Matcher m = p.matcher(str);

        return m.find();
    }

    public Optional<PMI> run(String vat) {

        String url;
        if(isCf(vat))
            url = serviceFiscal;
        else
            url = serviceVAT;

        try {
            Long startTs = System.currentTimeMillis();
            String html = connect(vat, url);
            Long endTs = System.currentTimeMillis() - startTs;
            logger.info("Total time for connection {} millis", endTs);

            Long startTs2 = System.currentTimeMillis();
            Tuple3<String, String, String> rowData = extractWithRegex(html.trim());
            PMI pmi = getPMI(rowData);
            pmi.setVat(vat);

            Long endTs2 = System.currentTimeMillis() - startTs2;
            logger.info("Total time for extraction {} millis", endTs2);
            logger.info("Address {} correctly extract for the vat {}", pmi, vat);

            return Optional.of(pmi);
        } catch (Exception e) {
            logger.error("Unable to analyzing vat {} due error: {} ", vat, e.getMessage());
        }

        return Optional.empty();

    }

    /**
     *
     * @param rowdata, where rowdata._1 is the address and rowdata._2 is the url of detail page.
     * @return
     */
    private PMI getPMI(Tuple3<String, String, String> rowdata) {
        String tokenUrl = rowdata._2().trim().split("/")[3];
        String[] tokens_ = tokenUrl.split("_");

        String CCIAAREA = tokens_[0];
        String ragioneSociale = tokens_[tokens_.length -1];

        String[] tokensAddress = rowdata._1().trim().split(",");
        String expProvincia = "(\\D+)\\d+";

        Pattern p = Pattern.compile(expProvincia);
        Matcher m = p.matcher(CCIAAREA);
        String provincia = null;

        if (m.find()) {
            provincia = m.group(1);
        } else{
            provincia = "";
        }

        Address address = new Address(removeComma(tokensAddress[0]), "", removeComma(tokensAddress[1]), provincia, CCIAAREA);
        PMI pmi = new PMI("", removeComma(rowdata._3()), rowdata._2().replaceAll(",","CommA"), removeComma(ragioneSociale), address);
        return pmi;
    }

    private String removeComma(String s ){
        return s.replaceAll(","," ");
    }

    /**
     *
     * @param htmlScript
     * @return a tuple <address, relativeUrl, name azienda>
     */
    private Tuple3<String, String, String> extractWithRegex(String htmlScript) throws RuntimeException{

        //Matcher m = p.matcher(htmlScript);
        if (m.reset(htmlScript).find()) {
            //  if(m.find()){
            String address = m.group(1);
            String url2 = m.group(2);
            String name = m.group(3);

            return new Tuple3<>(address, url2, name);

        }else{
            throw new RuntimeException("Unable find match with the regular expression: html:" + htmlScript);
        }
    }

    private String connect(String vat, String service) {

        String url = service + vat;
        Connection.Response response = null;
        try {
            response = Jsoup.connect(url).followRedirects(true).execute();

            Elements scriptWithData = response.parse().select("#searchResultsList script");
            String html = scriptWithData.toString();

            return html;
        } catch (org.jsoup.HttpStatusException e){
            if(response.statusCode() == 429){
                Map<String, String> headers = response.headers();
                logger.info("HTTP status code 429. Printing headers: ");
                headers.keySet().forEach(x -> logger.info(x + "\t" + headers.get(x)));

            }
        } catch (IOException e) {
            logger.error("Unable to extract data for vat: {}", vat);
            e.printStackTrace();
        } catch (RuntimeException e){
            logger.error("Unable to extract data for vat: {} due parsing exception", vat);
            //e.printStackTrace();
        }


        return "";
    }



}
