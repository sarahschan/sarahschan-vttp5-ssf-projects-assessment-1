package vttp2023.batch3.ssf.frontcontroller.services;

import java.util.Random;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class CaptchaService {
    
    public void generateCaptcha(HttpSession session) {

        Random random = new Random();

        // Generate 2 random numbers
        Integer num1 = random.nextInt(10) + 1;
        Integer num2 = random.nextInt(10) + 1;
    
        // Pick an operator
        String[] operators = {"+", "-", "*", "/"};
        Integer selectedOperatorIndex = random.nextInt(4);
        String selectedOperator = operators[selectedOperatorIndex];
    
        // Calulate the correct answer
        Integer correctAnswer = 0;
    
        switch (selectedOperator) {
            case "+":
                correctAnswer = num1 + num2;
                break;
    
            case "-":
                correctAnswer = num1 - num2;
                break;
    
            case "*":
                correctAnswer = num1 * num2;
                break;
    
            case "/":
                correctAnswer = num1 / num2;
                break;
        }


        // Create and save the captcha question and answer as a session attribute
        String captchaQuestion = String.format("What is %d %s %d?", num1, selectedOperator, num2);
        session.setAttribute("captchaQuestion", captchaQuestion);
        session.setAttribute("correctCaptchaAnswer", correctAnswer);

    }
    
}
