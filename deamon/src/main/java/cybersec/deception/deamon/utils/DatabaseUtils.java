package cybersec.deception.deamon.utils;

import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class DatabaseUtils {

    private static String driverClass;
    private static String url;
    private static String username;
    private static String password;
    private static String dbname;

    @Value("${database.driverclass}")
    public void setDriverClass(String driverClass) {
        DatabaseUtils.driverClass = driverClass;
    }

    @Value("${database.url}")
    public void setUrl(String url) {
        DatabaseUtils.url = url;
    }

    @Value("${database.username}")
    public void setUsername(String username) {
        DatabaseUtils.username = username;
    }

    @Value("${database.password}")
    public void setPassword(String password) {
        DatabaseUtils.password = password;
    }

    @Value("${database.name}")
    public void setDbname(String dbname) {
        DatabaseUtils.dbname = dbname;
    }

    public static void createDatabaseAndTable(Map<String, List<String>> entities) {
        Connection connection = null;
        Statement statement = null;

        try {
            // Carica il driver JDBC
            Class.forName(driverClass);

            // Connessione al database (senza specificare un database)
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();

            // Crea il database
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
            System.out.println("Database creato con successo");

            // Seleziona il database
            statement.executeUpdate("USE " + dbname);

            // Modifico lo sciprt .sql in base alle entità e poi lo eseguo
            Statement finalStatement = statement;
            entities.forEach((key, value) -> {
                    String outputFilePath = SQLFilesUtils.getUpdatedSqlFile(key, value);
                    executeSqlScript(finalStatement, outputFilePath);
                    FileUtils.deleteFile(outputFilePath);
                }
            );

            System.out.println("Dati popolati con successo");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void executeSqlScript(Statement statement, String filePath) {
        try {

            // Leggi il file SQL
            StringBuilder scriptContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while (true) {
                    try {
                        if (!((line = reader.readLine()) != null)) break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    scriptContent.append(line).append("\n");
                }
            }

            // Esegui lo script
            String[] sqlCommands = scriptContent.toString().split(";");
            for (String command : sqlCommands) {
                if (!command.trim().isEmpty()) {
                    statement.addBatch(command);
                }
            }

            System.out.println("Script SQL eseguito con successo.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
