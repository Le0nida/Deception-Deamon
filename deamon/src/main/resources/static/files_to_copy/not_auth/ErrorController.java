package io.swagger.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "not_authorized";
    }
}
