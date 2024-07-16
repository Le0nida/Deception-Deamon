package cybersec.deception.deamon.utils.servermanipulation;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PomMavenUtils {

    private static String pomPath;

    @Value("${pom.location}")
    public void setPomPath(String pomPath) {
        PomMavenUtils.pomPath = pomPath;
    }

    public static void configureDefaultPom(){

        // Recupero il documento
        Document doc = getPomDocument(pomPath);

        // Creo le dipendenze da aggiungere
        List<Element> elements = new ArrayList<>();
        elements.add(createJAXBDependency(doc));
        elements.add(createCommonsLangDependency(doc));

        // Aggiungo le dipendenze al document
        addDependencyToPom(doc, elements);

        // Aggiorno il pom
        updateDom(doc, pomPath);

        // Aggiorno la versione Java del POM
        FileUtils.replaceStringInFile(pomPath, "<java.version>1.7</java.version>", "<java.version>1.8</java.version>");
    }

    public static void configSwaggerApiPom(){

        // Recupero il documento
        Document doc = getPomDocument(pomPath);

        // Creo le dipendenze da aggiungere
        List<Element> elements = new ArrayList<>();
        elements.add(createHibernateDependency(doc));
        elements.add(createJDBCMySqlDependency(doc));
        elements.add(createJPADependency(doc));
        elements.add(createJPAstarterDependency(doc));

        // Aggiungo le dipendenze al document
        addDependencyToPom(doc, elements);

        // Aggiorno il pom
        updateDom(doc, pomPath);

        // Modifico il pom specificando la main class
        // addMainClassConfiguration(pomPath, "io.swagger.Spring2Boot");
    }

    /*private static void addMainClassConfiguration(String pomFilePath, String mainClass) {
        List<String> lines = FileUtils.leggiFile(pomFilePath);

        // Cerca la posizione del tag </build>
        int buildTagIndex = findBuildTagIndex(lines);

        if (buildTagIndex != -1) {
            // Aggiungi la configurazione mainClass alla lista
            lines.add(buildTagIndex, "                <mainClass>" + mainClass + "</mainClass>");

            // Sovrascrivi il file con le linee modificate
            FileUtils.scriviFile(pomFilePath, lines);

            System.out.println("Configurazione mainClass aggiunta con successo a " + pomFilePath);
        } else {
            System.err.println("Tag </build> non trovato nel file " + pomFilePath);
        }
    }

    private static int findBuildTagIndex(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals("</build>")) {
                return i;
            }
        }
        return -1;
    }*/

    public static void configureSecurityPom(){

        // Recupero il documento
        Document doc = getPomDocument(pomPath);

        // Creo le dipendenze da aggiungere
        List<Element> elements = new ArrayList<>();
        elements.add(createSecurityStarterDependency(doc));
        elements.add(createSecurityOauth2ClientDependency(doc));
        elements.add(createSecurityOauth2JoseDependency(doc));

        // Aggiungo le dipendenze al document
        addDependencyToPom(doc, elements);

        // Aggiorno il pom
        updateDom(doc, pomPath);
    }

    public static void configureAdminPom(){

        // Recupero il documento
        Document doc = getPomDocument(pomPath);

        // Creo le dipendenze da aggiungere
        List<Element> elements = new ArrayList<>();
        elements.add(createSecurityStarterDependency(doc));
        elements.add(createThymeleafDependency(doc));

        // Aggiungo le dipendenze al document
        addDependencyToPom(doc, elements);

        // Aggiorno il pom
        updateDom(doc, pomPath);
    }

    public static void configureVulnServicePom() {

        if (FileUtils.readFile(pomPath).contains("spring-boot-starter-thymeleaf")) {
            return;
        }
        // Recupero il documento
        Document doc = getPomDocument(pomPath);

        // Creo le dipendenze da aggiungere
        List<Element> elements = new ArrayList<>();
        elements.add(createThymeleafDependency(doc));

        // Aggiungo le dipendenze al document
        addDependencyToPom(doc, elements);

        // Aggiorno il pom
        updateDom(doc, pomPath);

    }

    public static void configureJWTPom() {

        // Recupero il documento
        Document doc = getPomDocument(pomPath);

        // Creo le dipendenze da aggiungere
        List<Element> elements = new ArrayList<>();
        elements.add(createJWTDependency(doc));

        // Aggiungo le dipendenze al document
        addDependencyToPom(doc, elements);

        // Aggiorno il pom
        updateDom(doc, pomPath);
    }

    private static Document getPomDocument (String path) {
        // Crea il documento DOM dal file esistente
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document document;
        try {
            document = documentBuilder.parse(new File(path));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        return document;
    }

    private static void updateDom(Document document, String path) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        try {
            transformer.transform(new DOMSource(document), new StreamResult(new File(path)));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addDependencyToPom(Document document, List<Element> elements) {
        try {
            Element dependencies = (Element) document.getElementsByTagName("dependencies").item(0);
            for(Element e: elements) {
                dependencies.appendChild(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Element createJWTDependency(Document document) {
        return createDependency(document, "io.jsonwebtoken", "jjwt", "0.9.1");
    }

    private static Element createHibernateDependency(Document document) {
        return createDependency(document, "org.hibernate", "hibernate-core", "5.5.6.Final");
    }

    private static Element createJDBCMySqlDependency(Document document) {
        return createDependency(document, "mysql", "mysql-connector-java", "8.0.23");
    }

    private static Element createJAXBDependency(Document document) {
        return createDependency(document, "javax.xml.bind", "jaxb-api", "2.3.1");
    }

    private static Element createJPAstarterDependency(Document document) {
        return createDependency(document, "org.springframework.boot", "spring-boot-starter-data-jpa", null);
    }
    private static Element createJPADependency(Document document) {
        return createDependency(document, "org.springframework.data", "spring-data-jpa", "2.1.19.RELEASE");
    }

    private static Element createCommonsLangDependency(Document document) {
        return createDependency(document, "org.apache.commons", "commons-lang3", "3.13.0");
    }

    private static Element createSecurityOauth2JoseDependency(Document document) {
        return createDependency(document, "org.springframework.security", "spring-security-oauth2-jose", null);
    }

    private static Element createSecurityOauth2ClientDependency(Document document) {
        return createDependency(document, "org.springframework.security", "spring-security-oauth2-client", null);
    }

    private static Element createSecurityStarterDependency(Document document) {
        return createDependency(document, "org.springframework.boot", "spring-boot-starter-security", null);
    }

    private static Element createThymeleafDependency(Document document) {
        return createDependency(document, "org.springframework.boot", "spring-boot-starter-thymeleaf", null);
    }

    private static Element createDependency(Document document, String groupId, String artifactId, String version) {
        Element dependency = document.createElement("dependency");

        Element groupIdEl = document.createElement("groupId");
        groupIdEl.appendChild(document.createTextNode(groupId));
        dependency.appendChild(groupIdEl);

        Element artifactIdEl = document.createElement("artifactId");
        artifactIdEl.appendChild(document.createTextNode(artifactId));
        dependency.appendChild(artifactIdEl);

        if (!Utils.isNullOrEmpty(version)) {
            Element versionEl = document.createElement("version");
            versionEl.appendChild(document.createTextNode(version));
            dependency.appendChild(versionEl);
        }


        return dependency;
    }

}
