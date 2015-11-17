package com.avishkar;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class AccessTokenUtil {

	public static void main(String args[]) throws Exception {
		/*
		// The factory instance is re-useable and thread safe.
		TwitterFactory factory = new TwitterFactory();
		Twitter twitter = factory.getInstance();
		AccessToken accessToken = loadAccessToken(twitter.verifyCredentials().getId());
		System.out.println(twitter.verifyCredentials().getName());
		//twitter.setOAuthConsumer("dH7ncP6uUBoCFDXyxEWvw", "K8hlkir7VQyvEZEkzYIixDXeZWBqrmr4oFMgeCgsQI");
		//twitter.setOAuthAccessToken(accessToken);
		Status status = twitter.updateStatus("Test Post 2");
		System.out.println("Successfully updated the status to [" + status.getText() + "].");*/
		
	    Twitter twitter = TwitterFactory.getSingleton();
	    Query query = new Query("modi");
	    QueryResult result = twitter.search(query);
	    for (Status status : result.getTweets()) {
	        System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
	    }
		
	}

	public static AccessToken loadAccessToken(long useId){
	    String token = "84003261-zccDySgBtXA5mmaLtcRnaJC4QSlNq92wn8xAZfuf4";
	    String tokenSecret = "c4DQUbQ1HEpyTaC5NbZbZZ45CSM3sVITDCX4O2Y9Ebc";
	    return new AccessToken(token, tokenSecret);
	  }
	
	public static Configuration getConfig(){
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setDebugEnabled(true)
		.setOAuthConsumerKey("rGBRfKZePGSy4dnrl3UATlAur")
		  .setOAuthConsumerSecret("egh5lfKAWyn7gArqNqK9OrxGXrHS82nMehY3nSron2SvQ9ujcy")
		  .setOAuthAccessToken("84003261-zccDySgBtXA5mmaLtcRnaJC4QSlNq92wn8xAZfuf4")
		  .setOAuthAccessTokenSecret("c4DQUbQ1HEpyTaC5NbZbZZ45CSM3sVITDCX4O2Y9Ebc");
		return builder.build();
	}
	
}
