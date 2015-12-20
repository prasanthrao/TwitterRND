package com.avishkar.db;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class DBUser extends DBAccess {

	public static void insertUser(String jsonStatus) throws UnknownHostException {
		DBObject dbObject = (DBObject) JSON.parse(jsonStatus);
		getDBConnection("User").insert(dbObject);
		System.out.println("New user eneterd in DB");
	}

	private static DBCursor getUser(long id) throws UnknownHostException {
		BasicDBObject fields = new BasicDBObject();
		fields.put("id", id);
		DBCursor cursor = getDBConnection("User").find(fields);
		return cursor;
	}

	public static boolean ifUserExists(long id) throws UnknownHostException {
		DBCursor cursor = getUser(id);
		return cursor.hasNext();
	}

	public static int getFollowerCount(long id) throws UnknownHostException {
		DBCursor cursor = getUser(id);
		if(cursor.hasNext()){
			BasicDBObject obj = (BasicDBObject) cursor.next();
			return (int) obj.get("followersCount");
		}
		return 0;
	}

	public static int getFriendsCount(long id) throws UnknownHostException {
		DBCursor cursor = getUser(id);
		if(cursor.hasNext()){
			BasicDBObject obj = (BasicDBObject) cursor.next();
			return (int) obj.get("friendsCount");
		}
		return 0;
	}

}
