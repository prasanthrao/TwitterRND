package com.avishkar.twitter;

import java.util.HashMap;
import java.util.Map;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class RateLimit {

	static Map<String, Integer> rateRemaining = new HashMap<String, Integer>();
	static Map<String, RateLimitStatus> rateMap = null;

	static long timeSinceUpdate = System.currentTimeMillis();

	public static int checkRateLimit(String uri, Twitter twitter) throws TwitterException, InterruptedException {
		if(System.currentTimeMillis()-timeSinceUpdate>(10*1000)){
			timeSinceUpdate = System.currentTimeMillis();
			rateMap = null;
			rateRemaining.clear();
			System.out.println("Timer flush of RATE limit status.");
		}
		if (null == rateMap || !rateRemaining.containsKey(uri) || rateRemaining.get(uri) <= 1) {
			rateRemaining.clear();
			fetchRateLimit(uri, twitter);
			rateRemaining.put(uri, rateMap.get(uri).getRemaining());
		}
		System.out.println("For URI:" + uri + " remaining requests:" + rateRemaining.get(uri));
		return rateRemaining.get(uri);

	}
	

	public static void decrementRateLimit(String uri) {
		int remaining = 0;
		if(rateRemaining.containsKey(uri))
		remaining = rateRemaining.get(uri);
		else rateRemaining.put(uri, rateMap.get(uri).getRemaining());
		rateRemaining.put(uri, remaining - 1);
	}

	private static int fetchRateLimit(String uri, Twitter twitter) throws TwitterException, InterruptedException {
		System.out.println("Checking rate limit for URI:" + uri);
		rateMap = twitter.getRateLimitStatus();
		RateLimitStatus rateLimit = rateMap.get(uri);
		RateLimitStatus apprateLimit = rateMap.get("/application/rate_limit_status");
		if (apprateLimit.getRemaining() <= 1) {
			consoleTimerWaitForRate("/application/rate_limit_status", apprateLimit);
		}
		if (apprateLimit.getRemaining() <= 1) {
			System.out.println("Rate Limit URI fetch exhausted. Check cancelled. Operation may fail.");
		}
		System.out.println("Rate Limit remaining:" + rateLimit.getRemaining());
		if (rateLimit.getRemaining() <= 0) {
			consoleTimerWaitForRate(uri, rateLimit);
		}
		return rateLimit.getRemaining();
	}

	private static void consoleTimerWaitForRate(String uri, RateLimitStatus rateLimit) throws InterruptedException {
		// Waiting logic
		int timeremaining = rateLimit.getSecondsUntilReset();
		System.out.println("Rate Limit exceed for " + uri);
		do {
			String status = "Waiting for API reset. Reset in(Seconds):" + timeremaining--;
			for (int i = 0; i < status.length(); i++)
				System.out.print("\b");
			System.out.print(status);
			Thread.sleep(1000);
		} while (timeremaining > 0);
		System.out.println("API Reset for URI:" + uri);
		System.out.println("Invoking fetch in 5 seconds");
		Thread.sleep(5000);
	}

}
