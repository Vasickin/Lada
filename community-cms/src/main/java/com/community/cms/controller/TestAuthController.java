package com.community.cms.controller;

import com.community.cms.model.User;
import com.community.cms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Optional;

@Controller
public class TestAuthController {
    @Autowired private UserService userService;
    
    @GetMapping("/test-auth")
    @ResponseBody
    public String testAuth() {
        Optional<User> userOpt = userService.findUserByUsername("admin");
        if (userOpt.isEmpty()) return "❌ Пользователь 'admin' не найден";
        
        User user = userOpt.get();
        return "✅ Найден: " + user.getUsername() + "<br>" +
               "Пароль: " + user.getPassword() + "<br>" +
               "Роли: " + user.getRoles() + "<br>" +
               "Enabled: " + user.isEnabled();
    }
}
