package cybersec.deception.deamon.utils;

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

    @Value("${pom.location}")
    private static String pomPath;

    public static void configSwaggerApiPom(){

        // Recupero il documento
        Document doc = getPomDocument(pomPath);

        // Creo le dipendenze da aggiungere
        List<Element> elements = new ArrayList<>();
        elements.add(createHibernateDependency(doc));
        elements.add(createJDBCMySqlDependency(doc));

        // Aggiungo le dipendenze al document
        addDependencyToPom(doc, elements);

        // Aggiorno il pom
        updateDom(doc, pomPath);

        // Aggiorno la versione Java del POM
        FileUtils.replaceStringInFile(pomPath, "<java.version>1.7</java.version>", "<java.version>1.8</java.version>");

        // Modifico il pom specificando la main class
        addMainClassConfiguration(pomPath, "io.swagger.Spring2Boot");
    }

    private static void addMainClassConfiguration(String pomFilePath, String mainClass) {
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
    }

    private static Document getPomDocument (String path) {
        // Crea il documento DOM dal file esistente
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
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
        Transformer transformer = null;
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

    private static Element createHibernateDependency(Document document) {
        return createDependency(document, "org.hibernate", "hibernate-core", "5.5.6.Final");
    }

    private static Element createJDBCMySqlDependency(Document document) {
        return createDependency(document, "mysql", "mysql-connector-java", "8.0.23");
    }

    private static Element createDependency(Document document, String groupId, String artifactId, String version) {
        Element dependency = document.createElement("dependency");

        Element groupIdEl = document.createElement("groupId");
        groupIdEl.appendChild(document.createTextNode(groupId));
        dependency.appendChild(groupIdEl);

        Element artifactIdEl = document.createElement("artifactId");
        artifactIdEl.appendChild(document.createTextNode(artifactId));
        dependency.appendChild(artifactIdEl);

        Element versionEl = document.createElement("version");
        versionEl.appendChild(document.createTextNode(version));
        dependency.appendChild(versionEl);

        return dependency;
    }

}