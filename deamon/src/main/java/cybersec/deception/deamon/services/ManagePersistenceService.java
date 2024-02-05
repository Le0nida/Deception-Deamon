package cybersec.deception.deamon.services;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ManagePersistenceService {

    @Value("${pom.location}")
    private String pomPath;

    @Value("${hibernate.cfg.location}")
    private String hibernateCfgPath;

    @Value("${hibernate.utils.location}")
    private String hibernateUtilsPath;

    @Value("${database.driverclass}")
    private String driverClass;

    @Value("${database.url}")
    private String url;

    @Value("${database.username}")
    private String username;

    @Value("${database.password}")
    private String password;

    @Value("${database.dialect}")
    private String dialect;

    @Value("${folder.model}")
    private String modelFolder;

    @Value("${folder.api}")
    private String apiFolder;
    public void managePersistence() {

        File folder = new File(modelFolder);

        if (folder.exists() && folder.isDirectory()) {

            // Step 1: entità del modello con annotazioni Hibernate
            // TODO far si che i file siano sostituiti
            buildHibernateEntities(folder.listFiles(), modelFolder);

            // Step 2: aggiungo la dipendenza Hibernate al pom
            addDependencyToPom();
            try {
                replaceStringInFile(pomPath, "<java.version>1.7</java.version>", "<java.version>1.8</java.version>");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            List<String> filePathList = new ArrayList<>();
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                filePathList.add(file.getAbsolutePath());
            }
            // Step 3: genero il file di configurazione di Hibernate
            generateHibernateConfig(hibernateCfgPath, driverClass, url, username, password, dialect, filePathList);


            // Stp 4: genero il file di utilità di Hibernate
            FileUtils.createFile(generateHibernateUtilContent(), hibernateUtilsPath);
            System.out.println("File created successfully: " + hibernateUtilsPath);

            File folderAPI = new File(apiFolder);
            for (File file : Objects.requireNonNull(folderAPI.listFiles())) {
                // Step 4: genero i dati di interesse nel controller
                if (file.getName().contains("Controller")) {
                    try {
                        aggiungiImportAnnotazioni(file.getAbsolutePath());

                        // Step 5: aggiunta metodi crud
                        List<String> controllerContent = leggiFile(file.getAbsolutePath());

                        for (File f: Objects.requireNonNull(folder.listFiles())) {
                            String entityName = f.getName().replace(".java","");
                            if(entityName.equals(file.getName().replace("ApiController.java",""))) {
                                String modelContent = readFile(f.getAbsolutePath());
                                List<String> updatedControllerContent = generateHibernateCRUD(controllerContent, modelContent, entityName);
                                scriviFile(file.getAbsolutePath(), updatedControllerContent);
                                System.out.println("CRUD methods updated successfully.");
                                break;
                            }
                        }


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        } else {
            System.out.println("La cartella non esiste o non è una directory.");
        }
    }

    private static void replaceStringInFile(String filePath, String searchString, String replacement) throws IOException {
        // Leggi tutte le linee del file e trasformale in una lista di stringhe
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        // Effettua la sostituzione della stringa in ogni linea
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // Effettua la sostituzione della stringa se presente
            lines.set(i, line.replace(searchString, replacement));
        }

        // Sovrascrivi il file con le linee modificate
        Files.write(path, lines, StandardCharsets.UTF_8);

        System.out.println("Sostituzione eseguita con successo nel file: " + filePath);
    }

    private static void buildHibernateEntities(File[] files, String folderPath) {

        if (files != null) {
            for (File file : files) {
                try {
                    transformJavaFile(folderPath+"/"+file.getName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Transformation completed successfully!");
            }
        } else {
            System.out.println("La cartella è vuota.");
        }
    }

    private static void transformJavaFile(String inputFilePath) throws IOException {
        // Leggi tutto il contenuto del file e trasformalo in una lista di stringhe
        Path path = Paths.get(inputFilePath);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        String tableName = inputFilePath.substring(inputFilePath.lastIndexOf("/") + 1).replace(".java", "").toLowerCase();
        boolean importAdded = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.contains("import java.util.Objects;")) {
                // Aggiungi l'import necessario se non è già presente
                if (!importAdded) {
                    lines.add(i, "import javax.persistence.*;");
                    importAdded = true;
                }
            }

            if (line.contains("public class")) {
                // Trasforma la linea per includere l'annotazione @Entity
                lines.set(i, "@Entity\n@Table(name = \"" + tableName + "\")\n" + lines.get(i));
            }

            // Controlla se la linea contiene un campo con annotazione @JsonProperty
            if (line.contains("@JsonProperty")) {
                if (line.contains("@JsonProperty(\"id\")")) {
                    // Trasforma la linea per includere @Id, @GeneratedValue e @Column annotations
                    lines.set(i, "  @Id\n  @GeneratedValue(strategy = GenerationType.IDENTITY)\n  @Column\n  " + line.trim());
                } else {
                    // Trasforma la linea per includere l'annotazione @Column
                    lines.set(i, "  @Column\n  " + line.trim());
                }
            }
        }

        // Scrivi le linee trasformate direttamente nel file di input
        Files.write(path, lines, StandardCharsets.UTF_8);

        System.out.println("File trasformato con successo: " + inputFilePath);
    }



    private void addDependencyToPom() {
        try {
            // Crea il documento DOM dal file esistente
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new File(pomPath));

            // Aggiunge la dipendenza
            Element dependencies = (Element) document.getElementsByTagName("dependencies").item(0);
            dependencies.appendChild(createHibernateDependency(document));
            dependencies.appendChild(createJDBCMySqlDependency(document));

            // Scrive il documento DOM aggiornato nel file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(new File(pomPath)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Element createHibernateDependency(Document document) {
        Element dependency = document.createElement("dependency");

        Element groupId = document.createElement("groupId");
        groupId.appendChild(document.createTextNode("org.hibernate"));
        dependency.appendChild(groupId);

        Element artifactId = document.createElement("artifactId");
        artifactId.appendChild(document.createTextNode("hibernate-core"));
        dependency.appendChild(artifactId);

        Element version = document.createElement("version");
        version.appendChild(document.createTextNode("5.5.6.Final"));
        dependency.appendChild(version);

        return dependency;
    }

    private static Element createJDBCMySqlDependency(Document document) {
        Element dependency = document.createElement("dependency");

        Element groupId = document.createElement("groupId");
        groupId.appendChild(document.createTextNode("mysql"));
        dependency.appendChild(groupId);

        Element artifactId = document.createElement("artifactId");
        artifactId.appendChild(document.createTextNode("mysql-connector-java"));
        dependency.appendChild(artifactId);

        Element version = document.createElement("version");
        version.appendChild(document.createTextNode("8.0.23"));
        dependency.appendChild(version);

        return dependency;
    }


    private static void generateHibernateConfig(String configFile, String driverClass, String url,
                                               String username, String password, String dialect,
                                               List<String> entityClassPaths) {

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            writer.write("<!DOCTYPE hibernate-configuration PUBLIC\n");
            writer.write("        \"-//Hibernate/Hibernate Configuration DTD 3.0//EN\"\n");
            writer.write("        \"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">\n");
            writer.write("<hibernate-configuration>\n");
            writer.write("\n");
            writer.write("    <session-factory>\n");
            writer.write("        <!-- JDBC Database connection settings -->\n");
            writer.write("        <property name=\"hibernate.connection.driver_class\">" + driverClass + "</property>\n");
            writer.write("        <property name=\"hibernate.connection.url\">" + url + "</property>\n");
            writer.write("        <property name=\"hibernate.connection.username\">" + username + "</property>\n");
            writer.write("        <property name=\"hibernate.connection.password\">" + password + "</property>\n");
            writer.write("\n");
            writer.write("        <!-- Specify dialect -->\n");
            writer.write("        <property name=\"hibernate.dialect\">" + dialect + "</property>\n");
            writer.write("\n");
            writer.write("        <!-- Echo all executed SQL to stdout -->\n");
            writer.write("        <property name=\"hibernate.show_sql\">true</property>\n");
            writer.write("\n");
            writer.write("        <!-- Drop and re-create the database schema on startup -->\n");
            writer.write("        <property name=\"hibernate.hbm2ddl.auto\">update</property>\n");
            writer.write("\n");
            // Mapping entity classes
            for (String entityClassPath : entityClassPaths) {
                entityClassPath = entityClassPath.substring(entityClassPath.lastIndexOf("\\main\\")+1);
                writer.write("        <mapping class=\"" + entityClassPath + "\"/>\n");
            }
            writer.write("\n");
            writer.write("    </session-factory>\n");
            writer.write("</hibernate-configuration>\n");

            System.out.println("Hibernate configuration file generated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void aggiungiImportAnnotazioni(String nomeFile) throws IOException {
        List<String> righe = leggiFile(nomeFile);
        List<String> nuovoContenuto = new ArrayList<>();

        boolean importAggiunto = false;
        boolean persistenceUnitAggiunto = false;
        boolean addedPackage = false;
        for (String riga : righe) {

            if (!importAggiunto && riga.contains("import javax.servlet.http.HttpServletRequest;")) {
                nuovoContenuto.add("import javax.persistence.EntityManagerFactory;");
                nuovoContenuto.add("import javax.persistence.PersistenceUnit;");
                importAggiunto = true;
            }

            if (!persistenceUnitAggiunto && riga.contains("private final HttpServletRequest request;")) {
                nuovoContenuto.add("\n    @PersistenceUnit");
                nuovoContenuto.add("    private EntityManagerFactory entityManagerFactory;\n");
                persistenceUnitAggiunto = true;
            }

            if (riga.contains("public ResponseEntity<")) {
                nuovoContenuto.add("    @Transactional");
            }

            nuovoContenuto.add(riga);
        }

        scriviFile(nomeFile, nuovoContenuto);
        System.out.println("Modifiche applicate con successo.");
    }

    private static List<String> leggiFile(String nomeFile) throws IOException {
        List<String> righe = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(nomeFile));

        String riga;
        while ((riga = reader.readLine()) != null) {
            righe.add(riga);
        }

        reader.close();
        return righe;
    }

    private static void scriviFile(String nomeFile, List<String> contenuto) throws IOException {
        FileWriter writer = new FileWriter(nomeFile);

        for (String riga : contenuto) {
            writer.write(riga + "\n");
        }

        writer.close();
    }

    private static String generateHibernateUtilContent() {
        return """
                package io.swagger.api;
                
                import org.hibernate.SessionFactory;
                import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
                import org.hibernate.cfg.Configuration;

                public class HibernateUtil {
                    private static final SessionFactory sessionFactory = buildSessionFactory();

                    private static SessionFactory buildSessionFactory() {
                        try {
                            Configuration configuration = new Configuration().configure();
                            return configuration.buildSessionFactory(
                                new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build()
                            );
                        } catch (Exception e) {
                            System.err.println("Initial SessionFactory creation failed." + e);
                            throw new ExceptionInInitializerError(e);
                        }
                    }

                    public static SessionFactory getSessionFactory() {
                        return sessionFactory;
                    }

                    public static void shutdown() {
                        getSessionFactory().close();
                    }
                }
                """;
    }

    private List<String> generateHibernateCRUD(List<String> controllerContent, String modelContent, String entityName) {
        // For simplicity, let's assume you have a HibernateUtil class for setting up Hibernate SessionFactory
        String hibernateUtilImport = "import javax.transaction.Transactional;\nimport org.hibernate.Session;\nimport org.hibernate.Transaction;";

        // Retrieve the method signatures to be replaced
        String createMethodSignature = "public ResponseEntity<" + entityName + "> create" + entityName + "(";//@Parameter(in = ParameterIn.DEFAULT, description = \"Created " + entityName.toLowerCase() + " object\", schema=@Schema()) @Valid @RequestBody " + entityName + " body)";
        String deleteMethodSignature = "public ResponseEntity<Void> delete" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"The name that needs to be deleted\", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name)";
        String updateMethodSignature = "public ResponseEntity<Void> update" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"name that need to be deleted\", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name, @Parameter(in = ParameterIn.DEFAULT, description = \"Update an existent " + entityName.toLowerCase() + " in the store\", schema=@Schema()) @Valid @RequestBody " + entityName + " body)";
        String retrieveMethodSignature = "public ResponseEntity<" + entityName + "> get" + entityName + "ById(";//@Parameter(in = ParameterIn.PATH, description = \"The name that needs to be fetched. Use " + entityName.toLowerCase() + "1 for testing. \", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name)";

        // Generate the updated method implementations
        String createMethodImplementation = generateCreateMethod(modelContent, entityName);
        String deleteMethodImplementation = generateDeleteMethod(modelContent, entityName);
        String updateMethodImplementation = generateUpdateMethod(modelContent, entityName);
        String retrieveMethodImplementation = generateRetrieveMethod(modelContent, entityName);

        for (int i = 0; i < controllerContent.size(); i++) {
            String line = controllerContent.get(i);
            if (line.contains(("import org.springframework.http.ResponseEntity;"))) {
                controllerContent.set(i, line + "\n" + hibernateUtilImport);
                break;
            }
        }

        substituteMethod(controllerContent, createMethodSignature, createMethodImplementation);
        substituteMethod(controllerContent, updateMethodSignature, updateMethodImplementation);
        substituteMethod(controllerContent, retrieveMethodSignature, retrieveMethodImplementation);
        substituteMethod(controllerContent, deleteMethodSignature, deleteMethodImplementation);

        removeEmptyStrings(controllerContent);

        return controllerContent;
    }

    private void removeEmptyStrings(List<String> stringList) {

        stringList.removeIf(str -> str.equals("null"));
    }

    private void substituteMethod(List<String> content, String signature, String codeToInject){
        boolean found = false, alreadyFound = false;
        for (int i = 0; i < content.size(); i++) {
            String line = content.get(i);
            if (line.contains(signature)) {
                found = true;
                continue;
            }
            if (found) {
                content.set(i, codeToInject);
                found = false;
                alreadyFound = true;
            }
            else if (alreadyFound) {
                content.set(i, "null");
                if (line.contains(">(HttpStatus.NOT_IMPLEMENTED);")) {
                    break;
                }
            }
        }
    }
    private static String generateCreateMethod(String modelContent, String entityName) {
        // Implement the logic to generate the create method
        return "    Session session = HibernateUtil.getSessionFactory().openSession();\n" +
                "    Transaction transaction = null;\n" +
                "    try {\n" +
                "        transaction = session.beginTransaction();\n" +
                "        session.save(body);\n" +
                "        transaction.commit();\n" +
                "        return new ResponseEntity<>(body, HttpStatus.CREATED);\n" +
                "    } catch (Exception e) {\n" +
                "        if (transaction != null) {\n" +
                "            transaction.rollback();\n" +
                "        }\n" +
                "        log.error(\"Error creating " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    } finally {\n" +
                "        session.close();\n" +
                "    }\n";
    }

    private static String generateDeleteMethod(String modelContent, String entityName) {
        // Implement the logic to generate the delete method
        return "    Session session = HibernateUtil.getSessionFactory().openSession();\n" +
                "    Transaction transaction = null;\n" +
                "    try {\n" +
                "        transaction = session.beginTransaction();\n" +
                "        " + entityName + " " + entityName.toLowerCase() + " = session.get(" + entityName + ".class, " + entityName.toLowerCase() + "name);\n" +
                "        if (" + entityName.toLowerCase() + " != null) {\n" +
                "            session.delete(" + entityName.toLowerCase() + ");\n" +
                "            transaction.commit();\n" +
                "            return new ResponseEntity<>(HttpStatus.NO_CONTENT);\n" +
                "        } else {\n" +
                "            return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "        }\n" +
                "    } catch (Exception e) {\n" +
                "        if (transaction != null) {\n" +
                "            transaction.rollback();\n" +
                "        }\n" +
                "        log.error(\"Error deleting " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    } finally {\n" +
                "        session.close();\n" +
                "    }\n";
    }

    private static String generateUpdateMethod(String modelContent, String entityName) {
        // Implement the logic to generate the update method
        return "    Session session = HibernateUtil.getSessionFactory().openSession();\n" +
                "    Transaction transaction = null;\n" +
                "    try {\n" +
                "        transaction = session.beginTransaction();\n" +
                "        " + entityName + " existing" + entityName + " = session.get(" + entityName + ".class, " + entityName.toLowerCase() + "name);\n" +
                "        if (existing" + entityName + " != null) {\n" +
                "            // Aggiorna le proprietà dell'utente esistente con i nuovi valori\n" +
                "            existing" + entityName + ".setFirstName(body.getFirstName());\n" +
                "            existing" + entityName + ".setLastName(body.getLastName());\n" +
                "            existing" + entityName + ".setEmail(body.getEmail());\n" +
                "            existing" + entityName + ".setPassword(body.getPassword());\n" +
                "            existing" + entityName + ".setPhone(body.getPhone());\n" +
                "            existing" + entityName + ".set" + entityName + "Status(body.get" + entityName + "Status());\n" +
                "            session.update(existing" + entityName + ");\n" +
                "            transaction.commit();\n" +
                "            return new ResponseEntity<>(HttpStatus.NO_CONTENT);\n" +
                "        } else {\n" +
                "            return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "        }\n" +
                "    } catch (Exception e) {\n" +
                "        if (transaction != null) {\n" +
                "            transaction.rollback();\n" +
                "        }\n" +
                "        log.error(\"Error updating " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    } finally {\n" +
                "        session.close();\n" +
                "    }\n";
    }

    private static String generateRetrieveMethod(String modelContent, String entityName) {
        // Implement the logic to generate the retrieve method
        return "    Session session = HibernateUtil.getSessionFactory().openSession();\n" +
                "    try {\n" +
                "        " + entityName + " " + entityName.toLowerCase() + " = session.get(" + entityName + ".class, " + entityName.toLowerCase() + "name);\n" +
                "        if (" + entityName.toLowerCase() + " != null) {\n" +
                "            return new ResponseEntity<>(" + entityName.toLowerCase() + ", HttpStatus.OK);\n" +
                "        } else {\n" +
                "            return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "        }\n" +
                "    } catch (Exception e) {\n" +
                "        log.error(\"Error retrieving " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    } finally {\n" +
                "        session.close();\n" +
                "    }\n";
    }

    private static String readFile(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)).collect(Collectors.joining("\n"));
    }

    private static void writeToFile(String filePath, String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes());
    }

    public List<String> getNotImplementedMethods() {
        return null;
    }
}
