package cybersec.deception.deamon.services;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class EntitiesManipulationService {

    @Value("${entities.dir.location}")
    private String entitiesDirectory;

    @Value("${entities.number}")
    private int numberOfEntity;

    // CRUD methods for entities

    public String retrieve(String filePath){

        filePath = FileUtils.validateJsonFileName(filePath);
        assert filePath != null;

        File file = new File(filePath);
        if (file.exists()) {
            List<String> content = FileUtils.leggiFile(file.getAbsolutePath());
            return getRandomEntity(content);
        } else {
            System.err.println("Il file non esiste o il percorso non è valido.");
        }

        return null;
    }

    public Map<String, String> retrieveAll(){
        Map<String, String> fileContentsMap;

        File[] files = FileUtils.getFilesFilteredByExtension(entitiesDirectory, ".json");
        fileContentsMap = readJsonContent(files);

        return fileContentsMap;
    }

    private Map<String, String> readJsonContent(File[] files){
        Map<String, String> fileContentsMap = new HashMap<>();
        if (!Utils.isNullOrEmpty(files)) {
            for (File file : files) {
                String fileName = file.getName();
                String key = fileName.substring(0, fileName.lastIndexOf('.'));
                List<String> content = FileUtils.leggiFile(file.getAbsolutePath());

                String json = getRandomEntity(content);

                fileContentsMap.put(key, json);
            }
        }
        return fileContentsMap;
    }

    public String getRandomEntityByName(String entityName) {
        File[] files = FileUtils.getFilesFilteredByExtension(entitiesDirectory, ".json");
        if (files != null) {
            for (File f: files) {
                if (f.getName().replace(".json","").equals(entityName)) {
                    return getRandomEntity(FileUtils.leggiFile(f.getAbsolutePath()));
                }
            }

        }
        return "";
    }

    public String getMultiRandomEntityByName(String entityName) {
        File[] files = FileUtils.getFilesFilteredByExtension(entitiesDirectory, ".json");
        if (files != null) {
            for (File f: files) {
                if (f.getName().replace(".json","").equals(entityName)) {
                    Random random = new Random();
                    StringBuilder sb =  new StringBuilder("[ \n");

                    for (int i = 0; i < random.nextInt(5, 50); i++) {
                        sb.append(getRandomEntity(FileUtils.leggiFile(f.getAbsolutePath()))).append(",\n");
                    }
                    return sb.toString();
                }
            }

        }
        return "";
    }

    public String getRandomEntity(List<String> content) {
        StringBuilder sb = new StringBuilder();
        int entity = new Random().nextInt(numberOfEntity);
        boolean found = false;
        int elementsnum = 0;
        for (String line : content) {

            // posso fare questo controllo perchè i miei oggetti non hanno altri oggeti innestati
            if (line.trim().equals("{")) {
                if (!found && elementsnum == entity) {
                    found = true;
                }
                elementsnum++;
            }

            if (found) {
                if (line.trim().equals("}") || line.trim().equals("},")) {
                    found = false;
                    if (line.trim().equals("},")) {
                        line = line.replace(",", "");
                    }
                }
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
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
