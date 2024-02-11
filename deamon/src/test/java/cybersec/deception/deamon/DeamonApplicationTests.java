package cybersec.deception.deamon;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.methods.MethodsGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class DeamonApplicationTests {

	@Autowired
	private MethodsGeneration methodsGeneration;

	@Test
	void generateMethodsTest() {
		String controllerPath = "C:\\Users\\leona\\Desktop\\generatedserver\\src\\main\\java\\io\\swagger\\api\\UserApiController.java";
		List<String> controllerContent = FileUtils.leggiFile(controllerPath);

		String entityName = "User";

		List<String> methodSignatures = MethodsGeneration.generateMethods(controllerContent, entityName);

		System.out.println("--TEST--\n\n");
		for (String signature : methodSignatures) {
			System.out.println(signature);
		}
	}

}
