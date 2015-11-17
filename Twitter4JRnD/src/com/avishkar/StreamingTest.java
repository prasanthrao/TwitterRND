package com.avishkar;

import java.net.UnknownHostException;
import java.util.ArrayList;

import com.avishkar.db.DBAccess;
import com.google.gson.Gson;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class StreamingTest {
	/**
	 * Main entry of this application.
	 *
	 * @param args
	 *            arguments doesn't take effect with this example
	 * @throws TwitterException
	 *             when Twitter service or network is unavailable
	 */
	public static void main(String[] args) throws TwitterException {
		TwitterStream twitterStream = new TwitterStreamFactory(AccessTokenUtil.getConfig()).getInstance();
		final Gson gson = new Gson();
		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {
				try {
					DBAccess.insertTweet(gson.toJson(status));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
		twitterStream.addListener(listener);
		ArrayList<Long> follow = new ArrayList<Long>();
		ArrayList<String> track = new ArrayList<String>();

		ArrayList<String> filterList = new ArrayList<String>();
//		filterList.add("need blood donor");
//		filterList.add("blood donor");
//		filterList.add("blood donation");
//		filterList.add("blood urgent");
//		filterList.add("blood");
		filterList.add("#FriendshipDay");

		track.addAll(filterList);

		long[] followArray = new long[follow.size()];
		for (int i = 0; i < follow.size(); i++) {
			followArray[i] = follow.get(i);
		}
		String[] trackArray = track.toArray(new String[track.size()]);

		// filter() method internally creates a thread which manipulates
		// TwitterStream and calls these adequate listener
		// methods continuously.
		twitterStream.filter(new FilterQuery(0, followArray, trackArray));
	}
}
