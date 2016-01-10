package com.avishkar;

import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class AccessTokenUtil {

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
	
	public static Configuration getConfigPGRReseach(){
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setDebugEnabled(true)
		.setOAuthConsumerKey("DEY7Ed0br7Bs8CTyQBA5eK55A ")
		.setOAuthConsumerSecret("ORIKGJZGTYlUXgLtdRYjaN5b970EfQYpv9j27hd5kQ5JJaPNJR")
		.setOAuthAccessToken("4732219470-IBRoPH3hpLx0IXOxIG3B7cWaAEwEGNFAiLPo6IX")
		.setOAuthAccessTokenSecret("DzyAVTlhyOqY0dKM1JxQkXRrYLmH56WN2Z5dAWByyPChW");
		return builder.build();
	}
	
}
