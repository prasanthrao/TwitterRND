package com.avishkar.twitter.data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatusData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Date createDate;
	
	private int retweetCount;
	
	private  boolean isRetweeted;
	
	private int favoriteCount;

	private long userID;
	
	public long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy h:m:s a");
		try {
			this.createDate = formatter.parse(createDate);
		} catch (ParseException e) {
			System.out.println("Parsing date failed");
		}
	}

	public int getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(int retweetCount) {
		this.retweetCount = retweetCount;
	}

	public boolean isRetweeted() {
		return isRetweeted;
	}

	public void setRetweeted(boolean isRetweeted) {
		this.isRetweeted = isRetweeted;
	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

}
