package cybersec.deception.deamon.utils.servermanipulation;

import cybersec.deception.deamon.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.*;

public class NotImplFileUtils {

    private static String apiFolder;

    @Value("${folder.api}")
    public void setDriverClass(String apiFolder) {
        NotImplFileUtils.apiFolder = apiFolder;
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

}
