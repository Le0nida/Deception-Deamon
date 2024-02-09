package cybersec.deception.deamon.services;

import cybersec.deception.deamon.utils.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ManagePersistenceService {

    @Value("${database.driverclass}")
    private String driverClass;

    @Value("${database.url}")
    private String url;

    @Value("${database.username}")
    private String username;

    @Value("${database.password}")
    private String password;

    @Value("${folder.model}")
    private String modelFolder;

    @Value("${folder.api}")
    private String apiFolder;

    @Value("${application.properties.location}")
    private String appPropertiesPath;

    @Value("${repository.interface.folder}")
    private String repositoryInterfaceDir;

    private final Map<String, List<String>> notImplementedMethods = new HashMap<>();

    public String getNotImplementedMethods() {

        StringBuilder builder = new StringBuilder("In the following file, there are methods (and their corresponding files) that have not been implemented for database usage.\n\n\n");
        for (String fileName : notImplementedMethods.keySet()){
            builder.append("- ").append(fileName).append("\n");
            for (String methodName: notImplementedMethods.get(fileName)) {
                builder.append("\t - ").append(methodName).append("\n");
            }
            builder.append("\n\n");
        }
        return builder.toString();
    }

    public void managePersistence() {

        File folderM = new File(modelFolder);
        File folderAPI = new File(apiFolder);

        if (!folderM.exists() || !folderM.isDirectory()) {
            System.err.println("La cartella Model non esiste");
            return;
        }
        if (!folderAPI.exists() || !folderAPI.isDirectory()) {
            System.err.println("La cartella API non esiste");
            return;
        }

        notImplementedMethods.clear();

        // Step 1: entità del modello con annotazioni Hibernate
        buildHibernateEntities(folderM.listFiles(), modelFolder);

        // Step 2: configuro il pom per usare Hibernate
        PomMavenUtils.configSwaggerApiPom();

        // Step 3: genero il file di configurazione di Hibernate
        addApplicationPropertiesJPAconfig();

        // Step 4: modifico i Controller per aggiungere logica Hibernate
        for (File file : Objects.requireNonNull(folderAPI.listFiles())) {

            if (file.getName().contains("Controller")) {

                // Step 4.a: genero import ed annotazioni
                aggiungiImportAnnotazioni(file.getAbsolutePath(), file.getName().replace("ApiController.java",""));

                // Step 4.b: scrittura dei metodi CRUD
                List<String> controllerContent = FileUtils.leggiFile(file.getAbsolutePath());

                for (File f: Objects.requireNonNull(folderM.listFiles())) {
                    String entityName = f.getName().replace(".java","");
                    if(entityName.equals(file.getName().replace("ApiController.java",""))) {

                        // Step 4.c: genero il file EntityRepository
                        createRepositoryInterface(entityName);

                        String modelContent = FileUtils.readFile(f.getAbsolutePath());
                        List<String> updatedControllerContent = generateJPACRUD(controllerContent, modelContent, entityName);
                        FileUtils.scriviFile(file.getAbsolutePath(), updatedControllerContent);

                        // Step 6: aggiorno la mappa di metodi non implementati
                        notImplementedMethods.put(file.getName(), findNotImplementedMethods(updatedControllerContent));

                        break;
                    }
                }
            }
        }
    }

    private void addApplicationPropertiesJPAconfig() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(appPropertiesPath, true))) {
            // Aggiungi le configurazioni al file
            writer.newLine();
            writer.write("# Configurazione del datasource");
            writer.newLine();
            writer.write("spring.datasource.url="+url);
            writer.newLine();
            writer.write("spring.datasource.username=" + username);
            writer.newLine();
            writer.write("spring.datasource.password=" + password);
            writer.newLine();
            writer.write("spring.datasource.driver-class-name=" + driverClass);
            writer.newLine();
            writer.newLine();
            writer.write("# Configurazione JPA");
            writer.newLine();
            writer.write("spring.jpa.properties.hibernate.show_sql=true");
            writer.newLine();
            writer.write("spring.jpa.hibernate.ddl-auto=update");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRepositoryInterface(String entityName) {
        String content = "package io.swagger.api;\n\n" +
                "import io.swagger.model." + entityName + ";\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n" +
                "@Repository\n" +
                "public interface " + entityName + "Repository extends JpaRepository<" + entityName + ", Long> {\n" +
                "    \n" +
                "}";

        String path = FileUtils.buildPath(repositoryInterfaceDir, entityName + "Repository.java");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupDatabase(String yamlString) {
        Map<String, List<String>> componentsProperties = YAMLUtils.getComponentsProperties(yamlString);
        DatabaseUtils.createDatabaseAndTable(componentsProperties);
    }

    private void buildHibernateEntities(File[] files, String folderPath) {

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

    private void transformJavaFile(String inputFilePath) throws IOException {
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
                lines.set(i, "@Entity\n@Table(name = \"" + tableName.toLowerCase() + "\")\n" + lines.get(i));
            }

            // Controlla se la linea contiene un campo con annotazione @JsonProperty
            if (line.contains("@JsonProperty")) {
                if (line.contains("@JsonProperty(\"id\")")) {
                    // Trasforma la linea per includere @Id, @GeneratedValue e @Column annotations
                    lines.set(i, "  @Id\n  @Column\n  " + line.trim());
                    //lines.set(i, "  @Id\n  @GeneratedValue(strategy = GenerationType.IDENTITY)\n  @Column\n  " + line.trim());
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

    private void aggiungiImportAnnotazioni(String nomeFile, String entityName) {
        List<String> righe;
        righe = FileUtils.leggiFile(nomeFile);
        List<String> nuovoContenuto = new ArrayList<>();

        boolean importAggiunto = false;
        boolean persistenceUnitAggiunto = false;
        for (String riga : righe) {

            if (!importAggiunto && riga.contains("import javax.servlet.http.HttpServletRequest;")) {
                nuovoContenuto.add("import org.springframework.beans.factory.annotation.Autowired;");
                nuovoContenuto.add("import javax.transaction.Transactional;");
                importAggiunto = true;
            }

            if (!persistenceUnitAggiunto && riga.contains("private final HttpServletRequest request;")) {
                nuovoContenuto.add("\n    @Autowired");
                nuovoContenuto.add("    private " + entityName + "Repository " + entityName.toLowerCase() + "Repository;\n");
                persistenceUnitAggiunto = true;
            }

            if (riga.contains("public ResponseEntity<")) {
                nuovoContenuto.add("    @Transactional");
            }

            nuovoContenuto.add(riga);
        }

        FileUtils.scriviFile(nomeFile, nuovoContenuto);
        System.out.println("Modifiche applicate con successo.");
    }

    private List<String> generateJPACRUD(List<String> controllerContent, String modelContent, String entityName) {
        // Retrieve the method signatures to be replaced
        String createMethodSignature = "public ResponseEntity<" + entityName + "> create" + entityName + "(";//@Parameter(in = ParameterIn.DEFAULT, description = \"Created " + entityName.toLowerCase() + " object\", schema=@Schema()) @Valid @RequestBody " + entityName + " body)";
        String deleteMethodSignature = "public ResponseEntity<Void> delete" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"The name that needs to be deleted\", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name)";
        String updateMethodSignature = "public ResponseEntity<" + entityName + "> update" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"name that need to be deleted\", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name, @Parameter(in = ParameterIn.DEFAULT, description = \"Update an existent " + entityName.toLowerCase() + " in the store\", schema=@Schema()) @Valid @RequestBody " + entityName + " body)";
        String retrieveMethodSignature = "public ResponseEntity<" + entityName + "> retrieve" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"The name that needs to be fetched. Use " + entityName.toLowerCase() + "1 for testing. \", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name)";

        // Generate the method implementations
        String createMethodImplementation = CRUDMethodsUtils.getCreateMethod(entityName);
        String deleteMethodImplementation = CRUDMethodsUtils.getDeleteMethod(entityName);
        String updateMethodImplementation = CRUDMethodsUtils.getUpdateMethod(entityName);
        String retrieveMethodImplementation = CRUDMethodsUtils.getRetrieveMethod(entityName);

        substituteMethod(controllerContent, createMethodSignature, createMethodImplementation);
        substituteMethod(controllerContent, updateMethodSignature, updateMethodImplementation);
        substituteMethod(controllerContent, retrieveMethodSignature, retrieveMethodImplementation);
        substituteMethod(controllerContent, deleteMethodSignature, deleteMethodImplementation);

        // Rimuovo le stringhe che corrispondevano ai vecchi contenuti del metodo
        Utils.removeEmptyStrings(controllerContent, "null");

        return controllerContent;
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

    public List<String> findNotImplementedMethods(List<String> content) {
        List<String> methods = new ArrayList<>();
        String method = "";
        boolean metodoIniziato = false, metodoFinito = false;

        for (String line : content) {
            if (!metodoIniziato && line.contains("public ResponseEntity")) {
                metodoIniziato = true;
                method = line;
                continue;
            }
            if (metodoIniziato) {

                // se ne incontro un altro prima di "HttpStatus.NOT_IMPLEMENTED"
                if (line.contains("public ResponseEntity")) {
                    metodoIniziato = false;
                    metodoFinito = false;
                }
                method += "\n" + line;

                if (metodoFinito) {
                    metodoFinito = false;
                    metodoIniziato = false;
                    methods.add(method.substring(0, method.indexOf("(")));
                }
                if (line.contains(">(HttpStatus.NOT_IMPLEMENTED);")) {
                    metodoFinito = true;
                }


            }

        }
        return methods;
    }
}
