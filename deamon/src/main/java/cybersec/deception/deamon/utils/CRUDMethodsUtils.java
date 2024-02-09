package cybersec.deception.deamon.utils;

import org.springframework.stereotype.Component;

@Component
public class CRUDMethodsUtils {

    public static String getCreateMethod(String entityName) {
        return "    Session session = HibernateUtil.getSessionFactory().openSession();\n" +
                "    Transaction transaction = null;\n" +
                "    try {\n" +
                "        transaction = session.beginTransaction();\n" +
                "        session.save(body);\n" +
                "        transaction.commit();\n" +
                "        return new ResponseEntity<>(body, HttpStatus.CREATED);\n" +
                "    } catch (Exception e) {\n" +
                "        if (transaction != null) {\n" +
                "            transaction.rollback();\n" +
                "        }\n" +
                "        log.error(\"Error creating " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    } finally {\n" +
                "        session.close();\n" +
                "    }\n";
    }

    public static String getDeleteMethod(String entityName) {
        return "    Session session = HibernateUtil.getSessionFactory().openSession();\n" +
                "    Transaction transaction = null;\n" +
                "    try {\n" +
                "        transaction = session.beginTransaction();\n" +
                "        " + entityName + " " + entityName.toLowerCase() + " = session.get(" + entityName + ".class, " + entityName.toLowerCase() + "name);\n" +
                "        if (" + entityName.toLowerCase() + " != null) {\n" +
                "            session.delete(" + entityName.toLowerCase() + ");\n" +
                "            transaction.commit();\n" +
                "            return new ResponseEntity<>(HttpStatus.NO_CONTENT);\n" +
                "        } else {\n" +
                "            return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "        }\n" +
                "    } catch (Exception e) {\n" +
                "        if (transaction != null) {\n" +
                "            transaction.rollback();\n" +
                "        }\n" +
                "        log.error(\"Error deleting " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    } finally {\n" +
                "        session.close();\n" +
                "    }\n";
    }

    public static String getUpdateMethod(String entityName) {
        return "    Session session = HibernateUtil.getSessionFactory().openSession();\n" +
                "    Transaction transaction = null;\n" +
                "    try {\n" +
                "        transaction = session.beginTransaction();\n" +
                "        " + entityName + " existing" + entityName + " = session.get(" + entityName + ".class, " + entityName.toLowerCase() + "name);\n" +
                "        if (existing" + entityName + " != null) {\n" +
                "            // Aggiorna le proprietà dell'utente esistente con i nuovi valori\n" +
                "            existing" + entityName + ".setFirstName(body.getFirstName());\n" +
                "            existing" + entityName + ".setLastName(body.getLastName());\n" +
                "            existing" + entityName + ".setEmail(body.getEmail());\n" +
                "            existing" + entityName + ".setPassword(body.getPassword());\n" +
                "            existing" + entityName + ".setPhone(body.getPhone());\n" +
                "            existing" + entityName + ".set" + entityName + "Status(body.get" + entityName + "Status());\n" +
                "            session.update(existing" + entityName + ");\n" +
                "            transaction.commit();\n" +
                "            return new ResponseEntity<>(HttpStatus.NO_CONTENT);\n" +
                "        } else {\n" +
                "            return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "        }\n" +
                "    } catch (Exception e) {\n" +
                "        if (transaction != null) {\n" +
                "            transaction.rollback();\n" +
                "        }\n" +
                "        log.error(\"Error updating " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    } finally {\n" +
                "        session.close();\n" +
                "    }\n";
    }

    public static String getRetrieveMethod(String entityName) {
        return "    Session session = HibernateUtil.getSessionFactory().openSession();\n" +
                "    try {\n" +
                "        " + entityName + " " + entityName.toLowerCase() + " = session.get(" + entityName + ".class, " + entityName.toLowerCase() + "name);\n" +
                "        if (" + entityName.toLowerCase() + " != null) {\n" +
                "            return new ResponseEntity<>(" + entityName.toLowerCase() + ", HttpStatus.OK);\n" +
                "        } else {\n" +
                "            return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "        }\n" +
                "    } catch (Exception e) {\n" +
                "        log.error(\"Error retrieving " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    } finally {\n" +
                "        session.close();\n" +
                "    }\n";
    }

    
}
