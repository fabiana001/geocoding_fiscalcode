package it.teamdigitale.clients;

import eu.europa.ec.taxud.vies.services.checkvat.CheckVatPortType;
import eu.europa.ec.taxud.vies.services.checkvat.CheckVatService;
import it.teamdigitale.datastructures.Address;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientEuropeanVATCheck {

    CheckVatService service = new CheckVatService();
    CheckVatPortType port = service.getCheckVatPort();
    Holder<String> itCountryCode = new Holder<String>("IT");
    private static final Logger logger = LogManager.getLogger(ClientEuropeanVATCheck.class.getName());

    public Optional<Address> getAddress(Holder<String> vat) {

        Holder<XMLGregorianCalendar> requestDate = new Holder<XMLGregorianCalendar>();
        Holder<Boolean> valid = new Holder<Boolean>();
        Holder<String> name = new Holder<String>();
        Holder<String> address = new Holder<String>();

        try {
            port.checkVat(itCountryCode, vat, requestDate, valid, name, address);

            if (!valid.value) {
                logger.error("VAT number {} is invalid.", vat.value);

            } else {
                // Print time of request and name and address based on given
                // country code and VAT number

                String reformattedString = address.value.substring(0, address.value.length() - 1).replace("\n", ", ");
                logger.info("Extracted info for vat {}: <name: {}> <address: {}>", vat.value, name.value,reformattedString );
                //logger.info("Time of request: " + requestDate.value);
                //logger.info("Name: " + name.value);
                //logger.info("Address: " + address.value);

                String exp = "(.*?) \n(\\d*) (.*?) (\\w*)";

                Pattern p = Pattern.compile(exp);
                Matcher m = p.matcher(address.value);
                if (m.find()) {

                    String via = m.group(1).trim();
                    String cap = m.group(2).trim();
                    String citta = m.group(3).trim();
                    String provincia = m.group(4).trim();

                    Address res = new Address(via, cap, citta, provincia);

                    return Optional.ofNullable(res);

                } else {
                    logger.error("VAT {}: Unable to extract address informarion from {}", vat.value,reformattedString);
                    return Optional.empty();
                }

            }

        } catch (SOAPFaultException ex) {
            logger.error("Country code {} is invalid.", itCountryCode.value);
        }

        return Optional.empty();
    }

    public static void main(String[] args) {
        //Holder<String> vatNumber = new Holder<String>("00566140265");
        Holder<String> vatNumber = new Holder<String>("03823100718");
        ClientEuropeanVATCheck cvc = new ClientEuropeanVATCheck();
        Optional<Address> res = cvc.getAddress(vatNumber);

        System.out.println(res);
    }



}
