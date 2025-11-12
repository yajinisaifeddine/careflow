package com.careflow.controllers;

import com.careflow.models.User;
import com.careflow.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RestController
public class TestController {
private final UserRepository userRepository;
    @GetMapping("/test")
    public Map<?,?> getMethodName() {
        User user =userRepository.findById(29l).orElse(null);
        return Map.of(
                "status",200,
                "message","test",
                "data", user,
                "timeStamp", LocalDateTime.now()
        );
    }

}
