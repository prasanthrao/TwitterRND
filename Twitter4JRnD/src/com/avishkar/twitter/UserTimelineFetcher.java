package com.avishkar.twitter;

import java.net.UnknownHostException;
import java.util.HashSet;

import com.avishkar.AccessTokenUtil;
import com.avishkar.db.DBAccess;
import com.avishkar.db.DBStatus;
import com.google.gson.Gson;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class UserTimelineFetcher {
	
	public static void getUserTimeline(Long id, Twitter twitter){
		try {
//			if(DBStatus.ifUserExists(id)){
//				System.out.println("User tweets exists in system for "+id);
//				return;
//			}
			RateLimit.checkRateLimit("/statuses/user_timeline", twitter);
			RateLimit.decrementRateLimit("/statuses/user_timeline");
			ResponseList<Status> statuses = twitter.getUserTimeline(id);
			for(Status status:statuses){
				if(!DBStatus.ifStatusExists(status.getId())){
					System.out.println("Status add. User:"+status.getUser().getScreenName()+"|Status:"+status.getText());
					final Gson gson = new Gson();
					DBStatus.insertStatus(gson.toJson(status));
				}
				else{
					System.out.println("Status exists. User:"+status.getUser().getScreenName()+"|Status:"+status.getText());
				}
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
