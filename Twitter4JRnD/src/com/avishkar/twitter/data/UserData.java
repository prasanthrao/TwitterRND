package com.avishkar.twitter.data;

public class UserData {
	
	private String screenName;
	
	private long friendsCount;
	
	private long followersCount;
	
	private long tweetCount;
	
	private int favCount;
	
	private int listedCount;
	
	private long userID;
	
	private boolean isProtected = false;

	public String getScreenName() {
		return screenName;
	}

	public long getFriendsCount() {
		return friendsCount;
	}

	public long getFollowersCount() {
		return followersCount;
	}

	public long getTweetCount() {
		return tweetCount;
	}

	public long getUserID() {
		return userID;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public void setFriendsCount(long friendsCount) {
		this.friendsCount = friendsCount;
	}

	public void setFollowersCount(long followersCount) {
		this.followersCount = followersCount;
	}

	public void setTweetCount(long tweetCount) {
		this.tweetCount = tweetCount;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

	public long getListedCount() {
		return listedCount;
	}

	public void setListedCount(int listedCount) {
		this.listedCount = listedCount;
	}

	public long getFavCount() {
		return favCount;
	}

	public void setFavCount(int favCount) {
		this.favCount = favCount;
	}
	
	

}
