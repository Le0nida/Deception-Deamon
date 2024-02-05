package cybersec.deception.deamon.services;

import cybersec.deception.deamon.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;
@Service
public class EntitiesManipulationService {

    @Value("${entities.dir.location}")
    private String entitiesDirectory;


    // CRUD methods for entities

    public String retrieve(String fileName){

        fileName = FileUtils.validateJsonFileName(fileName);
        assert fileName != null;

        File file = new File(fileName);
        if (file.exists()) {
            return FileUtils.readFileContent(file.getAbsolutePath());
        } else {
            System.err.println("Il file non esiste o il percorso non è valido.");
        }

        return null;
    }

    public Map<String, String> retrieveAll(){
        Map<String, String> fileContentsMap = new HashMap<>();

        File[] files = FileUtils.getFilesFilteredByExtension(entitiesDirectory, ".json");
        fileContentsMap = FileUtils.readFilesContent(files);

        return fileContentsMap;
    }

    public boolean create(String fileName, String jsonString) {
        fileName = FileUtils.validateJsonFileName(fileName);
        assert fileName != null;

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String filePath = FileUtils.buildPath(entitiesDirectory, fileName);

            try (FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write(jsonObject.toString(4)); // Indentazione con 4 spazi
                System.out.println("File JSON creato con successo: " + filePath);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Errore durante la creazione del file JSON: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String fileName) {
        fileName = FileUtils.validateJsonFileName(fileName);
        assert fileName != null;

        String filePath = FileUtils.buildPath(entitiesDirectory, fileName);
        File file = new File(filePath);

        if (file.exists() && file.isFile()) {
            return file.delete();
        } else {
            System.err.println("Il file non esiste o non è un file: " + filePath);
            return false;
        }
    }

    public boolean update(String fileName, String jsonString) {
        return delete(fileName) && create(fileName, jsonString);
    }



}
