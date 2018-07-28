package com.avishkar.db;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class DBFollowers extends DataAccess {

	public static void insertFollowers(String json) throws UnknownHostException {
		DBObject dbObject = (DBObject)JSON.parse(json);
		getDBConnection("Followers").insert(dbObject);
		System.out.println("New followers eneterd in DB");
	}
	
	public static boolean ifFollowerExists(long id) throws UnknownHostException {
		BasicDBObject fields = new BasicDBObject();
		fields.put("id", id);
		DBCursor cursor = getDBConnection("Followers").find(fields);
		return cursor.hasNext();
	}

	public static List<Long> getFollowers(long id) throws UnknownHostException {
		List<Long> userIds = new LinkedList<Long>();
		if (!ifFollowerExists(id))
			return userIds;
		BasicDBObject fields = new BasicDBObject();
		fields.put("id", id);
		DBCursor cursor = getDBConnection("Followers").find(fields);
		DBObject follower = cursor.next();
		String followersString = follower.get("followers").toString();
		followersString = followersString.trim();
		if ("".equals(followersString))
			return userIds;
		String[] items = followersString.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
		for (int i = 0; i < items.length; i++) {
			if (!"".equals(items[i]))
				userIds.add(Long.parseLong(items[i]));
		}
		return userIds;
	}
}
