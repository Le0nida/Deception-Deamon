package cybersec.deception.deamon.services;
import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.PomMavenUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ServerBuildingService {

    @Value("${default.server.framework}")
    private String serverFramework;

    @Value("${default.server.directory}")
    private String serverDirectory;

    @Value("${default.server.specFile}")
    private String serverSpecFileLocation;

    @Value("${pom.location}")
    private String pomPath;

    @Value("${application.properties.location}")
    private String appPropertiesPath;


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

    public byte[] getZip() throws IOException {
        File sourceDirectory = new File(serverDirectory);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            zipFile(sourceDirectory, sourceDirectory.getName(), zos);

            zos.finish();
            zos.flush();

            byte[] zipBytes = baos.toByteArray();

            // Verifica la validità del file ZIP
            if (isZipValid(zipBytes)) {
                return zipBytes;
            } else {
                throw new IOException("Il file ZIP non è valido.");
            }
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOutputStream) throws IOException {
        if (fileToZip.isDirectory()) {
            for (File file : Objects.requireNonNull(fileToZip.listFiles())) {
                zipFile(file, fileName + "/" + file.getName(), zipOutputStream);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, bytesRead);
                }
                zipOutputStream.closeEntry();
            }
        }
    }

    private static boolean isZipValid(byte[] zipBytes) throws IOException {
        try (InputStream is = new ByteArrayInputStream(zipBytes);
             ZipArchiveInputStream zis = new ZipArchiveInputStream(is)) {
            return zis.getNextEntry() != null;
        }
    }

    private static void generateServerCode(String specPath, String lang, String outputDir) {
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
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int exitCode = process.waitFor();
        return exitCode;
    }

    public void cleanDirectory() {
        FileUtils.deleteDirectory(serverDirectory);
    }
}
