package com.avishkar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class DBUsersToCSV {

	public static void main(String[] args) throws IOException, ParseException {
		Mongo mongo = new Mongo("localhost", 27017);
		DB db = mongo.getDB("prasanthgrao");
		DBCollection con = db.getCollection("User");
		//Fetch the recent date
		DateFormat dateFormatTweet = new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss a");
		List<String> dateList = con.distinct("status.createdAt");
		Date compareDate = dateFormatTweet.parse(dateList.get(0));
		for(String date:dateList){
			Date thisDate = dateFormatTweet.parse(date);
			if(compareDate.after(thisDate))
				compareDate = thisDate;
		}
		
		
		DBCursor users = con.find();
		File file = new File("users.csv");
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

		bw.write("id,screenName,followersCount,friendsCount,statusesCount,days"+System.lineSeparator());
		for(DBObject user:users){
			Long id = Long.parseLong(user.get("id").toString());
			String screenName = (String) user.get("screenName");
			int followersCount = (int) user.get("followersCount");
			int friendsCount = (int) user.get("friendsCount");
			int statusesCount = (int) user.get("statusesCount");
			int hours = Integer.MAX_VALUE;
			BasicDBObject status = (BasicDBObject) user.get("status");
			if(status!=null){
				Date lasttweetTime = dateFormatTweet.parse(status.get("createdAt").toString());
				hours = (int) (-(compareDate.getTime() - lasttweetTime.getTime())/ (1000 * 60 * 60));
			}
			bw.write(id+","+screenName+","+followersCount+","+friendsCount+","+statusesCount+","+hours+System.lineSeparator());
			bw.flush();
		}
		bw.close();

	}

}
