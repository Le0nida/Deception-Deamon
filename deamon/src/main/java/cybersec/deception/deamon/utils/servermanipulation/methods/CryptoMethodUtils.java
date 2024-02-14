package cybersec.deception.deamon.utils.servermanipulation.methods;

public class CryptoMethodUtils {

    public static String getJPATransferCryptoMethod() {
        return
                "if (receiverAddress != null && senderAddress != null && amount != null && currency != null) {\n" +
                        "\n" +
                        "            if (\"\".equals(currency)) {\n" +
                        "                return new ResponseEntity<String>(\"Currency can't be empty\", HttpStatus.BAD_REQUEST);\n" +
                        "            } else if (\"\".equals(amount)) {\n" +
                        "                return new ResponseEntity<String>(\"Amount can't be empty\", HttpStatus.BAD_REQUEST);\n" +
                        "            } else if (\"\".equals(receiverAddress) || receiverAddress.contains(\"1\") || receiverAddress.contains(\"0\")) {\n" +
                        "                return new ResponseEntity<String>(\"Receiver address is not valid\", HttpStatus.BAD_REQUEST);\n" +
                        "            } else if (\"\".equals(senderAddress) || senderAddress.contains(\"1\")  || senderAddress.contains(\"0\")) {\n" +
                        "                return new ResponseEntity<String>(\"Sender address is not valid\", HttpStatus.BAD_REQUEST);\n" +
                        "            }\n" +
                        "\n" +
                        "            if (senderAddress.contains(\"23\") && senderAddress.contains(\"e\")) {\n" +
                        "                return new ResponseEntity<String>(HttpStatus.FORBIDDEN);\n" +
                        "            }\n" +
                        "            else {\n" +
                        "                return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);\n" +
                        "            }\n" +
                        "        }\n" +
                        "        return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);";
    }
}
