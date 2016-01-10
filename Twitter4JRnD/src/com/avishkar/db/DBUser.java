package com.avishkar.db;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import com.avishkar.twitter.data.UserData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;

public class DBUser extends DBAccess {

	public static void insertUser(String jsonStatus) throws UnknownHostException {
		DBObject dbObject = (DBObject) JSON.parse(jsonStatus);
		getDBConnection("User").insert(dbObject);
		System.out.println("New user eneterd in DB");
	}

	private static DBCursor getDBUser(long id) throws UnknownHostException {
		BasicDBObject fields = new BasicDBObject();
		fields.put("id", id);
		DBCursor cursor = getDBConnection("User").find(fields);
		return cursor;
	}
	
	public static List<Long> getAllPublicUserID() throws UnknownHostException{
		List<Long> userIDs = new LinkedList<Long>();
		BasicDBObject query = new BasicDBObject();
		query.put("isProtected", false);
		query.put("statusesCount", new BasicDBObject("$gt", 0));
		BasicDBObject fields = new BasicDBObject();
		fields.put("id", 1);
		DBCursor cursor = getDBConnection("User").find(query,fields);
		while(cursor.hasNext()){
			DBObject userObj = cursor.next();
			userIDs.add(Long.parseLong(userObj.get("id").toString()));
		}
		return userIDs;
	}
	
	public static User getUser(long id) throws UnknownHostException{
		DBCursor cursor = getDBUser(id);
		DBObject dbUser = cursor.next();
		dbUser.removeField("_id");
		try {
			return TwitterObjectFactory.createUser(dbUser.toString());
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static UserData getUserData(long id) throws UnknownHostException{
		if(ifUserExists(id)){
			DBCursor dbUser = getDBUser(id);
			DBObject user = dbUser.next();
			
			UserData userData = new UserData();
			userData.setUserID(id);
			userData.setFollowersCount((int) user.get("followersCount"));
			userData.setScreenName((String) user.get("screenName"));
			userData.setFriendsCount((int) user.get("friendsCount"));
			userData.setTweetCount((int) user.get("statusesCount"));
			userData.setProtected((boolean) user.get("isProtected"));
			
			return userData;
		}
		return null;
	}

	public static boolean ifUserExists(long id) throws UnknownHostException {
		DBCursor cursor = getDBUser(id);
		return cursor.hasNext();
	}

	public static int getFollowerCount(long id) throws UnknownHostException {
		DBCursor cursor = getDBUser(id);
		if(cursor.hasNext()){
			BasicDBObject obj = (BasicDBObject) cursor.next();
			return (int) obj.get("followersCount");
		}
		return 0;
	}

	public static int getFriendsCount(long id) throws UnknownHostException {
		DBCursor cursor = getDBUser(id);
		if(cursor.hasNext()){
			BasicDBObject obj = (BasicDBObject) cursor.next();
			return (int) obj.get("friendsCount");
		}
		return 0;
	}

}
