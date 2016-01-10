package com.avishkar.twitter;

import java.net.UnknownHostException;
import com.avishkar.db.DBStatus;
import com.google.gson.Gson;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class UserTimelineFetcher {

	public static void getUserTimeline(Long id, Twitter twitter) {
		try {
			RateLimit.checkRateLimit("/statuses/user_timeline", twitter);
			RateLimit.decrementRateLimit("/statuses/user_timeline");
			Paging paging = new Paging();
			paging.count(50);
			ResponseList<Status> statuses = twitter.getUserTimeline(id, paging);
			System.out.println("Statuses read:"+statuses.size());
			for (Status status : statuses) {
				if (!DBStatus.ifStatusExists(status.getId())) {
//					System.out.println(
//							"Status add. User:" + status.getUser().getScreenName() + "|Status:" + status.getText());
					final Gson gson = new Gson();
					DBStatus.insertStatus(gson.toJson(status));
				} else {
//					System.out.println(
//							"Status exists. User:" + status.getUser().getScreenName() + "|Status:" + status.getText());
				}
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public static void getUserTimelineWithDBCheck(Long id, Twitter twitter) throws UnknownHostException{
		if(!DBStatus.ifUserExists(id)){
			getUserTimeline(id, twitter);
		}
	}

}
