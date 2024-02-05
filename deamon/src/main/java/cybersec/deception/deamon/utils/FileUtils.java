package cybersec.deception.deamon.utils;

import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import java.io.File;

@Component
public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    public static void checkEmptyFolder(String folderPath){
        if (!Utils.isNullOrEmpty(folderPath)) {
            createOrEmptyFolder(folderPath);
        }
        else {
            logger.severe("Il path della directory in input per contenere il server è vuoto");
        }
    }
    private static void createOrEmptyFolder(String folderPath) {
        File folder = new File(folderPath);

        // Verifica se la cartella esiste
        if (!folder.exists()) {
            // Se la cartella non esiste, prova a crearla
            boolean success = folder.mkdirs();
            if (!success) {
                System.err.println("Impossibile creare la cartella: " + folderPath);
                return;
            }
            System.out.println("Cartella creata: " + folderPath);
        } else {
            // Se la cartella esiste, svuotala
            emptyFolder(folder);
        }
    }

    private static void emptyFolder(File folder) {
        // Verifica se la cartella è una directory
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                // Svuota la cartella eliminando tutti i file al suo interno
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Se è una sottocartella, svuotala ricorsivamente
                        emptyFolder(file);
                    } else {
                        // Se è un file, eliminalo
                        boolean deleted = file.delete();
                        if (!deleted) {
                            System.err.println("Impossibile eliminare il file: " + file.getAbsolutePath());
                        } else {
                            System.out.println("File eliminato: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    public static File[] getFilesFilteredByExtension(String directory, String extension) {
        if (!Utils.isNullOrEmpty(directory)) {

            if (!extension.startsWith(".")){
                extension = "." + extension;
            }

            File folder = new File(directory);
            if (folder.isDirectory()) {
                String finalExtension = extension;
                return folder.listFiles((dir, name) -> name.toLowerCase().endsWith(finalExtension));
            }
        }
        return null;
    }

    public static String readFileContent(String filePath) {
        try {
            byte[] encodedBytes = Files.readAllBytes(Paths.get(filePath));
            return new String(encodedBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, String> readFilesContent(File[] files){
        Map<String, String> fileContentsMap = new HashMap<>();
        if (!Utils.isNullOrEmpty(files)) {
            for (File file : files) {
                String fileName = file.getName();
                String key = fileName.substring(0, fileName.lastIndexOf('.'));
                String content = FileUtils.readFileContent(file.getAbsolutePath());

                fileContentsMap.put(key, content);
            }
        }
        return fileContentsMap;
    }

    public static String validateJsonFileName(String fileName) {
        if (Utils.isNullOrEmpty(fileName)) {
            return null;
        }

        if (!fileName.endsWith(".json")){
            fileName = fileName + ".json";
        }
        return fileName;
    }

    public static String buildPath(String dir, String fileName) {
        return dir + File.separator + fileName;
    }

    public static void createFile(String yamlSpecFile, String serverSpecFileLocation) {
        try {
            File file = new File(serverSpecFileLocation);

            // Se il file non esiste, verrà creato
            file.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(yamlSpecFile.getBytes());
            }

        } catch (IOException e) {
            System.out.println("Si è verificato un errore durante la creazione del file.");
            e.printStackTrace();
        }
    }

    public static void deleteFile(String fileName) {
        File file = new File(fileName);

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Il file è stato eliminato con successo.");
            } else {
                System.out.println("Impossibile eliminare il file.");
            }
        } else {
            System.out.println("Il file non esiste.");
        }
    }
}
