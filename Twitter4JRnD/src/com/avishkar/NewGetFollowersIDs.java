/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.avishkar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.avishkar.db.DBAccess;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import twitter4j.HttpResponseCode;
import twitter4j.IDs;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public final class NewGetFollowersIDs {

	static final String pendingIdListFile = "pending.tmp";
	static final String retrivedIdFile = "retrived.tmp";

	static long DAY_IN_MS = 1000 * 60 * 60 * 24;
	static Date sevenDaysAgo = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));

	/**
	 * Usage: java twitter4j.examples.friendsandfollowers.GetFollowersIDs
	 * [screen name]
	 *
	 * @param args
	 *            message
	 * @throws TwitterException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args)
			throws TwitterException, InterruptedException, IOException, ClassNotFoundException {
		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();
		DBAccess.setDbName("suhasn");

		HashSet<Long> fetchedIds = new HashSet<>();
		HashSet<Long> pendingIds = new HashSet<>();
		HashSet<Long> currentLevel = new HashSet<>();
		File fileExistsTestHandle = new File(pendingIdListFile);
		currentLevel.add((long) 39703979);
		for (int level = 1; level <= 2; level++) {
			System.out.println("Iteration " + level + ".users in level:" + currentLevel.size());
			pendingIds.clear();
			for (Long id : currentLevel) {
				List<Long> filteredUserListOnStatusCount = getUserFollwers(id);
				fetchedIds.add(id);
				writeHashSet(retrivedIdFile, fetchedIds);
				if (filteredUserListOnStatusCount == null)
					continue;
				System.out.println("No of followers whose follwers < 100 : " + filteredUserListOnStatusCount.size());
				for (Long filteredId : filteredUserListOnStatusCount) {
					if (!fetchedIds.contains(filteredId))
						pendingIds.add(filteredId);
					writeHashSet(pendingIdListFile, pendingIds);
				}
			}
			currentLevel.clear();
			currentLevel.addAll(pendingIds);
			System.out.println("Pending Ids Size:" + pendingIds.size());
			System.out.println("Fetched Ids Size:" + fetchedIds.size());
		}
	}

	private static List<Long> getUserFollwers(long twitterUserId)
			throws UnknownHostException, TwitterException, InterruptedException {
		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();
		List<Long> filteredUserListOnFollowerCount = new ArrayList<Long>();
		final Gson gson = new Gson();
		try {

			checkRateLimit("/followers/ids", twitter);
			System.out.println("Listing followers's Follower for ID:" + twitterUserId + System.lineSeparator());
			IDs followerIDs = twitter.getFollowersIDs(twitterUserId, -1);
			JsonObject followerJSON = new JsonObject();

			followerJSON.addProperty("id", twitterUserId);
			followerJSON.addProperty("followers", gson.toJson(followerIDs.getIDs()));
			DBAccess.insert(gson.toJson(followerJSON));

			System.out.println("Current Followers Fetched Size:" + followerIDs.getIDs().length);
			// Filtering for influential user
			if (followerIDs.getIDs().length > 1000) {
				System.out.println("User assumed as Influential");
				return null;
			}
			int from = 0;
			int to = 99;
			int limit = checkRateLimit("/users/lookup", twitter);
			while (from < followerIDs.getIDs().length) {
				if (to > followerIDs.getIDs().length)
					to = followerIDs.getIDs().length - 1;
				if (limit == 0)
					checkRateLimit("/users/lookup", twitter);
				ResponseList<User> followers = twitter.lookupUsers(Arrays.copyOfRange(followerIDs.getIDs(), from, to));
				System.out.println("Recieved User count:" + followers.size());
				for (User user : followers) {
					DBAccess.insertUser(gson.toJson(user));
					if (user.getFollowersCount() < 1000) {
						// if(user.getStatusesCount()>0 &&
						// user.getStatus()!=null &&
						// sevenDaysAgo.after(user.getStatus().getCreatedAt()))
						filteredUserListOnFollowerCount.add(user.getId());
						getStatuses(user.getScreenName(),twitter);
					} else
						System.out.println("User " + user.getScreenName()
								+ " is pruned for Influential or over subscription." + " Follower count:"
								+ user.getFollowersCount() + " Friends Count:" + user.getFriendsCount());
				}

				from += 100;
				to += 100;
				limit--;
			}
		} catch (TwitterException te) {
			if (te.getStatusCode() == HttpResponseCode.UNAUTHORIZED
					|| te.getStatusCode() == HttpResponseCode.NOT_FOUND) {
				System.out.println("Encountered locked profile. Skipping " + twitterUserId);
				return null;

				// log something here
			}
			te.printStackTrace();
			System.out.println("Failed to get followers' Follower: " + te.getMessage());
			// System.exit(-1);
		}

		return filteredUserListOnFollowerCount;
	}
	
	private static void getStatuses(String screenName, Twitter twitter){
		try {
			final Gson gson = new Gson();
			checkRateLimit("/statuses/user_timeline", twitter);
			ResponseList<Status> statuses = twitter.getUserTimeline(screenName);
			for(Status status:statuses){
				DBAccess.insertStatus(gson.toJson(status));
			}
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int checkRateLimit(String uri, Twitter twitter) throws TwitterException, InterruptedException {
		System.out.println("Checking rate limit for URI:" + uri);
		Map<String, RateLimitStatus> rateMap = twitter.getRateLimitStatus();
		RateLimitStatus rateLimit = rateMap.get(uri);
		RateLimitStatus apprateLimit = rateMap.get("/application/rate_limit_status");
		if (apprateLimit.getRemaining() <= 1) {
			consoleTimerWaitForRate("/application/rate_limit_status", apprateLimit);
		}
		if (apprateLimit.getRemaining() <= 1) {
			System.out.println("Rate Limit URI fetch exhausted. Check cancelled. Operation may fail.");
		}
		System.out.println("Rate Limit remaining:" + rateLimit.getRemaining());
		if (rateLimit.getRemaining() == 0) {
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

	private static void writeHashSet(String filename, HashSet<Long> set) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(set);
		oos.close();
	}

	private static HashSet<Long> readHashSet(String filename) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fis);
		HashSet<Long> set = (HashSet<Long>) ois.readObject();
		ois.close();
		return set;
	}
}
