package vttp2023.batch3.ssf.frontcontroller.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import vttp2023.batch3.ssf.frontcontroller.services.AuthenticationService;
import vttp2023.batch3.ssf.frontcontroller.services.CaptchaService;

@Controller
@RequestMapping()
public class FrontController {

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	CaptchaService captchaService;


	// Show login page
	@GetMapping(path={"", "/"})
	public String landingPage(){
		return "view0";
	}


	// Handle login
	@PostMapping("/login")
	public String handleLogin(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam(value="userCaptchaAnswer", required=false) String userCaptchaAnswer, Model model, HttpSession session){

		// Step 1: Check if user is locked out
		if (authenticationService.isLocked(username)){
			model.addAttribute("user", username);
			return "view2";
		}


		// Step 2: If user is not locked out, validate form fields
		if (username.length() < 2 || password.length() < 2) {
			String errorMessage = "Username and password must be more than 2 characters";
			model.addAttribute("errorMessage", errorMessage);
			return "view0";
		}


		// Step 4: If captcha is required, validate the captcha field
		if (authenticationService.getFailedAttempts(username) > 0 && (userCaptchaAnswer == null || userCaptchaAnswer.isBlank())) {
			// ^ If this is not the user's first attempt, and the captcha field is empty

			String errorMessage = "Captcha is required";
			model.addAttribute("errorMessage", errorMessage);
			model.addAttribute("captchaQuestion", session.getAttribute("captchaQuestion"));
			
			return "view0";
		}


		// Step 5: If captcha all fields are okay and captcha is entered, check the captcha
		if (authenticationService.getFailedAttempts(username) > 0) {
			
			// Step 5a: Get the correct answer from the session
			String correctCaptchaAnswer = String.valueOf(session.getAttribute("correctCaptchaAnswer"));
			System.out.println("The correct answer is " + correctCaptchaAnswer);
			System.out.println("The user's ansewr is " + userCaptchaAnswer);

			// If user's answer is incorrect, count as a failed login attempt
			if (!userCaptchaAnswer.equals(correctCaptchaAnswer)) {
				
				// Step 5b: Increment the failed attempts counter
				authenticationService.incrementFailedAttempts(username);
				System.out.println(username + " has " + authenticationService.getFailedAttempts(username) + " failed login attempts");

				// Step 5c: Check if the user has 3 failed attempts. If they do, lock user and redirect to view2
				if (authenticationService.getFailedAttempts(username) == 3) {
					authenticationService.disableUser(username);
					model.addAttribute("user", username);
					return "view2";
				}

				// Step 5d: If they still have attempts left, generate a new captcha
				captchaService.generateCaptcha(session);

				// Step 5e: Get the question and add it to the model. Also include error message
				String captchaQuestion = (String) session.getAttribute("captchaQuestion");
				String errorMessage = "Incorrect Captcha answer";
				model.addAttribute("errorMessage", errorMessage);
				model.addAttribute("captchaQuestion", captchaQuestion);

				System.out.println("MADE IT HERE");

				// Step 5f: Return to login view with error message and captcha
				return "view0";
			}
		}


		// Step 3: If the form fields are okay, try and authenticate
		try {

			// Step 3a: Use authenticationService to call the API
			//	remember to change between authenticate / authenticateSimulator
			authenticationService.authenticate(username, password);

			System.out.println("Successful authentication of user " + username);

			// Step 3b: Clear any bad request tracking in redis
			authenticationService.clearUserTracking(username);

			// Step 3c: Mark the user as authenticated
			session.setAttribute("authenticated", username);
			System.out.println("Session attribute authenticated set to " + session.getAttribute("authenticated"));

			// Step 3d: Redirect the user to protected
			return "redirect:/protected/view1";


		} catch (Exception e) {

			System.out.println(e.getMessage());

			// Step 3a: Authentication has failed, increment failed login attempt tracking on redis
			authenticationService.incrementFailedAttempts(username);


			// Step 3b: Check the number of failed attempts, lock and redirect if necessary
			System.out.println(username + " has " + authenticationService.getFailedAttempts(username) + " failed login attempts");
			if (authenticationService.getFailedAttempts(username) == 3) {
				authenticationService.disableUser(username);
				model.addAttribute("user", username);
				return "view2";
			}


			// Step 3c: If the user can still try to log in, generate the captcha
			//	note that captchaService stores the question and answer as session variables
			captchaService.generateCaptcha(session);

			// Step 3d: Get the question from the session and add it to the model. Include login error message
			String captchaQuestion = (String) session.getAttribute("captchaQuestion");
			String errorMessage = "Invalid username and/or password";
			model.addAttribute("errorMessage", errorMessage);
			model.addAttribute("captchaQuestion", captchaQuestion);
			
			// Step 3e: Redirect back to the login page
			return "view0";

		}


	}
	
}
