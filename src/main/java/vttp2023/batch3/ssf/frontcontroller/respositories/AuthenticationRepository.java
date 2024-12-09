package vttp2023.batch3.ssf.frontcontroller.respositories;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import vttp2023.batch3.ssf.frontcontroller.constant.Constant;

@Repository
public class AuthenticationRepository {

	private static final String REDIS_KEY_PREFIX = "USER:";
	private static final String FAILEDATTEMPTS = "failedAttempts";

	
	@Autowired
	@Qualifier(Constant.TEMPLATE02)
	RedisTemplate<String, String> template;

	
	// Get a user's current number of failed attempts
	public int getFailedAttempts(String username){
		String redisKey = REDIS_KEY_PREFIX + username;
		Object failedAttempts = template.opsForHash().get(redisKey, FAILEDATTEMPTS);

		if (failedAttempts == null) {
			return 0;
		} else {
			return Integer.parseInt(failedAttempts.toString());
		}
	}


	// Increment a user's failed attempt counter
	public void incrementFailedAttempts(String username){

		// Get current failedAttempts count and increment
		Integer incrementedFailedAttempts = getFailedAttempts(username) + 1;

		// Save new value to redis
		String redisKey = REDIS_KEY_PREFIX + username;
		template.opsForHash().put(redisKey, FAILEDATTEMPTS, incrementedFailedAttempts.toString());
	}


	// Disable a user for 30 minutes
	public Boolean disableUser(String username){
		Long lockDuration = (long) 30;
		Duration expireDuration = Duration.ofMinutes(lockDuration);
		String redisKey = REDIS_KEY_PREFIX + username;
		return template.expire(redisKey, expireDuration);
	}


	// Check if a user is currently locked
	//	consider locked if redis key exists and has an expiry
	public Boolean isUserLocked(String username){
		
		String redisKey = REDIS_KEY_PREFIX + username;
		
		// Null safe check for key and expiry
		Boolean keyExists = template.hasKey(redisKey);
		Long expiry = template.getExpire(redisKey);

		if (Boolean.TRUE.equals(keyExists) && expiry != null && expiry > 0){
			return true;
		}

		return false;
	}


	// Clear bad request tracking after successful login
	public void clearUserTracking(String username){
		String redisKey = REDIS_KEY_PREFIX + username;
		template.delete(redisKey);
	}

}