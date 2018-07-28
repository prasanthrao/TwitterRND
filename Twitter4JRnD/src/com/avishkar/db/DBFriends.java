package com.avishkar.db;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class DBFriends extends DataAccess {

	public static void insertFriends(String json) throws UnknownHostException {
		DBObject dbObject = (DBObject)JSON.parse(json);
		getDBConnection("Friends").insert(dbObject);
		System.out.println("New Friends eneterd in DB");
	}
	
	public static boolean ifFriendsExists(long id) throws UnknownHostException {
		BasicDBObject fields = new BasicDBObject();
		fields.put("id", id);
		DBCursor cursor = getDBConnection("Friends").find(fields);
		return cursor.hasNext();
	}

	public static List<Long> getFriends(long id) throws UnknownHostException {
		List<Long> userIds = new LinkedList<Long>();
		if (!ifFriendsExists(id))
			return null;
		BasicDBObject fields = new BasicDBObject();
		fields.put("id", id);
		DBCursor cursor = getDBConnection("Friends").find(fields);
		DBObject follower = cursor.next();
		String FriendsString = follower.get("friends").toString();
		FriendsString = FriendsString.trim();
		if ("".equals(FriendsString))
			return userIds;
		String[] items = FriendsString.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
		for (int i = 0; i < items.length; i++) {
			if (!"".equals(items[i]))
				userIds.add(Long.parseLong(items[i]));
		}
		return userIds;
	}
}
