package com.avishkar.twitter;

import com.avishkar.AccessTokenUtil;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class TwitterClient{
	static Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();

}
