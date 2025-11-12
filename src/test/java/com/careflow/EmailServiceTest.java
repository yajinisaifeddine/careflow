package com.careflow;

import com.careflow.services.email.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {
@Autowired
private EmailService emailService;
    @Test
    void senEmailtest(){
emailService.sendSimpleEmail("saifeddineyajini@gmail.com","si fawzi","sifawzi cv labas 3lik");
    }
}
