package com.avishkar.twitter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.avishkar.AccessTokenUtil;
import com.avishkar.db.DBAccess;
//import com.avishkar.db.DBAccess;
import com.avishkar.db.DBFollowers;
import com.avishkar.db.DBUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import twitter4j.HttpResponseCode;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class Fetcher {

	private static final int FOLLOWERS_LIMIT = 1000;

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
				List<Long> filteredUserListOnStatusCount = getUserFollwers(id);
				if (filteredUserListOnStatusCount == null)
					continue;
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

	private static List<Long> getUserFollwers(long twitterUserId)
			throws UnknownHostException, TwitterException, InterruptedException {
		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();
		List<Long> filteredUserListOnFollowerCount = new ArrayList<Long>();
		List<Long> followersList = new ArrayList<Long>();
		final Gson gson = new Gson();
		try {
			if(!DBUser.ifUserExists(twitterUserId)){
				long[] user = {twitterUserId};
				ResponseList<User> userResponse = twitter.lookupUsers(user);
				if(!userResponse.isEmpty())
					DBUser.insertUser(gson.toJson(userResponse.get(0)));
			}

			/*
			 * Followers Fetch logic
			 */
			if (!DBFollowers.ifFollowerExists(twitterUserId)) {
				System.out.println("Listing Followers from Twitter for ID:" + twitterUserId + System.lineSeparator());
				RateLimit.checkRateLimit("/followers/ids", twitter);
				RateLimit.decrementRateLimit("/followers/ids");
				IDs followerIDs = twitter.getFollowersIDs(twitterUserId, -1);
				
				// DB Persistence Logic for Followers
				JsonObject followerJSON = new JsonObject();
				followerJSON.addProperty("id", twitterUserId);
				followerJSON.addProperty("followers", gson.toJson(followerIDs.getIDs()));
				DBFollowers.insertFollowers(gson.toJson(followerJSON));
				System.out.println("Current Followers Fetched Size:" + followerIDs.getIDs().length);
			}

			List<Long> dbFollowersList = DBFollowers.getFollowers(twitterUserId);
			System.out.println("Followers of User:" + twitterUserId + " in DB:" + dbFollowersList.size());
			if (dbFollowersList.size() > FOLLOWERS_LIMIT) {
				System.out.println("User " + twitterUserId + " assumed as Influential.Skipped");
				return null;
			}
			for (Long follower : dbFollowersList) {
				if (!DBUser.ifUserExists(follower)) {
					followersList.add(follower);
				} else {
					if (DBUser.getFollowerCount(follower) < FOLLOWERS_LIMIT)
						filteredUserListOnFollowerCount.add(follower);
				}
			}
			if (followersList.isEmpty()) {
				System.out.println("All followers for in DB for User ID:" + twitterUserId);
				return filteredUserListOnFollowerCount;
			}

			int from = 0;int to = 99;
			while (from < followersList.size()) {
				if (to > followersList.size())
					to = followersList.size() - 1;

				long[] string_list = new long[to - from];

				for (int i = 0; i < string_list.length; i++) {
					string_list[i] = followersList.get(i + from);
				}

				RateLimit.checkRateLimit("/users/lookup", twitter);
				RateLimit.decrementRateLimit("/users/lookup");
				ResponseList<User> followers = twitter.lookupUsers(string_list);
				System.out.println("Recieved User count:" + followers.size());
				for (User user : followers) {
					if (!DBUser.ifUserExists(user.getId())) {
						System.out.println("New User in DB. Screen Name:" + user.getScreenName());
						DBUser.insertUser(gson.toJson(user));
					} else {
						System.out.println("User " + user.getScreenName() + " already exists in DB.");
					}
					if (user.getFollowersCount() < FOLLOWERS_LIMIT) {
						// if(user.getStatusesCount()>0 &&
						// user.getStatus()!=null &&
						// sevenDaysAgo.after(user.getStatus().getCreatedAt()))
						if (filteredUserListOnFollowerCount.add(user.getId()))
							filteredUserListOnFollowerCount.add(user.getId());
						// getStatuses(user.getScreenName(),twitter);
					} else
						System.out.println("User " + user.getScreenName()
								+ " is pruned for Influential or over subscription." + " Follower count:"
								+ user.getFollowersCount() + " Friends Count:" + user.getFriendsCount());
				}

				from += 100;
				to += 100;
			}
		} catch (TwitterException te) {
			if (te.getStatusCode() == HttpResponseCode.UNAUTHORIZED
					|| te.getStatusCode() == HttpResponseCode.NOT_FOUND) {
				System.out.println("Encountered locked profile. Skipping " + twitterUserId);
				if(!DBFollowers.ifFollowerExists(twitterUserId)){
					JsonObject followerJSON = new JsonObject();
					
					followerJSON.addProperty("id", twitterUserId);
					followerJSON.addProperty("followers", "");
					DBFollowers.insertFollowers(gson.toJson(followerJSON));
				}

				return filteredUserListOnFollowerCount;
			}
			te.printStackTrace();
			System.out.println("Failed to get followers' Follower: " + te.getMessage());
		}

		return filteredUserListOnFollowerCount;
	}

}
