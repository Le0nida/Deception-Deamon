package cybersec.deception.deamon.utils;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import java.util.stream.Collectors;

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

    public static void emptyFolder(File folder) {
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
    public static void replaceStringInFile(String filePath, String searchString, String replacement) {
        // Leggi tutte le linee del file e trasformale in una lista di stringhe
        Path path = Paths.get(filePath);
        List<String> lines = null;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Effettua la sostituzione della stringa in ogni linea
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // Effettua la sostituzione della stringa se presente
            lines.set(i, line.replace(searchString, replacement));
        }

        // Sovrascrivi il file con le linee modificate
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Sostituzione eseguita con successo nel file: " + filePath);
    }

    public static String readFile(String filePath) {
        try {
            return Files.lines(Paths.get(filePath)).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void scriviFile(String nomeFile, List<String> contenuto) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(nomeFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String riga : contenuto) {
            try {
                writer.write(riga + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> leggiFile(String nomeFile) {
        List<String> righe = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(nomeFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String riga;
        while (true) {
            try {
                if (!((riga = reader.readLine()) != null)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            righe.add(riga);
        }

        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return righe;
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

    public static void deleteDirectory(String dirName) {
        File directory = new File(dirName);
        deleteDirectory(directory);
    }

    private static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    public static boolean existsFile(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static void copyDirectory(String sourceDirectoryPath, String targetDirectoryPath){
        // Crea un oggetto Path per la directory di origine
        Path sourcePath = Paths.get(sourceDirectoryPath);

        // Crea un oggetto Path per la directory di destinazione
        Path targetPath = Paths.get(targetDirectoryPath);

        // Copia il contenuto della directory di origine nella directory di destinazione
        try {
            Files.walk(sourcePath)
                    .forEach(source -> {
                        try {
                            Path destination = targetPath.resolve(sourcePath.relativize(source));
                            Files.copy(source, destination);
                        } catch (IOException e) {
                            e.printStackTrace(); // Gestisci l'eccezione in base alle tue esigenze
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyFile(String sourcePath, String targetPath) {
        try {
            Files.copy(Path.of(sourcePath), Path.of(targetPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
