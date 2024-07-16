package cybersec.deception.deamon.utils.database;

import cybersec.deception.deamon.services.MockarooService;
import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

@Component
public class SQLFilesUtils {

    public static MockarooService mockarooService;
    public static String sqlfilesDirectory;
    @Autowired
    public SQLFilesUtils(MockarooService mockarooService) {
        SQLFilesUtils.mockarooService = mockarooService;
    }

    @Value("${sqlfiles.dir.location}")
    public void setSqlfilesDirectory(String sqlfilesDirectory) {
        SQLFilesUtils.sqlfilesDirectory = sqlfilesDirectory;
    }

    public static String generateSQLFileMockaroo(String inputFileName, String requestBody) {

        String inputFilePath = FileUtils.buildPath(sqlfilesDirectory, inputFileName + "Updated.sql");

        HttpRequest request = mockarooService.buildSQLRequest(requestBody);
        String result = mockarooService.generateData(request);
        result = result.replace("insert into  ", "insert into " + inputFileName + " ");

        String createTable = mockarooService.createTable(inputFileName, requestBody);
        FileUtils.scriviFile(inputFilePath, createTable + "\n\n" + result);

        if (FileUtils.existsFile(inputFilePath)) {
            return inputFilePath;
        }
        else {
            System.out.println("Il file " + inputFilePath + " non esiste");
        }
        return null;
    }

    public static String getUpdatedSqlFile(String inputFileName, List<String> selectedAttributes) {

        String inputFilePath = FileUtils.buildPath(sqlfilesDirectory, inputFileName + ".sql");

        if (FileUtils.existsFile(inputFilePath)) {
            String outputFilePath = inputFilePath.replace(".sql", "Updated.sql");

            modifySQLFile(inputFilePath, outputFilePath, selectedAttributes);

            return outputFilePath;
        }
        else {
            System.out.println("Il file " + inputFilePath + " non esiste");
        }
        return null;
    }

    private static boolean containsAttribute(String s, List<String> attributesOfInterest) {
        for (String attr: attributesOfInterest) {
            if (s.contains(" " + attr + " ") || s.contains("\t" + attr + " ")) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsIndex(int index, List<Integer> indexToRemove) {
        for (int i: indexToRemove) {
            if (i == index) {
                return true;
            }
        }
        return false;
    }

    private static boolean equalsAttribute(String s, List<String> attributesOfInterest) {
        for (String attr: attributesOfInterest) {
            if (s.equals(attr)) {
                return true;
            }
        }
        return false;
    }

    private static String[] extractAttributesFromInsertStatement(String insertStatement) {
        // Trova la sottostringa tra parentesi tonde che contiene i valori
        int startIndex = insertStatement.indexOf("(");
        int endIndex = insertStatement.indexOf(")");
        String valuesString = insertStatement.substring(startIndex + 1, endIndex);

        // Separa i valori usando la virgola come delimitatore e rimuove eventuali spazi
        String[] values = valuesString.split(",");
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return values;
    }

    private static String[] extractValuesFromInsertStatement(String insertStatement) {
        // Trova la sottostringa tra parentesi tonde che contiene i valori
        int startIndex = insertStatement.indexOf("(");
        int endIndex = insertStatement.lastIndexOf(")");
        String valuesString = insertStatement.substring(startIndex + 1, endIndex);

        // Separa i valori usando la virgola come delimitatore e rimuove eventuali spazi
        String[] values = valuesString.split(",");
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return values;
    }

    private static void modifySQLFile(String inputFile, String outputFile, List<String> attributesOfInterest) {
        List<String> list = FileUtils.leggiFile(inputFile);

        List<String> output = new ArrayList<>();

        for (String s: list) {
            if (s.startsWith("insert into")) {
                String initialString = s.substring(0, s.indexOf("(") + 1);
                String[] singoliAttributi = extractAttributesFromInsertStatement(s);

                List<Integer> toRemoveIndex = new ArrayList<>();
                int i = 0;
                for (String attr: singoliAttributi) {
                    if (equalsAttribute(attr, attributesOfInterest)) {
                        initialString += attr + ", ";
                    }
                    else {
                        toRemoveIndex.add(i);
                    }
                    i++;
                }
                initialString = initialString.substring(0, initialString.lastIndexOf(", ")) + ") values (";

                String[] singoliValori = extractValuesFromInsertStatement(s.substring(s.indexOf(") values")));
                int k = 0;
                for (String val: singoliValori) {
                    if (!containsIndex(k, toRemoveIndex)){
                        initialString += val + ", ";
                    }
                    k++;
                }
                initialString = initialString.substring(0, initialString.lastIndexOf(", ")) + ");";

                output.add(initialString);
            }
            else {
                if (s.contains("create table") || s.contains("PRIMARY KEY") || s.contains(");")) {
                    output.add(s);
                    continue;
                }
                if (containsAttribute(s, attributesOfInterest)) {
                    output.add(s);
                }
            }
        }

        FileUtils.scriviFile(outputFile, output);
    }

    private static void processSQLFile(String inputFilePath, String outputFilePath, List<String> selectedAttributes) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            boolean createTableStatementFound = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("create table")) {
                    createTableStatementFound = true;
                    writer.write(line);
                    writer.newLine();
                } else if (createTableStatementFound && line.trim().startsWith("(")) {
                    // Found the start of column definitions
                    List<String> columns = extractColumns(line);
                    List<String> selectedColumns = getSelectedColumns(columns, selectedAttributes);

                    // Generate the new create table statement
                    writer.write("(\n\t" + String.join(",\n\t", selectedColumns));
                    writer.newLine();
                } else if (createTableStatementFound && line.trim().startsWith(")")) {
                    // Found the end of column definitions
                    writer.write(line);
                    writer.newLine();
                    break;
                } else if (createTableStatementFound) {
                    // Ignore other lines within the create table block
                    writer.write(line);
                    writer.newLine();
                }
            }

            // Process insert statements
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("insert into")) {
                    List<String> values = extractValues(line);
                    List<String> selectedValues = getSelectedValues(values, selectedAttributes);

                    // Generate the new insert statement
                    writer.write("insert into " + getTableName(line) + " (" + String.join(", ", selectedAttributes) + ") values (" + String.join(", ", selectedValues) + ");");
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> extractColumns(String line) {
        String columnsString = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")).trim();
        return List.of(columnsString.split(","));
    }

    private static List<String> getSelectedColumns(List<String> columns, List<String> selectedAttributes) {
        List<String> selectedColumns = new ArrayList<>();
        for (String column : columns) {
            String columnName = column.trim().split("\\s")[0];
            if (selectedAttributes.contains(columnName)) {
                selectedColumns.add(column.trim());
            }
        }
        return selectedColumns;
    }

    private static List<String> extractValues(String line) {
        String valuesString = line.substring(line.indexOf("values") + 6, line.lastIndexOf(")")).trim();
        return List.of(valuesString.split(","));
    }

    private static List<String> getSelectedValues(List<String> values, List<String> selectedAttributes) {
        List<String> selectedValues = new ArrayList<>();
        for (String value : values) {
            selectedValues.add(value.trim());
        }
        return selectedValues;
    }

    private static String getTableName(String line) {
        String[] tokens = line.trim().split(" ");
        return tokens[2]; // Assumes that the table name is the third token in the "insert into" statement
    }
}
