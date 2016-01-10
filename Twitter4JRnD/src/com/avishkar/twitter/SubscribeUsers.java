package com.avishkar.twitter;

import java.net.UnknownHostException;
import java.util.List;

import com.avishkar.AccessTokenUtil;
import com.avishkar.db.DBAccess;
import com.avishkar.db.DBUser;
import com.avishkar.twitter.data.UserData;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class SubscribeUsers {

	public static void main(String[] args) throws UnknownHostException {
		DBAccess.setDbName("prasanthgrao");
		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();
		List<Long> users = DBUser.getAllPublicUserID();
		try {
			System.out.println(twitter.getScreenName());
			for (Long long1 : users) {
				UserData userData = DBUser.getUserData(long1);
				if(userData.getTweetCount()>0){
					twitter.createFriendship(long1);
				}
				System.out.println(long1);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

}
