package com.avishkar.twitter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.avishkar.AccessTokenUtil;
import com.avishkar.db.DBAccess;
import com.avishkar.db.DBFollowers;
//import com.avishkar.db.DBAccess;
import com.avishkar.db.DBFriends;
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

public class UserFriendsFetcher {

	private static final int friendS_LIMIT = 250;

	public static void main(String[] args) throws UnknownHostException, IllegalStateException, TwitterException {
		DBAccess.setDbName("suhasn");
		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();
		List<Long> followers = DBFollowers.getFollowers(twitter.getId());
		System.out.println("Followers fetched:" + followers.size());
		for (Long follower : followers) {
			UserTimelineFetcher.getUserTimeline(follower, twitter);
//			try {
////				List<Long> friends = getUserFriends(follower);
////				if (null != friends)
////					System.out.println(friends.size());
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (UnknownHostException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (TwitterException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

	public static List<Long> getUserFriends(long twitterUserId)
			throws UnknownHostException, TwitterException, InterruptedException {
		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();
		List<Long> filteredUserListOnfriendCount = new ArrayList<Long>();
		List<Long> friendsList = new ArrayList<Long>();
		final Gson gson = new Gson();
		try {
			if (!DBUser.ifUserExists(twitterUserId)) {
				long[] user = { twitterUserId };
				ResponseList<User> userResponse = twitter.lookupUsers(user);
				if (!userResponse.isEmpty())
					DBUser.insertUser(gson.toJson(userResponse.get(0)));
			}

			/*
			 * friends Fetch logic
			 */
			if (!DBFriends.ifFriendsExists(twitterUserId)) {
				System.out.println("Listing friends from Twitter for ID:" + twitterUserId + System.lineSeparator());
				RateLimit.checkRateLimit("/friends/ids", twitter);
				RateLimit.decrementRateLimit("/friends/ids");
				IDs friendIDs = twitter.getFriendsIDs(twitterUserId, -1);

				// DB Persistence Logic for friends
				JsonObject friendJSON = new JsonObject();
				friendJSON.addProperty("id", twitterUserId);
				friendJSON.addProperty("friends", gson.toJson(friendIDs.getIDs()));
				DBFriends.insertFriends(gson.toJson(friendJSON));
				System.out.println("Current Friends Fetched Size:" + friendIDs.getIDs().length);
			}

			List<Long> DBFriendsList = DBFriends.getFriends(twitterUserId);
			System.out.println("friends of User:" + twitterUserId + " in DB:" + DBFriendsList.size());
			if (DBFriendsList.size() > friendS_LIMIT) {
				System.out.println("User " + twitterUserId + " assumed to be over subscribed.Skipped");
				return null;
			}
			for (Long friend : DBFriendsList) {
				if (!DBUser.ifUserExists(friend)) {
					friendsList.add(friend);
				} else {
					if (DBUser.getFriendsCount(friend) < friendS_LIMIT)
						filteredUserListOnfriendCount.add(friend);
				}
			}
			if (friendsList.isEmpty()) {
				System.out.println("All friends for in DB for User ID:" + twitterUserId);
				return filteredUserListOnfriendCount;
			}

			int from = 0;
			int to = 99;
			while (from < friendsList.size()) {
				if (to > friendsList.size())
					to = friendsList.size() - 1;

				long[] string_list = new long[to - from];

				for (int i = 0; i < string_list.length; i++) {
					string_list[i] = friendsList.get(i + from);
				}

				RateLimit.checkRateLimit("/users/lookup", twitter);
				RateLimit.decrementRateLimit("/users/lookup");
				ResponseList<User> friends = twitter.lookupUsers(string_list);
				System.out.println("Recieved User count:" + friends.size());
				for (User user : friends) {
					if (!DBUser.ifUserExists(user.getId())) {
						System.out.println("New User in DB. Screen Name:" + user.getScreenName());
						DBUser.insertUser(gson.toJson(user));
					} else {
						System.out.println("User " + user.getScreenName() + " already exists in DB.");
					}
					if (user.getFriendsCount() < friendS_LIMIT) {
						// if(user.getStatusesCount()>0 &&
						// user.getStatus()!=null &&
						// sevenDaysAgo.after(user.getStatus().getCreatedAt()))
						if (filteredUserListOnfriendCount.add(user.getId()))
							filteredUserListOnfriendCount.add(user.getId());
						// getStatuses(user.getScreenName(),twitter);
					} else
						System.out.println("User " + user.getScreenName()
								+ " is pruned for Influential or over subscription." + " friend count:"
								+ user.getFollowersCount() + " Friends Count:" + user.getFriendsCount());
				}

				from += 100;
				to += 100;
			}
		} catch (TwitterException te) {
			if (te.getStatusCode() == HttpResponseCode.UNAUTHORIZED
					|| te.getStatusCode() == HttpResponseCode.NOT_FOUND) {
				System.out.println("Encountered locked profile. Skipping " + twitterUserId);
				if (!DBFriends.ifExists(twitterUserId)) {
					JsonObject friendJSON = new JsonObject();

					friendJSON.addProperty("id", twitterUserId);
					friendJSON.addProperty("friends", "");
					DBFriends.insert(gson.toJson(friendJSON));
				}

				return filteredUserListOnfriendCount;
			}
			te.printStackTrace();
			System.out.println("Failed to get friends' friend: " + te.getMessage());
		}

		return filteredUserListOnfriendCount;
	}

}
