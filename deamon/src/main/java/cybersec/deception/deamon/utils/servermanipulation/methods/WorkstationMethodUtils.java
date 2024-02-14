package cybersec.deception.deamon.utils.servermanipulation.methods;

public class WorkstationMethodUtils {

    public static String getJPAAccessWorkstationMethod() {
        return
                "if (workstation != null && password != null && !workstation.isEmpty() && !password.isEmpty()) {\n" +
                "    if (workstationRepository.existsByWorkstation(workstation)) {\n" +
                "            return new ResponseEntity<>(\"Invalid workstation\", HttpStatus.OK);\n" +
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
}
