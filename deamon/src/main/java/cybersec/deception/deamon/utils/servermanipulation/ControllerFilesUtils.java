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

    public static String getNotImplementedMethods(boolean persistence) {

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
                List<String> list = MethodsGeneration.getNotImplementedMethods(controllerContent, file.getName().replace("ApiController.java", ""), persistence);
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
        builder.append("These files are available in \"\\server\\generatedServer\\src\\main\\java\\io\\swagger\\api\\\".");
        return builder.toString();
    }

    public static void substituteMethod(List<String> content, String signature, String codeToInject){
        boolean found = false, alreadyFound = false, endSignature = false;
        for (int i = 0; i < content.size(); i++) {
            String line = content.get(i);
            if (line.contains(signature)) {
                if (line.contains(") {")) {
                    endSignature = true;
                }
                found = true;
                continue;
            }
            if (found && !endSignature && line.contains(") {")) {
                endSignature = true;
                continue;
            }
            if (found && endSignature) {
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
