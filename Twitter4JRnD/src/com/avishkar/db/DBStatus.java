package com.avishkar.db;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.avishkar.twitter.data.StatusData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.json.DataObjectFactory;

public class DBStatus extends DataAccess {

	public static void insertStatus(String jsonStatus) throws UnknownHostException {
		DBObject dbObject = (DBObject) JSON.parse(jsonStatus);
		getDBConnection("Status").insert(dbObject);
	}

	public static boolean ifStatusExists(long id) throws UnknownHostException {
		BasicDBObject fields = new BasicDBObject();
		fields.put("id", id);
		DBCursor cursor = getDBConnection("Status").find(fields);
		return cursor.hasNext();
	}

	public static boolean ifUserExists(long id) throws UnknownHostException {
		BasicDBObject fields = new BasicDBObject();
		fields.put("user.id", id);
		DBCursor cursor = getDBConnection("Status").find(fields);
		return cursor.hasNext();
	}

	public static int getStatusesCount(Long userID) throws UnknownHostException {
		BasicDBObject fields = new BasicDBObject();
		fields.put("user.id", userID);
		DBCursor cursor = getDBConnection("Status").find(fields);
		return cursor.count();
	}

	public static List<Long> getUserIDs() throws UnknownHostException {
		List<Long> users = new LinkedList<Long>();
		List userIds = getDBConnection("Status").distinct("user.id");
		for (Object object : userIds) {
			users.add(new Long(object.toString()));
		}
		return users;
	}

	public static Map<Long, List<StatusData>> getAllStatus() throws UnknownHostException {
		Map<Long, List<StatusData>> statusDataMap = new HashMap<Long, List<StatusData>>();

		DBCursor cursor = getDBConnection("Status").find();
		while (cursor.hasNext()) {
			StatusData status = parseToStatusData(cursor);
			if (!statusDataMap.containsKey(status.getUserID())) {
				statusDataMap.put(status.getUserID(), new LinkedList<StatusData>());
			}
			statusDataMap.get(status.getUserID()).add(status);
		}
		return statusDataMap;
	}

	public static List<StatusData> getStatuses(Long userID) throws UnknownHostException {
		List<StatusData> statuses = new LinkedList<StatusData>();
		BasicDBObject fields = new BasicDBObject();
		fields.put("user.id", userID);

		DBCursor cursor = getDBConnection("Status").find(fields);
		while (cursor.hasNext()) {
			StatusData status = parseToStatusData(cursor);
			statuses.add(status);
		}
		return statuses;
	}

	private static StatusData parseToStatusData(DBCursor cursor) {
		DBObject dbStatus = cursor.next();
		dbStatus.removeField("_id");
		StatusData status = new StatusData();
		status.setCreateDate(dbStatus.get("createdAt").toString());
		status.setFavoriteCount((int) dbStatus.get("favoriteCount"));
		status.setRetweetCount((int) dbStatus.get("retweetCount"));
		status.setRetweeted((boolean) dbStatus.get("isRetweeted"));
		status.setUserID(((BasicDBObject) dbStatus.get("user")).getLong("id"));
		status.setRetweeted(dbStatus.containsField("retweetedStatus"));
		status.setTweet(dbStatus.get("text").toString());
		status.setLanguage(dbStatus.get("lang").toString());
		return status;
	}

}
