package cybersec.deception.deamon.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.ApplPropUtils;
import cybersec.deception.deamon.utils.servermanipulation.ControllerFilesUtils;
import cybersec.deception.deamon.utils.servermanipulation.PomMavenUtils;
import cybersec.deception.deamon.utils.ZipUtils;
import cybersec.deception.deamon.utils.servermanipulation.methods.MethodsGeneration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ServerBuildingService {

    @Value("${default.server.framework}")
    private String serverFramework;

    @Value("${default.server.directory}")
    private String serverDirectory;

    @Value("${default.server.specFile}")
    private String serverSpecFileLocation;

    @Value("${application.properties.location}")
    private String appPropertiesPath;

    @Value("${swagger.home}")
    private String homeControllerPath;

    @Value("${swagger.config}")
    private String swaggerUIConfigPath;

    public void buildBasicServerFromSwagger(String yamlSpecFile, String basepath){

        // creo la directory di destinazione del progetto genero (o la svuoto)
        FileUtils.checkEmptyFolder(serverDirectory);

        // converto la stringa .YAML in .JSON
        String jsonSpecString = convertYamlToJson(yamlSpecFile);

        // creo il file.yaml (temporaneo) dal testo ricevuto in input
        //FileUtils.createFile(jsonSpecFile, serverSpecFileLocation);

        // genero il codice del server all'interno della directory
        generateServerCode(jsonSpecString, serverFramework, serverDirectory);

        try {
            ZipUtils.extractAndDeleteZip(FileUtils.buildPath(serverDirectory, "generated.zip"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // configuro il pom.xml
        PomMavenUtils.configureDefaultPom();

        // Step 2: configuro il pom per usare Hibernate
        PomMavenUtils.configSwaggerApiPom();

        // Step 3: genero il file di configurazione di Hibernate
        ApplPropUtils.addApplicationPropertiesJPAconfig();

        // Modifico il path base
        FileUtils.replaceStringInFile(appPropertiesPath, "server.servlet.contextPath=/api/v3", "server.servlet.contextPath=/"+ basepath);

        // elimino il file.yaml (non più necessario)
        //FileUtils.deleteFile(serverSpecFileLocation);
    }

    private String convertYamlToJson(String yamlString) {
        // Carica YAML in una mappa
        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap = yaml.load(yamlString);

        // Crea un ObjectMapper per la conversione da mappa a JSON
        ObjectMapper jsonMapper = new ObjectMapper();

        // Converte la mappa in una stringa JSON
        try {
            return jsonMapper.writeValueAsString(yamlMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanDirectory() {
        FileUtils.deleteDirectory(serverDirectory);
    }

    public byte[] getZip() throws IOException {
        return ZipUtils.getZip(serverDirectory);
    }

    private static void generateServerCode(String jsonSpecString, String lang, String outputDir) {
        try {

            int exitCode = sendSwaggerRequest(jsonSpecString, lang, outputDir);

            if (exitCode == 0) {
                System.out.println("Code generation completed successfully!");
            } else {
                System.err.println("Code generation failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sendSwaggerRequest(String specContent, String lang, String outputDir) throws IOException, InterruptedException {
        String apiUrl = "https://generator3.swagger.io/api/generate";

        // Create the payload
        String payload = "{\n" +
                "  \"spec\": " + "\"" + specContent.replace("\"", "\\\"") + "\",\n" +
                "  \"lang\": \"" + lang + "\",\n" +
                "  \"type\": \"SERVER\",\n" +
                "  \"codegenVersion\": \"V3\"\n" +
                "}";

        // Send the request
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Get the response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream is = connection.getInputStream();
                 FileOutputStream fos = new FileOutputStream(outputDir + "/generated.zip")) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            return 0; // Success
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.err.println("Error response: " + response.toString());
            }
            return responseCode; // Failure
        }
    }

    public void removeDocs() {
        FileUtils.replaceStringInFile(homeControllerPath, "redirect:/swagger-ui/", "");
        FileUtils.replaceStringInFile(swaggerUIConfigPath, "registry.addViewController(\"/swagger-ui/\")", "//registry.addViewController(\"/swagger-ui/\")");
    }

    public void buildNotImplementedMethods() {
        for (File f: ControllerFilesUtils.getControllers()) {
            FileUtils.scriviFile(f.getAbsolutePath(), adddImportDate(MethodsGeneration.modifyNotImpl(FileUtils.leggiFile(f.getAbsolutePath()))));
        }
    }

    private List<String> adddImportDate(List<String> righe) {
        List<String> newContent = new ArrayList<>();
        for (String riga : righe) {
            newContent.add(riga);
            if (riga.contains("import javax.servlet.http.HttpServletRequest;")) {
                newContent.add("import java.util.Date;\n");
            }

        }
        return newContent;
    }
}
