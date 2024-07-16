package cybersec.deception.deamon.utils.database;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseUtils {

    public static String driverClass;
    public static String url;
    public static String username;
    public static String password;
    public static String dbname;

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

    public static void createDatabaseAndTable(Map<String, List<String>> entities, String tableCode, Map<String, String> mockarooRequestsMap) {
        Statement statement = null;

        try {
            statement = getStatement();

            // Modifico lo script .sql in base alle entità e poi lo eseguo
            for (String fileName : entities.keySet()) {

                String outputFilePath = SQLFilesUtils.getUpdatedSqlFile(fileName, entities.get(fileName));
                if (Utils.isNullOrEmpty(outputFilePath) && mockarooRequestsMap.get("request"+fileName) != null) {
                    outputFilePath = SQLFilesUtils.generateSQLFileMockaroo(fileName, mockarooRequestsMap.get("request"+fileName));
                }
                if (!Utils.isNullOrEmpty(outputFilePath)) {
                    FileUtils.replaceStringInFile(outputFilePath, " " + fileName + " ", " " + tableCode + "_" + fileName.toLowerCase() + " ");
                    executeSql(outputFilePath, statement);
                    FileUtils.deleteFile(outputFilePath);
                }
            }


            System.out.println("Dati popolati con successo");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    if (statement.getConnection() != null) {
                        statement.getConnection().close();
                    }
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static Statement getStatement() throws SQLException, ClassNotFoundException {
        // Carica il driver JDBC
        Class.forName(driverClass);

        // Connessione al database (senza specificare un database)
        Connection connection = DriverManager.getConnection(url, username, password);
        Statement statement = connection.createStatement();

        // Crea il database
        statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
        System.out.println("Database creato con successo");

        // Seleziona il database
        statement.executeUpdate("USE " + dbname);

        return statement;
    }

    public static void executeSqlFile(String filePath) {
        Statement statement = null;

        try {
            statement = getStatement();

            executeSql(filePath, statement);

            System.out.println("Sql file " + filePath + " executed");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    if (statement.getConnection() != null) {
                        statement.getConnection().close();
                    }
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void executeSql(String filePath, Statement statement) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder statementBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                // Ignora le righe vuote e i commenti
                if (!line.trim().isEmpty() && !line.trim().startsWith("--")) {
                    statementBuilder.append(line.trim());

                    // Se la riga contiene un punto e virgola, esegui la query
                    if (line.trim().endsWith(";")) {
                        String sqlStatement = statementBuilder.toString();
                        statement.executeUpdate(sqlStatement);
                        statementBuilder.setLength(0); // Resetta il builder per la prossima query
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
