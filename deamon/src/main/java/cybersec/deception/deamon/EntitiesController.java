package cybersec.deception.deamon;

import cybersec.deception.deamon.services.EntitiesManipulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/entities")
public class EntitiesController {
   private final EntitiesManipulationService entManipulationService;

   @Autowired
   public EntitiesController(EntitiesManipulationService entManipulationService) {
       this.entManipulationService = entManipulationService;
   }

    @GetMapping("/retrieve/{fileName}")
    public ResponseEntity<String> retrieveEntity(@PathVariable String fileName) {
        try {
            String result = this.entManipulationService.retrieve(fileName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante il recupero: " + e.getMessage());
        }
    }

    @GetMapping("/retrieveAll")
    public ResponseEntity<Map<String, String>> retrieveAllEntities() {
        try {
            Map<String, String> result = this.entManipulationService.retrieveAll();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/delete/{fileName}")
    public ResponseEntity<Boolean> deleteEntity(@PathVariable String fileName) {
        try {
            boolean result = this.entManipulationService.delete(fileName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/create/{fileName}")
    public ResponseEntity<Boolean> createEntity(
            @PathVariable String fileName,
            @RequestBody String jsonString) {
        try {
            boolean result = this.entManipulationService.create(fileName, jsonString);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/update/{fileName}")
    public ResponseEntity<Boolean> updateEntity(
            @PathVariable String fileName,
            @RequestBody String jsonString) {
        try {
            boolean result = this.entManipulationService.update(fileName, jsonString);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
