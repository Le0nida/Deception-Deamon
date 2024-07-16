package io.swagger.api;

import io.swagger.log.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class AdminController {


    private final LogService logService;

    private final HttpServletRequest request;

    @Autowired
    public AdminController(LogService logService, HttpServletRequest request) {
        this.logService = logService;
        this.request = request;
    }


    @GetMapping("/")
    public String admin() {
        logService.log(request);
        return "redirect:/admin/login";
    }

    @GetMapping("/login")
    public String login() {
        logService.log(request);
        return "admin_login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        logService.log(request);
        return "admin_dashboard";
    }

    @GetMapping("/users")
    public String users() {
        logService.log(request);
        return "admin_users";
    }

    @GetMapping("/settings")
    public String settings() {
        logService.log(request);
        return "admin_settings";
    }

    @GetMapping("/logs")
    public String logs() {
        logService.log(request);
        return "admin_logs";
    }
}
