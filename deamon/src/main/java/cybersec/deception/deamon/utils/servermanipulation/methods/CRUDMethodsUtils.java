package cybersec.deception.deamon.utils.servermanipulation.methods;

import org.springframework.stereotype.Component;

@Component
public class CRUDMethodsUtils {

    public static String getJPACreateMethod(String entityName) {
        return
                "String accept = request.getHeader(\"Accept\");\n" +
                "if (accept != null && accept.equals(\"application/json\") && body != null) {\n" +
                "    try {\n" +
                "       if (body.getId() != null && " + entityName.toLowerCase() + "Repository.existsById(body.getId())) {\n" +
                "           return new ResponseEntity<>(" + entityName.toLowerCase() + "Repository.getOne(body.getId()), HttpStatus.CONFLICT);\n" +
                "       }\n" +
                "        " + entityName.toLowerCase() + "Repository.save(body);\n" +
                "        return new ResponseEntity<>(body, HttpStatus.CREATED);\n" +
                "    } catch (Exception e) {\n" +
                "        log.error(\"Error creating " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    }\n" +
                "}\n" +
                "return new ResponseEntity<>(HttpStatus.BAD_REQUEST);";
    }


    public static String getJPADeleteMethod(String entityName) {
        return
                "if (" + entityName.toLowerCase() + "Id != null && !equals(\"\")) {\n" +
                "    try {\n" +
                "        " + entityName + " " + entityName.toLowerCase() + " = " + entityName.toLowerCase() + "Repository.findById(" + entityName.toLowerCase() + "Id).orElse(null);\n" +
                "        if (" + entityName.toLowerCase() + " != null) {\n" +
                "            " + entityName.toLowerCase() + "Repository.deleteById(" + entityName.toLowerCase() + "Id);\n" +
                "            return new ResponseEntity<>(HttpStatus.OK);\n" +
                "        } else {\n" +
                "            return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "        }\n" +
                "    } catch (Exception e) {\n" +
                "        log.error(\"Error deleting " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    }\n" +
                "}\n" +
                "return new ResponseEntity<>(HttpStatus.BAD_REQUEST);";
    }


    public static String getJPAUpdateMethod(String entityName) {
        return
                "String accept = request.getHeader(\"Accept\");\n" +
                "if (accept != null && accept.equals(\"application/json\")) {\n" +
                "    try {\n" +
                "        " + entityName + " existing" + entityName + " = " + entityName.toLowerCase() + "Repository.findById(body.getId()).orElse(null);\n" +
                "        if (existing" + entityName + " != null) {\n" +
                "            existing" + entityName + " = body;\n" +
                "            " + entityName.toLowerCase() + "Repository.save(body);\n" +
                "            return new ResponseEntity<>(body, HttpStatus.OK);\n" +
                "        } else {\n" +
                "            return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "        }\n" +
                "    } catch (Exception e) {\n" +
                "        log.error(\"Error updating " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    }\n" +
                "}\n" +
                "return new ResponseEntity<>(HttpStatus.BAD_REQUEST);";
    }


    public static String getJPARetrieveMethod(String entityName) {
        return
                "if (" + entityName.toLowerCase() + "Id != null && !equals(\"\")) {\n" +
                "    try {\n" +
                "        " + entityName + " " + entityName.toLowerCase() + " = " + entityName.toLowerCase() + "Repository.findById(" + entityName.toLowerCase() + "Id).orElse(null);\n" +
                "        if (" + entityName.toLowerCase() + " != null) {\n" +
                "            return new ResponseEntity<>(" + entityName.toLowerCase() + ", HttpStatus.OK);\n" +
                "        } else {\n" +
                "            return new ResponseEntity<>(HttpStatus.NOT_FOUND);\n" +
                "        }\n" +
                "    } catch (Exception e) {\n" +
                "        log.error(\"Error retrieving " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    }\n" +
                "}\n" +
                "return new ResponseEntity<>(HttpStatus.BAD_REQUEST);";
    }

}
