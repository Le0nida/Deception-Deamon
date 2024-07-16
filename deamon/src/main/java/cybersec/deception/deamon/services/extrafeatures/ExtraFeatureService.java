package cybersec.deception.deamon.services.extrafeatures;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtraFeatureService {

    private final AdminPagesService adminPagesService;

    @Autowired
    public ExtraFeatureService(AdminPagesService adminPagesService) {
        this.adminPagesService = adminPagesService;
    }

    public void addAdminPages(String adminUsername, String adminPass) {
        adminPagesService.addAdminFeatures(adminUsername,adminPass);
    }
}
