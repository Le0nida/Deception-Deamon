package cybersec.deception.deamon.services;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.PomMavenUtils;
import cybersec.deception.deamon.utils.ZipUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

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

        // creo il file.yaml (temporaneo) dal testo ricevuto in input
        FileUtils.createFile(yamlSpecFile, serverSpecFileLocation);

        // genero il codice del server all'interno della directory
        generateServerCode(serverSpecFileLocation, serverFramework, serverDirectory);

        // configuro il pom.xml
        PomMavenUtils.configureDefaultPom();

        // Modifico il path base
        FileUtils.replaceStringInFile(appPropertiesPath, "server.servlet.contextPath=/api/v3", "server.servlet.contextPath=/"+ basepath);

        // elimino il file.yaml (non più necessario)
        FileUtils.deleteFile(serverSpecFileLocation);
    }

    public void cleanDirectory() {
        FileUtils.deleteDirectory(serverDirectory);
    }

    public byte[] getZip() throws IOException {
        return ZipUtils.getZip(serverDirectory);
    }

    private void generateServerCode(String specPath, String lang, String outputDir) {
        try {
            // Utilizza java -jar per eseguire swagger-codegen-cli
            String command = "java -jar " + System.getProperty("user.home") + "/.m2/repository/io/swagger/codegen/v3/swagger-codegen-cli/3.0.29/swagger-codegen-cli-3.0.29.jar";
            int exitCode = getExitCode(command, "generate", "-i", specPath, "-l", lang, "-o", outputDir);

            if (exitCode == 0) {
                System.out.println("Code generation completed successfully!");
            } else {
                System.err.println("Code generation failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int getExitCode(String command, String... args) throws InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd.exe", "/c", command, args[0], args[1], args[2], args[3], args[4], args[5], args[6]
        );

        processBuilder.inheritIO();
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return process.waitFor();
    }

    public void removeDocs() {
        FileUtils.replaceStringInFile(homeControllerPath, "redirect:/swagger-ui/)", "");
        FileUtils.replaceStringInFile(swaggerUIConfigPath, "registry.addViewController(\"/swagger-ui/\")", "//registry.addViewController(\"/swagger-ui/\")");
    }
}
