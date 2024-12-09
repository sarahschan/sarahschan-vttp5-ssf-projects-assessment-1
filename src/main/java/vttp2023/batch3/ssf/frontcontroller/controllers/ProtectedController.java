package vttp2023.batch3.ssf.frontcontroller.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/protected")
public class ProtectedController {

	@GetMapping("/view1")
	public String showView1(HttpSession session){

		// Check if session has been authenticated
		String authenticatedUser = (String) session.getAttribute("authenticated");

		if (authenticatedUser == null) {
			return "redirect:/";

		} else {
			return "view1";
		}

	}

	@GetMapping("/logout")
	public String handleLogout(HttpSession session){

		System.out.println("Logging out user " + session.getAttribute("authenticated"));

		// clear session data
		session.removeAttribute("authenticated");
		session.invalidate();

		System.out.println("User successfully logged out");

		return "redirect:/";
	}
}
