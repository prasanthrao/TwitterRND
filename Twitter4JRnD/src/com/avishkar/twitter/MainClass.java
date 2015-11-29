package com.avishkar.twitter;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.avishkar.AccessTokenUtil;
import com.avishkar.db.DBAccess;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class MainClass {
	
	public static void main(String[] args) throws UnknownHostException, TwitterException, InterruptedException {
		DBAccess.setDbName("prasanthgrao");
		HashSet<Long> currentLevel = new HashSet<>();
		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();
//		currentLevel.add((long) 39703979);
		currentLevel.add(twitter.getId());
		List<Long> nextLevel = new LinkedList<Long>();
		for (int level = 1; level <= 2; level++) {
			System.out.println("Main: Iteration " + level + ".users in level:" + currentLevel.size());
			for (Long id : currentLevel) {
				System.out.println("Main: Fetching data for user ID:" + id);
				List<Long> filteredUserListOnStatusCount = UserFollowerFetcher.getUserFollwers(id);
				if (filteredUserListOnStatusCount == null)
					continue;
				for(Long user:filteredUserListOnStatusCount){
					UserTimelineFetcher.getUserTimeline(user, twitter);
				}
				System.out.println(
						"Main: No of followers whose followers < 100 : " + filteredUserListOnStatusCount.size());
				nextLevel.addAll(filteredUserListOnStatusCount);
			}
			System.out.println(
					"Main: Fecting for Iteration " + level + " completed. Users in next level:" + nextLevel.size());
			currentLevel.clear();
			currentLevel.addAll(nextLevel);
			nextLevel.clear();
		}
	}
}
