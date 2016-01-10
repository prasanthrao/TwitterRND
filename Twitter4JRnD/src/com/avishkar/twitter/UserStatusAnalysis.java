package com.avishkar.twitter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.omg.CORBA.Environment;

import com.avishkar.AccessTokenUtil;
import com.avishkar.db.DBAccess;
import com.avishkar.db.DBFollowers;
import com.avishkar.db.DBStatus;
import com.avishkar.db.DBUser;
import com.avishkar.twitter.data.StatusData;
import com.avishkar.twitter.data.UserData;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class UserStatusAnalysis {

	private static final int HOURS_IN_MILLISECONDS = 60*60*1000;
	private static final Object DELIMITER = ",";

	public static void main(String[] args) throws UnknownHostException, IllegalStateException, TwitterException, FileNotFoundException {
		DBAccess.setDbName("prasanthgrao");
		Twitter twitter = new TwitterFactory(AccessTokenUtil.getConfig()).getInstance();
		List<Long> followers = DBFollowers.getFollowers(twitter.getId());
		StringBuffer buffer = new StringBuffer();
		buffer.append("UserID").append(DELIMITER).append("ScreenName").append(DELIMITER).append("FollowerCount")
				.append(DELIMITER).append("FriendsCount").append(DELIMITER).append("TotalStatusCount").append(DELIMITER)
				.append("StatusSampleSize").append(DELIMITER).append("hIndex").append(DELIMITER).append("retweets")
				.append(DELIMITER).append("IntervalBetweenTweets(Hours)").append(System.lineSeparator());

		for (Long follower : followers) {
			System.out.println();
			List<StatusData> statuses = DBStatus.getStatuses(follower);
			if (statuses == null || statuses.isEmpty()) {
				System.out.println("No Statuses for follower id:" + follower);
				continue;
			}

			// h-index computing
			Collections.sort(statuses, new Comparator<StatusData>() {
				@Override
				public int compare(StatusData o1, StatusData o2) {
					return o1.getRetweetCount() - o2.getRetweetCount();
				}

			});
			int hIndex = 0, retweets = 0;
			for (StatusData status : statuses) {
				if (!status.isRetweeted()) {
					if (hIndex < status.getRetweetCount())
						hIndex++;
					retweets += status.getRetweetCount();
				}
			}
			
			long interval = -1;
			if(statuses.size()>1){
				Collections.sort(statuses, new Comparator<StatusData>() {
					@Override
					public int compare(StatusData o1, StatusData o2) {
						return o1.getCreateDate().compareTo(o2.getCreateDate());
					}
					
				});
				long base = 1;
				for(int i=1;i<statuses.size();i++){
					interval += (statuses.get(i).getCreateDate().getTime()-statuses.get(i-1).getCreateDate().getTime())/base;
					base++;
				}
				System.out.println("Interval between tweets(hours):"+interval/HOURS_IN_MILLISECONDS);
			}
			// System.out.println("Follower id:" + follower +"|Total Status:" +
			// statuses.size() + "|h-index:" + hIndex + "|retweets:" +
			// retweets);

			UserData user = DBUser.getUserData(follower);

			buffer.append(follower).append(DELIMITER).append(user.getScreenName()).append(DELIMITER)
					.append(user.getFollowersCount()).append(DELIMITER).append(user.getFriendsCount()).append(DELIMITER)
					.append(user.getTweetCount()).append(DELIMITER).append(statuses.size()).append(DELIMITER)
					.append(hIndex).append(DELIMITER).append(retweets).append(DELIMITER).append(interval/HOURS_IN_MILLISECONDS).append(System.lineSeparator());
		}
		System.out.println(buffer.toString());
		try (PrintStream out = new PrintStream(new FileOutputStream("temp.csv"))) {
		    out.print(buffer.toString());
		}
	}
}
