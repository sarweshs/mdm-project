package com.mdm.reviewdashboard.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BotNlpDashboardController {

    @GetMapping("/bot-nlp-dashboard")
    public String showNlpDashboard() {
        return "bot-nlp-dashboard"; // Thymeleaf will render bot-nlp-dashboard.html
    }
} 