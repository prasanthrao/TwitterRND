package com.avishkar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class DBFollowersToCSV {

	public static void main(String[] args) throws IOException {
		Mongo mongo = new Mongo("localhost", 27017);
		DB db = mongo.getDB("prasanthgrao");
		DBCollection con = db.getCollection("Followers");
		DBCursor followers = con.find();
		File file = new File("followers.csv");

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Source,Target" + System.lineSeparator());

		for (DBObject follower : followers) {
			String followersString = follower.get("followers").toString();
			String[] items = followersString.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
			for (int i = 0; i < items.length; i++)
				bw.write(follower.get("id") + "," + items[i] + System.lineSeparator());
		}
		bw.close();
	}

}
