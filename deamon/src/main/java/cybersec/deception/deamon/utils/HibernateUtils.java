package cybersec.deception.deamon.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HibernateUtils {
    public static String generateHibernateUtilContent() {
        return """
                package io.swagger.api;
                
                import org.hibernate.SessionFactory;
                import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
                import org.hibernate.cfg.Configuration;

                public class HibernateUtil {
                    private static final SessionFactory sessionFactory = buildSessionFactory();

                    private static SessionFactory buildSessionFactory() {
                        try {
                            Configuration configuration = new Configuration().configure();
                            return configuration.buildSessionFactory(
                                new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build()
                            );
                        } catch (Exception e) {
                            System.err.println("Initial SessionFactory creation failed." + e);
                            throw new ExceptionInInitializerError(e);
                        }
                    }

                    public static SessionFactory getSessionFactory() {
                        return sessionFactory;
                    }

                    public static void shutdown() {
                        getSessionFactory().close();
                    }
                }
                """;
    }

    public static void generateHibernateConfig(String configFile, String driverClass, String url,
                                         String username, String password,
                                         File folder) {

        List<String> entityClassPaths = new ArrayList<>();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            entityClassPaths.add(file.getAbsolutePath());
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            writer.write("<!DOCTYPE hibernate-configuration PUBLIC\n");
            writer.write("        \"-//Hibernate/Hibernate Configuration DTD 3.0//EN\"\n");
            writer.write("        \"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">\n");
            writer.write("<hibernate-configuration>\n");
            writer.write("\n");
            writer.write("    <session-factory>\n");
            writer.write("        <!-- JDBC Database connection settings -->\n");
            writer.write("        <property name=\"hibernate.connection.driver_class\">" + driverClass + "</property>\n");
            writer.write("        <property name=\"hibernate.connection.url\">" + url + "</property>\n");
            writer.write("        <property name=\"hibernate.connection.username\">" + username + "</property>\n");
            writer.write("        <property name=\"hibernate.connection.password\">" + password + "</property>\n");
            writer.write("\n");
            writer.write("        <!-- Specify dialect -->\n");
            //writer.write("        <property name=\"hibernate.dialect\">" + dialect + "</property>\n");
            writer.write("\n");
            writer.write("        <!-- Echo all executed SQL to stdout -->\n");
            writer.write("        <property name=\"hibernate.show_sql\">true</property>\n");
            writer.write("\n");
            writer.write("        <!-- Drop and re-create the database schema on startup -->\n");
            writer.write("        <property name=\"hibernate.hbm2ddl.auto\">update</property>\n");
            writer.write("\n");
            // Mapping entity classes
            for (String entityClassPath : entityClassPaths) {
                entityClassPath = entityClassPath.substring(entityClassPath.lastIndexOf("\\main\\")+1);
                writer.write("        <mapping class=\"" + entityClassPath + "\"/>\n");
            }
            writer.write("\n");
            writer.write("    </session-factory>\n");
            writer.write("</hibernate-configuration>\n");

            System.out.println("Hibernate configuration file generated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
