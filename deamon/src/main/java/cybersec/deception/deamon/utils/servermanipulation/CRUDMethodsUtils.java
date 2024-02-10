package cybersec.deception.deamon.utils.servermanipulation;

import org.springframework.stereotype.Component;

@Component
public class CRUDMethodsUtils {

    public static String getCreateMethod(String entityName) {
        return
                "String accept = request.getHeader(\"Accept\");\n" +
                "if (accept != null && accept.equals(\"application/json\")) {\n" +
                "    try {\n" +
                "        " + entityName.toLowerCase() + "Repository.save(body);\n" +
                "        return new ResponseEntity<>(body, HttpStatus.CREATED);\n" +
                "    } catch (Exception e) {\n" +
                "        log.error(\"Error creating " + entityName + "\", e);\n" +
                "        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);\n" +
                "    }\n" +
                "}\n" +
                "return new ResponseEntity<>(HttpStatus.BAD_REQUEST);";
    }


    public static String getDeleteMethod(String entityName) {
        return
                "if (" + entityName.toLowerCase() + "Id != null && !equals(\"\")) {\n" +
                "    try {\n" +
                "        " + entityName + " " + entityName.toLowerCase() + " = " + entityName.toLowerCase() + "Repository.findById(" + entityName.toLowerCase() + "Id).orElse(null);\n" +
                "        if (" + entityName.toLowerCase() + " != null) {\n" +
                "            " + entityName.toLowerCase() + "Repository.deleteById(" + entityName.toLowerCase() + "Id);\n" +
                "            return new ResponseEntity<>(HttpStatus.NO_CONTENT);\n" +
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


    public static String getUpdateMethod(String entityName) {
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


    public static String getRetrieveMethod(String entityName) {
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


    public static String getLoginUserMethod() {
        return
                "String accept = request.getHeader(\"Accept\");\n" +
                "if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {\n" +
                "    if (userRepository.existsByUsername(username)) {\n" +
                        "            return new ResponseEntity<>(\"Invalid username\", HttpStatus.OK);\n" +
                        "        }\n" +
                        "        // Non utilizzare valori casuali, ma valori derivati dalla password\n" +
                        "        else if (password.contains(\"ì\") && password.contains(\"°\") && password.contains(\"e\") && password.contains(\"\\\"\") && password.contains(\"*\")) {\n" +
                        "            return new ResponseEntity<>(\"Access Denied\", HttpStatus.UNAUTHORIZED);\n" +
                        "        } else if (password.contains(\"ì%\") && password.contains(\"g°\") && password.contains(\"er\") && password.contains(\"ù\\\"\") && password.contains(\"?*\")) {\n" +
                        "            return new ResponseEntity<>(\"Forbidden\", HttpStatus.FORBIDDEN);\n" +
                        "        } else {\n" +
                        "            return new ResponseEntity<>(\"Wrong Password\", HttpStatus.OK);\n" +
                        "        }\n" +
                "}\n" +
                "return new ResponseEntity<>(HttpStatus.BAD_REQUEST);";
    }
    public static String getLogoutUserMethod() {
        return
                "return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);";
    }







}
