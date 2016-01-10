package com.avishkar.twitter;

import java.net.UnknownHostException;
import java.util.List;

import com.avishkar.AccessTokenUtil;
import com.avishkar.db.DBAccess;
import com.avishkar.db.DBFollowers;
import com.avishkar.db.DBStatus;
import com.avishkar.db.DBUser;
import com.avishkar.twitter.data.UserData;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class StatusFetcher {

	public static void main(String[] args) throws UnknownHostException, IllegalStateException, TwitterException {
		DBAccess.setDbName("prasanthgrao");
//		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();
		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfigPGRReseach()).getInstance();
		List<Long> users = DBUser.getAllPublicUserID();
		System.out.println("Total Users:"+users.size());
		List<Long> existingUsers = DBStatus.getUserIDs();
		for (Long long1 : existingUsers) {
			users.remove(long1);
		}
		System.out.println("Pending users:"+users.size());
		try {
			System.out.println(twitter.getScreenName());
			int i = 1;
			for (Long user : users) {
				System.out.println("Fetching for user:" + user);
				UserData userData = DBUser.getUserData(user);
				if(userData.getTweetCount()>0){
					UserTimelineFetcher.getUserTimelineWithDBCheck(user, twitter);
				}
				System.out.println("Completed " + i++ + " of " + users.size());
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

}
