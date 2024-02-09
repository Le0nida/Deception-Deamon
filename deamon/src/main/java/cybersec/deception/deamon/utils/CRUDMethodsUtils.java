package cybersec.deception.deamon.utils;

import org.springframework.stereotype.Component;

@Component
public class CRUDMethodsUtils {

    public static String getCreateMethod(String entityName) {
        return  "       try {\n" +
                "           " + entityName.toLowerCase() + "Repository.save(body);\n" +
                "           return new ResponseEntity<>(body, HttpStatus.CREATED);\n" +
                "       } catch (Exception e) {\n" +
                "           log.error(\"Error creating " + entityName + "\", e);\n" +
                "           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "       }\n";
    }

    public static String getDeleteMethod(String entityName) {
        return  "       try {\n" +
                "           " + entityName + " " + entityName.toLowerCase() + " = " + entityName.toLowerCase() + "Repository.findById(" + entityName.toLowerCase() + "Id).orElse(null);\n" +
                "           if (" + entityName.toLowerCase() + " != null) {\n" +
                "               " + entityName.toLowerCase() + "Repository.deleteById(" + entityName.toLowerCase() + "Id);\n" +
                "               return new ResponseEntity<>(HttpStatus.NO_CONTENT);\n" +
                "           } else {\n" +
                "               return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "           }\n" +
                "       } catch (Exception e) {\n" +
                "           log.error(\"Error deleting " + entityName + "\", e);\n" +
                "           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "       }\n";
    }

    public static String getUpdateMethod(String entityName) {
        return  "       try {\n" +
                "           " + entityName + " existing" + entityName + " = " + entityName.toLowerCase() + "Repository.findById(body.getId()).orElse(null);\n" +
                "           if (existing" + entityName + " != null) {\n" +
                "               existing" + entityName + " = body;\n" +
                "               " + entityName.toLowerCase() + "Repository.save(body);\n" +
                "               return new ResponseEntity<>(body, HttpStatus.OK);\n" +
                "           } else {\n" +
                "               return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "           }\n" +
                "       } catch (Exception e) {\n" +
                "           log.error(\"Error updating " + entityName + "\", e);\n" +
                "           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "       }\n";
    }

    public static String getRetrieveMethod(String entityName) {
        return  "       try {\n" +
                "           " + entityName + " " + entityName.toLowerCase() + " = " + entityName.toLowerCase() + "Repository.findById(" + entityName.toLowerCase() + "Id).orElse(null);\n" +
                "           if (" + entityName.toLowerCase() + " != null) {\n" +
                "               return new ResponseEntity<>(" + entityName.toLowerCase() + ", HttpStatus.OK);\n" +
                "           } else {\n" +
                "               return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "           }\n" +
                "       } catch (Exception e) {\n" +
                "           log.error(\"Error retrieving " + entityName + "\", e);\n" +
                "           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "       }\n";
    }

    
}
