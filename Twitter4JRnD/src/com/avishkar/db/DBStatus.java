package com.avishkar.db;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class DBStatus extends DBAccess {
	
	public static void insertStatus(String jsonStatus) throws UnknownHostException{
		DBObject dbObject = (DBObject)JSON.parse(jsonStatus);
		getDBConnection("Status").insert(dbObject);
	}
	
	public static boolean ifStatusExists(long id) throws UnknownHostException{
		BasicDBObject fields = new BasicDBObject();
		fields.put("id",id);
		DBCursor cursor = getDBConnection("Status").find(fields);
		return cursor.hasNext();
	}

	public static boolean ifUserExists(long id) throws UnknownHostException{
		BasicDBObject fields = new BasicDBObject();
		fields.put("user.id",id);
		DBCursor cursor = getDBConnection("Status").find(fields);
		return cursor.hasNext();
	}

}
