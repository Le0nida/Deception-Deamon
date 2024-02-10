package cybersec.deception.deamon.utils.servermanipulation.methods;

public class UserMethodsUtils {

    public static String getJPALoginUserMethod() {
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
    public static String getJPALogoutUserMethod() {
        return
                "return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);";
    }

}
