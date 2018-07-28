package com.avishkar.db;

import java.net.UnknownHostException;
import java.util.HashMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class DataAccess {
	
	private static final int DB_PARAM_PORT = 27017;
	private static final String DB_PARAM_LOCALHOST = "localhost";
	
	private static HashMap<String,DBCollection> collection = new HashMap<>();
	private static String dbName = "twitter";
	static DB db = null;

	protected static DBCollection getDBConnection(String collectionName) throws UnknownHostException{
		if(!collection.containsKey(dbName+collectionName)){
			if(db==null){
				Mongo mongo = new MongoClient(DB_PARAM_LOCALHOST, DB_PARAM_PORT);
				db = mongo.getDB(dbName);
			}
			collection.put(dbName+collectionName, db.getCollection(collectionName));
		}
		return collection.get(dbName+collectionName);
	}
	
	public static void insertTweet(String jsonStatus) throws UnknownHostException{
		DBObject dbObject = (DBObject)JSON.parse(jsonStatus);
		getDBConnection("tweet").insert(dbObject);
	}
	
	public static void insertUser(String jsonStatus) throws UnknownHostException{
		DBObject dbObject = (DBObject)JSON.parse(jsonStatus);
		getDBConnection("User").insert(dbObject);
	}

	public static void insertStatus(String jsonStatus) throws UnknownHostException{
		DBObject dbObject = (DBObject)JSON.parse(jsonStatus);
		getDBConnection("Status").insert(dbObject);
	}
	
	public static boolean ifExists(long id) throws UnknownHostException{
		BasicDBObject fields = new BasicDBObject();
		fields.put("id",id);
		DBCursor cursor = getDBConnection("Followers").find(fields);
		return cursor.hasNext();
	}
	
	public static boolean ifUserExists(long id) throws UnknownHostException{
		BasicDBObject fields = new BasicDBObject();
		fields.put("id",id);
		DBCursor cursor = getDBConnection("User").find(fields);
		return cursor.hasNext();
	}
	
	public static void main(String args[]) throws UnknownHostException{
		System.out.println(ifExists(3097759151L));
	}
	
	public static void insert(String json) throws UnknownHostException {
		DBObject dbObject = (DBObject)JSON.parse(json);
		getDBConnection("Followers").insert(dbObject);
	}
		
	public static String getDbName() {
		return dbName;
	}

	public static void setDbName(String dbName) {
		DataAccess.dbName = dbName;
	}

	public static long getTweetCount() throws UnknownHostException{
		return getDBConnection("tweet").getCount();
	}


}
