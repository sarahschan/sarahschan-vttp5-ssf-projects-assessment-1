package vttp2023.batch3.ssf.frontcontroller.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp2023.batch3.ssf.frontcontroller.respositories.AuthenticationRepository;

@Service
public class AuthenticationService {

	private static final String AUTH_URL = "https://authservice-production-e8b2.up.railway.app/api/authenticate";

	RestTemplate restTemplate = new RestTemplate();

	@Autowired
	AuthenticationRepository authenticationRepository;


	// Sends the POST request to the external authentication API
	public void authenticate(String username, String password) throws Exception {

		// build payload
		JsonObject payloadJsonObject = Json.createObjectBuilder()
										.add("username", username)
										.add("password", password)
										.build();

		// convert it to a string for the rest template
		String payload = payloadJsonObject.toString();

		// Set the HTTP headers
		HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			headers.set("Accept", "application/json");

		// Create a request entity to send to the API
		RequestEntity<String> request = RequestEntity.post(AUTH_URL)
													 .headers(headers)
													 .body(payload);

		// Use the rest template to send the POST request to the API
		try {

			ResponseEntity<String> response = restTemplate.exchange(request, String.class);
			
			// Check if response header is 201 Accepted
			if (response.getStatusCode() != HttpStatus.ACCEPTED) {
				throw new Exception("Authentication failed: " + response.getStatusCode() + response.getBody());
			}

		} catch (Exception e) {
			throw new Exception("Authentication failed: " + e.getMessage());
		}
	}


	// Increment failed login attempts
	public void incrementFailedAttempts(String username){
		authenticationRepository.incrementFailedAttempts(username);
	}


	// Check the number of failed attempts
	public Integer getFailedAttempts(String username){
		return authenticationRepository.getFailedAttempts(username);
	}


	// Lock user for 30 mins
	public void disableUser(String username) {
		authenticationRepository.disableUser(username);
	}


	// Check if user is locked
	public boolean isLocked(String username) {
		return authenticationRepository.isUserLocked(username);
	}


	// Clear bad request tracking
	public void clearUserTracking(String username) {
		authenticationRepository.clearUserTracking(username);
	}


	// simulator since API is not working
	public void authenticateSimulator(String username, String password) throws Exception {

		// Simulate a successful authentication
		if (username.equals("sarah") && password.equals("sarah")){
			return;
		}

		// Simulate authentication failure
		throw new Exception("Authentication failed");
	}

}
