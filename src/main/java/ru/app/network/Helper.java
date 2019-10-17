package ru.app.network;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.app.main.Settings;
import ru.app.util.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Helper {

    public static Payment createPayment(String response) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource src = new InputSource();
        src.setCharacterStream(new StringReader(response));

        Document doc = builder.parse(src);
        String cmd1 = doc.getElementsByTagName("command").item(0).getTextContent();
        String cmd2 = doc.getElementsByTagName("command_id").item(0).getTextContent();

        String[] data = cmd1.split("\\*");

        Payment payment = new Payment();
        payment.setId(Long.valueOf(cmd2));
        payment.setNumber(data[1]);
        payment.setSum(Integer.valueOf(data[2]));
        payment.setText(data[3].substring(0, data[3].length() - 1)); // w/o # symbol

        return payment;
    }

    public static Map<String, String> loadProp(File payFile) throws IOException {
        Map<String, String> result = new HashMap<>();
        Properties p = new Properties();
        p.load(new FileReader(payFile));
        for (String key : p.stringPropertyNames()) {
            result.put(key, p.getProperty(key));
        }
        return result;
    }

    public static void saveProp(Map<String, String> prms, File payFile) {
        try {
            Properties prop = new Properties();
            for (Map.Entry<String, String> e : prms.entrySet()) {
                prop.setProperty(e.getKey(), e.getValue());
            }
            OutputStream os = new FileOutputStream(payFile);
            prop.store(os, null);
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveFile(Payment payment, Status status) throws IOException {
        Logger.console("Save file operation started : " + payment + "\t" + status);
        String paymentsDir = Settings.paymentsDir + "payment";
        String successDir = Settings.paymentsDir + "success" + Settings.separator;
        String errorDir = Settings.paymentsDir + "error" + Settings.separator;

        long id = payment.getId();
        Path payFile = Paths.get(paymentsDir + "payment");
        if (!Files.exists(payFile)) {
            Logger.console("PAYMENT FILE NOT EXISTS!");
            return;
        }
        Path target = Paths.get((status == Status.SUCCESS ? successDir : errorDir) + "payment_" + id);
        Files.copy(payFile, target);
        Files.delete(payFile);
        Logger.console("payment file successfully copied and deleted");
    }

    /**
     *
     *  String yes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
     *                 "<response>\n" +
     *                 "    <command>*9285685445*10*text#</command>\n" +
     *                 "    <command_id>165679</command_id>\n" +
     *                 "    <command_type>ussd</command_type>\n" +
     *                 "    <command_wait_incoming_sms>true</command_wait_incoming_sms>\n" +
     *                 "    <sign>29d25982a4dc5b8361c578ad78fbf749</sign>\n" +
     *                 "</response>";
     */
}