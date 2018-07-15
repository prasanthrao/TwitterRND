package com.avishkar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avishkar.db.DBFollowers;
import com.avishkar.db.DBStatus;
import com.avishkar.db.DBUser;
import com.avishkar.db.DataAccess;
import com.avishkar.sentiword.SentiTweetData;
import com.avishkar.sentiword.SentiWordNet;
import com.avishkar.twitter.data.StatusData;
import com.avishkar.twitter.data.UserData;

public class DBUsersGraphToCSV {

	public static void main(String[] args) throws IOException, ParseException {
		DataAccess.setDbName("prasanthgrao");

		SentiWordNet sentiWordNet = new SentiWordNet();
		Map<Long, UserData> userdataList = DBUser.getMappedUserData();
		File file = new File("users_graph.csv");
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		List<Long> followers = DBFollowers.getFollowers(84003261);
		followers.add((long) 84003261);
		System.out.println("total users:" + followers.size());
		writeHeader(bw);
		int usercount = userdataList.size();
		Map<Long, List<StatusData>> statusMap = DBStatus.getAllStatus();
		Set<String> namedEntitySet = new HashSet<String>();
		for (Long follower : followers) {
			UserData user = userdataList.get(follower);
			if (user == null)
				continue;
			String screenName = user.getScreenName();
			long followersCount = user.getFollowersCount();
			long friendsCount = user.getFriendsCount();
			long statusesCount = user.getTweetCount();
			long favoriteCount = user.getFavCount();
			long listedCount = user.getListedCount();

			List<Long> userfollowers = DBFollowers.getFollowers(follower);
			ArrayList<Long> userSubFollowers = new ArrayList<Long>();
			ArrayList<Long> userSubFriends = new ArrayList<Long>();
			for (Long userFollower : userfollowers) {
				if (userdataList.containsKey(userFollower)) {
					userSubFollowers.add(userdataList.get(userFollower).getFollowersCount());
					userSubFriends.add(userdataList.get(userFollower).getFriendsCount());
				}
			}
			long avgSubFollowers = getAvg(userSubFollowers);
			long avgSubFriends = getAvg(userSubFriends);

			if (usercount-- % 100 == 0) {
				System.out.println(".." + usercount);
			}
			System.out.println("User:" + screenName);
			int favCount = 0;
			int rtCount = 0;
			int hIndex = 0;
			double netSentiScore = 0;
			long avgInterval = 0;
			String isRetweeted = "UNDEF";
			Map<String, Integer> namedEntityCount = createNamedEntityMap();

			if (statusMap.containsKey(user.getUserID())) {
				List<StatusData> dbStatuses = statusMap.get(user.getUserID());
				List<Date> statusCreateDate = new ArrayList<Date>();
				List<Double> sentiScore = new ArrayList<Double>();
				for (StatusData statusData : dbStatuses) {
					favCount += statusData.getFavoriteCount();
					rtCount += statusData.getRetweetCount();
					statusCreateDate.add(statusData.getCreateDate());
					if ("en".equalsIgnoreCase(statusData.getLanguage())) {
						SentiTweetData sentiData = sentiWordNet.evaluate(statusData.getTweet());
						sentiScore.add(sentiData.getScore());
						namedEntitySet.addAll(sentiData.getNamedEntities());
						updateMap(namedEntityCount, sentiData.getNamedEntities());
					}
				}
				netSentiScore = SentiWordNet.calculateAverage(sentiScore);
				hIndex = 0;
				for (int i = 0; i < dbStatuses.size(); i++) {
					int smaller = Math.min(dbStatuses.get(i).getRetweetCount(), dbStatuses.size() - i);
					hIndex = Math.max(hIndex, smaller);
				}
				if (rtCount > 0)
					isRetweeted = "T";
				else
					isRetweeted = "F";
				avgInterval = avgTime(statusCreateDate);
			}
			if (!isRetweeted.equalsIgnoreCase("UNDEF")) {
				bw.write(System.lineSeparator() + user.getUserID() + "," + screenName + "," + followersCount + ","
						+ friendsCount + "," + statusesCount + "," + favoriteCount + "," + listedCount + ","
						+ avgInterval + "," + avgSubFollowers + "," + avgSubFriends + "," + netSentiScore);
				writeNamedEntityMap(namedEntityCount, bw);
				bw.write("," + isRetweeted);
				bw.flush();
			}
		}
		bw.flush();
		bw.close();
		System.out.println(namedEntitySet);

	}

	private static void writeHeader(BufferedWriter bw) throws IOException {
		bw.write(
				"id,screenName,followersCount,friendsCount,statusesCount,favouritesCount,listedCount,avgInt,avgSubFollowers,avgSubFriends,netSentiScore,");
		for(String namedEntity:namedEntities) {
			bw.write(namedEntity+",");
		}
		bw.write("isRetweeted");
	}

	private static Long getAvg(List<Long> values) {
		if (values != null && !values.isEmpty()) {
			long avg = 0;
			for (Long value : values) {
				avg += value;
			}
			return avg / values.size();
		}
		return (long) 0;
	}

	private static long avgTime(List<Date> statusCreateDate) {
		if (statusCreateDate.size() >= 2) {
			long avgTime = 0;
			Collections.sort(statusCreateDate);
			for (int i = 0; i < statusCreateDate.size() - 1; i++) {
				avgTime = (avgTime + (statusCreateDate.get(i + 1).getTime() - statusCreateDate.get(i).getTime())) / 2;
			}
			return avgTime / (1000 * 60 * 60);
		}
		return 0;
	}

	private static String[] namedEntities = { "LOCATION", "NATIONALITY", "NUMBER", "IDEOLOGY", "MONEY", "PERSON", "SET", "MISC",
			"TIME", "ORDINAL", "EMAIL", "CAUSE_OF_DEATH", "URL", "O", "STATE_OR_PROVINCE", "ORGANIZATION", "DATE",
			"CITY", "COUNTRY", "RELIGION", "PERCENT", "TITLE", "CRIMINAL_CHARGE", "DURATION" };

	private static Map<String, Integer> createNamedEntityMap() {
		Map<String, Integer> namedEntityMap = new HashMap<>();
		for (String namedEntity : namedEntities) {
			namedEntityMap.put(namedEntity, 0);
		}
		return namedEntityMap;
	}
	
	private static void updateMap(Map<String, Integer> namedEntityMap, Set<String> tweetEntities) {
		for(String tweetEntity :tweetEntities) {
			namedEntityMap.put(tweetEntity, namedEntityMap.get(tweetEntity)+1);
		}
	}
	
	private static void writeNamedEntityMap(Map<String, Integer> namedEntityMap,BufferedWriter bw) throws IOException {
		for(String tweetEntity:namedEntities) {
			bw.write( "," + namedEntityMap.get(tweetEntity));
		}
	}

}
