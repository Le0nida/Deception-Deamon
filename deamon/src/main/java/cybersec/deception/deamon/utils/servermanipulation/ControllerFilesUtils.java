package cybersec.deception.deamon.utils.servermanipulation;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.methods.MethodsGeneration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class ControllerFilesUtils {

    private static String apiFolder;

    @Value("${folder.api}")
    public void setApiFolder(String apiFolder) {
        ControllerFilesUtils.apiFolder = apiFolder;
    }

    public static List<File> getControllers() {

        List<File> files = new ArrayList<>();

        File folderAPI = new File(apiFolder);
        if (!folderAPI.exists() || !folderAPI.isDirectory()) {
            System.err.println("La cartella API non esiste");
            return files;
        }

        for (File file : Objects.requireNonNull(folderAPI.listFiles())) {
            if (file.getName().contains("Controller")) {
                files.add(file);
            }
        }
        return files;
    }

    public static String getNotImplementedMethods() {

        File folderAPI = new File(apiFolder);
        if (!folderAPI.exists() || !folderAPI.isDirectory()) {
            System.err.println("La cartella Model non esiste");
            return "Error retrieving methods";
        }

        Map<String, List<String>> notImplementedMethods = new HashMap<>();

        // Itero su tutti i controller e aggiungo la gestione del log
        for (File file : Objects.requireNonNull(folderAPI.listFiles())) {
            if (file.getName().contains("Controller")) {
                List<String> controllerContent = FileUtils.leggiFile(file.getAbsolutePath());
                List<String> list = findNotImplementedMethods(controllerContent);
                if (!list.isEmpty()) {
                    notImplementedMethods.put(file.getName(), list);
                }
            }
        }

        if (notImplementedMethods.isEmpty()) {
            return "Good news! All methods are implemented!";
        }

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

    private static List<String> findNotImplementedMethods(List<String> content) {
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

    public static void substituteMethod(List<String> content, String signature, String codeToInject){
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

}
