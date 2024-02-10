package cybersec.deception.deamon.services;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.ControllerFilesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiUtilsService {

    @Value("${apiutils.source.path}")
    private String apiUtilsSourcePath;

    @Value("${apiutils.destination.path}")
    private String apiUtilsDestinationPath;

    public void addApiUtils(){

        // aggiungo il file ApiUtils.java per generare delay ed errori dei metodi
        FileUtils.copyFile(apiUtilsSourcePath, apiUtilsDestinationPath);

        List<File> controllers = ControllerFilesUtils.getControllers();
        for (File f: controllers) {
            addApiUtilsCode(f.getPath());
        }
    }

    private void addApiUtilsCode(String nomeFile) {
        List<String> righe = FileUtils.leggiFile(nomeFile);
        List<String> newContent = new ArrayList<>();

        boolean foundMethod = false;
        boolean apiUtils = false;
        for (String riga : righe) {

            if (!apiUtils && riga.contains("private final HttpServletRequest request;")) {
                newContent.add("\n    @Autowired");
                newContent.add("    private ApiUtils apiUtils;\n");
                apiUtils = true;
            }

            newContent.add(riga);
            if (riga.contains("public ResponseEntity<")) {
                foundMethod = true;
            }

            if (riga.contains("{") && foundMethod) {
                newContent.add( "       apiUtils.simulateRandomDelay();\n" +
                                "       if (apiUtils.shouldThrowException()) {\n" +
                                "           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                                "       }\n\n");
                foundMethod = false;
            }
        }

        FileUtils.scriviFile(nomeFile, newContent);
        System.out.println("Modifiche applicate con successo.");
    }
}
