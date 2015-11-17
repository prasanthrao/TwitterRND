package com.avishkar;

import java.net.UnknownHostException;

import com.avishkar.db.DBAccess;
import com.google.gson.Gson;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterSearch {

	public static void main(String[] args) throws TwitterException, UnknownHostException {
		Twitter twitter = TwitterFactory.getSingleton();
		final Gson gson = new Gson();
	    Query query = new Query("blood urgent");
	    query.setSince("2012-09-01");
	    QueryResult result = twitter.search(query);
	    for (Status status : result.getTweets()) {
	    	DBAccess.insertTweet(gson.toJson(status));
	        System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
	    }
	}
}
