package cybersec.deception.deamon.services;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.YAMLUtils;
import cybersec.deception.deamon.utils.database.DatabaseUtils;
import cybersec.deception.deamon.utils.database.SQLFilesUtils;
import cybersec.deception.deamon.utils.servermanipulation.methods.MethodsGeneration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ManagePersistenceService {



    @Value("${folder.model}")
    private String modelFolder;

    @Value("${folder.api}")
    private String apiFolder;

    @Value("${entities.dir.location}")
    private String entitiesDirectory;

    @Value("${repository.interface.folder}")
    private String repositoryInterfaceDir;

    @Autowired
    public MockarooService mockarooService;


    public void managePersistence(String tableCode, Map<String, String> mockarooRequestsMap) {

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

        // Step 0: costruisco le entità cutson in json
        buildCustomEntities(mockarooRequestsMap);

        // Step 1: entità del modello con annotazioni Hibernate
        buildHibernateEntities(folderM.listFiles(), modelFolder, tableCode);

        // Step 4: modifico i Controller per aggiungere logica Hibernate
        for (File file : Objects.requireNonNull(folderAPI.listFiles())) {

            if (file.getName().contains("Controller") && isDefaultEntity(file.getName().replace("ApiController.java",""))) {

                // Step 4.a: genero import ed annotazioni
                aggiungiImportAnnotazioni(file.getAbsolutePath(), file.getName().replace("ApiController.java",""));

                // Step 4.b: scrittura dei metodi CRUD
                List<String> controllerContent = FileUtils.leggiFile(file.getAbsolutePath());

                for (File f: Objects.requireNonNull(folderM.listFiles())) {
                    String entityName = f.getName().replace(".java","");
                    if(entityName.equals(file.getName().replace("ApiController.java",""))) {

                        // Step 4.c: genero il file EntityRepository
                        createRepositoryInterface(entityName);

                        List<String> updatedControllerContent = MethodsGeneration.generateMethods(controllerContent, entityName);
                        FileUtils.scriviFile(file.getAbsolutePath(), updatedControllerContent);

                        break;
                    }
                }
            }
        }

        // Final step: rimuovo le entità custom
        deleteCustomEntities(mockarooRequestsMap);
    }

    private void deleteCustomEntities(Map<String, String> mockarooRequestsMap) {
        if (mockarooRequestsMap != null && !mockarooRequestsMap.isEmpty()) {
            for (Map.Entry<String, String> entry: mockarooRequestsMap.entrySet()) {
                String inputFilePath = FileUtils.buildPath(entitiesDirectory, entry.getKey().substring(7) + ".json");
                FileUtils.deleteFile(inputFilePath);
            }
        }
    }

    private void buildCustomEntities(Map<String, String> mockarooRequestsMap) {
        if (mockarooRequestsMap != null && !mockarooRequestsMap.isEmpty()) {
            for (Map.Entry<String, String> entry: mockarooRequestsMap.entrySet()) {
                String inputFilePath = FileUtils.buildPath(entitiesDirectory, entry.getKey().substring(7) + ".json");

                HttpRequest request = mockarooService.buildJSONRequest(entry.getValue());
                String result = mockarooService.generateData(request);

                FileUtils.scriviFile(inputFilePath, result);
            }
        }
    }

    private boolean isDefaultEntity(String entityName) {
        File[] files = FileUtils.getFilesFilteredByExtension(entitiesDirectory, ".json");
        if (files != null) {
            for (File f: files) {
                if (f.getName().replace(".json","").equals(entityName)) {
                    return true;
                }
            }

        }
        return false;
    }

    private void createRepositoryInterface(String entityName) {
        String str = "\n";
        if (entityName.equals("User")) {
            str = """
                        boolean existsByUsername(String username);\s
                        List<User> findByUsername(String username);
                    """;
        }
        else if (entityName.equals("Workstation")) {
            str = "    boolean existsByWorkstation(String workstation); \n";
        }

        String content = "package io.swagger.api;\n\n" +
                "import io.swagger.model." + entityName + ";\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n" +
                "@Repository\n" +
                "public interface " + entityName + "Repository extends JpaRepository<" + entityName + ", Long> {\n" +
                str + "}";

        String path = FileUtils.buildPath(repositoryInterfaceDir, entityName + "Repository.java");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupDatabase(String yamlString, String tableCode, Map<String, String> mockarooRequestsMap) {
        Map<String, List<String>> componentsProperties = YAMLUtils.getComponentsProperties(yamlString);
        DatabaseUtils.createDatabaseAndTable(componentsProperties, tableCode, mockarooRequestsMap);
    }

    private void buildHibernateEntities(File[] files, String folderPath, String tableCode) {

        if (files != null) {
            for (File file : files) {
                try {
                    if (isDefaultEntity(file.getName().replace(".java",""))) {
                        transformJavaFile(folderPath+"/"+file.getName(), tableCode);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Transformation completed successfully!");
            }
        } else {
            System.out.println("La cartella è vuota.");
        }
    }

    private void transformJavaFile(String inputFilePath, String tableCode) throws IOException {
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
                lines.set(i, "@Entity\n@Table(name = \"" + tableCode + "_" + tableName.toLowerCase() + "\")\n" + lines.get(i));
            }

            // Controlla se la linea contiene un campo con annotazione @JsonProperty
            if (line.contains("@JsonProperty")) {
                if (line.contains("@JsonProperty(\"id\")")) {
                    // Trasforma la linea per includere @Id, @GeneratedValue e @Column annotations
                    lines.set(i, "  @Id\n  @GeneratedValue(strategy = GenerationType.IDENTITY)\n  " + line.trim());
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
                nuovoContenuto.add("import java.util.Date;");
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




}
