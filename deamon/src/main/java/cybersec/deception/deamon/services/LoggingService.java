package cybersec.deception.deamon.services;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.database.DatabaseUtils;
import cybersec.deception.deamon.utils.servermanipulation.ApplPropUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class LoggingService {

    @Value("${log.httprequestlog.model}")
    private String httprequestlogPath;

    @Value("${log.sqlfile.createtable}")
    private String sqlLogFilePath;

    @Value("${log.source.folder}")
    private String logSourceFolder;

    @Value("${log.destination.folder}")
    private String logDestinationFolder;

    @Value("${folder.api}")
    private String apiFolder;

    public void manageLogging(String tableCode, boolean persistence) {

        // aggiungo la cartella per la gestione del Logging nel server
        try {
            FileUtils.copyDirectory(logSourceFolder, logDestinationFolder);

            // creo il nome della tabella per gestire il log con il baseCode associato
            FileUtils.replaceStringInFile(httprequestlogPath, "TABLECODETOSUBSTITUTE", tableCode +"_");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // aggiungo i parametri di configurazione di JPA (se non è già stato fatto)
        if (!persistence) {
            ApplPropUtils.addApplicationPropertiesJPAconfig();
        }

        // Aggiungo infromazioni di cifratura per il log
        ApplPropUtils.addApplicationPropertiesCryptoConfig();

        // aggiungo il metodo "logService.log()" ad ogni metodo dei controller
        addLogsManagement();

        // creo nel db la tabella per la gestione del log
        FileUtils.replaceStringInFile(sqlLogFilePath, "TABLECODETOSUBSTITUTE", tableCode + "_");
        DatabaseUtils.executeSqlFile(sqlLogFilePath);
        FileUtils.replaceStringInFile(sqlLogFilePath, tableCode + "_", "TABLECODETOSUBSTITUTE");
    }

    private void addLogsManagement() {

        File folderAPI = new File(apiFolder);
        if (!folderAPI.exists() || !folderAPI.isDirectory()) {
            System.err.println("La cartella Model non esiste");
            return;
        }

        // Itero su tutti i controller e aggiungo la gestione del log
        for (File file : Objects.requireNonNull(folderAPI.listFiles())) {
            if (file.getName().contains("Controller")) {
                addLogsInController(file.getPath());
            }
        }
    }


    private void addLogsInController(String filePath) {
        List<String> lines = FileUtils.leggiFile(filePath);
        List<String> newContent = new ArrayList<>();

        boolean importAggiunto = false, logService = false, foundMethod = false;
        for (String riga : lines) {

            if (!importAggiunto && riga.contains("package io.swagger.api;")) {
                newContent.add(riga);
                newContent.add("\nimport io.swagger.log.LogService;");
                newContent.add("\nimport org.springframework.beans.factory.annotation.Autowired;");
                importAggiunto = true;
                continue;
            }

            if (!logService && riga.contains("private final HttpServletRequest request;")) {
                newContent.add("\n    @Autowired");
                newContent.add("    private LogService logService;\n");
                logService = true;
            }

            newContent.add(riga);
            if (riga.contains("public ResponseEntity<")) {
                foundMethod = true;
            }

            if (riga.contains(") {") && foundMethod) {
                newContent.add("        logService.log(request);\n");
                foundMethod = false;
            }
        }

        FileUtils.scriviFile(filePath, newContent);
        System.out.println("Modifiche applicate con successo.");
    }
}
