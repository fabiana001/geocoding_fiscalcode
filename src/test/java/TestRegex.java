import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

    String s = "<script>\n" +
            "\t\t\t\t\t\t\t\t\ticrifCompaniesLocations.push({\n" +
            "\t\t\t\t\t\t\t\t\t\t\"address\" : \"ORTELLA, KM 3 ., COLFELICE, Italy\",\n" +
            "\t\t\t\t\t\t\t\t\t\t\"letter\" : \"A\",\n" +
            "\t\t\t\t\t\t\t\t\t\t\"url\" : \"/it/scheda-azienda/FR145096_SOCIETA'_AMBIENTE_FROSINONE_S.P.A._(COSTITUITA_EX_ART._113_D._LGS._267/2000)\",\n" +
            "\t\t\t\t\t\t\t\t\t\t\"name\" : \"SOCIETA' AMBIENTE FROSINONE S.P.A. (COSTITUITA EX ART. 113 D. LGS. 267\\/2000)\"\n" +
            "\t\t\t\t\t\t\t\t\t});\n" +
            "\t\t\t\t\t\t\t\t\t</script> ";

    String exp = "\"address\"\\s*:\\s*\"(.*?)\"," +
            "\\s*\"letter\".*?,"+
            "\\s*\"url\"\\s*:\\s*\"([[-a-zA-Z0-9+&@#/%?=~_|!:,.;)(\\\\]*']*)\"," +
            "\\s*\"name\"\\s*:\\s*\"([[-a-zA-Z0-9+&@#/%?=~_|!:,.;)(\\\\]*\\s']*)\"" + "\\s*"+ "}\\);";

    Matcher m = Pattern.compile(exp).matcher("");

    public static void main(String[] args) {
        TestRegex tr = new TestRegex();

        if (tr.m.reset(tr.s).find()) {
            //  if(m.find()){
            String address = tr.m.group(1);
            String url2 = tr.m.group(2);
            String name = tr.m.group(3);

            System.out.println(address + " " + url2 + " " + name);
        } else {
            System.out.println("ho un problema");
        }
    }

}
