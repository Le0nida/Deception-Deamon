package cybersec.deception.deamon.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Component
public class SQLFilesUtils {

    public static String sqlfilesDirectory;

    @Value("${sqlfiles.dir.location}")
    public void setSqlfilesDirectory(String sqlfilesDirectory) {
        SQLFilesUtils.sqlfilesDirectory = sqlfilesDirectory;
    }

    public static String getUpdatedSqlFile(String inputFileName, List<String> selectedAttributes) {

        String inputFilePath = FileUtils.buildPath(sqlfilesDirectory, inputFileName + ".sql");

        if (FileUtils.existsFile(inputFilePath)) {
            String outputFilePath = inputFilePath.replace(".sql", "Updated.sql");

            processSQLFile(inputFilePath, outputFilePath, selectedAttributes);

            return outputFilePath;
        }
        else {
            System.out.println("Il file " + inputFilePath + " non esiste");
        }
        return null;
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
