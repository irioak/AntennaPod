package de.podfetcher.feed;

import java.util.ArrayList;


import de.podfetcher.storage.*;
import android.content.Context;
import android.database.Cursor;


/**
 * Singleton class
 * Manages all feeds, categories and feeditems
 *  
 *
 *  */
public class FeedManager {
	
	private static FeedManager singleton;
	
	public ArrayList<Feed> feeds;
	public ArrayList<FeedCategory> categories;
	
	Cursor feedlistCursor;

	
	private FeedManager() {
		feeds = new ArrayList<Feed>();
		categories = new ArrayList<FeedCategory>();

	}
	
	public static FeedManager getInstance(){
		if(singleton == null) {
			singleton = new FeedManager();
		}
		return singleton;		
	}

	/** Add and Download a new Feed */
	public void addFeed(Context context, String url) {
		// TODO Check if URL is correct
		PodDBAdapter adapter = new PodDBAdapter(context);
		Feed feed = new Feed(url);
		feed.id = adapter.setFeed(feed);
		
		DownloadRequester req = DownloadRequester.getInstance();
		req.downloadFeed(context, feed);
		
	}
	
	/** Reads the database */
	public void loadDBData(Context context) {
		PodDBAdapter adapter = new PodDBAdapter(context);
		feedlistCursor = adapter.getAllFeedsCursor();
		updateArrays(context);
	}
	
	
	public void updateArrays(Context context) {
		feedlistCursor.requery();
		PodDBAdapter adapter = new PodDBAdapter(context);
		feeds.clear();
		categories.clear();
		extractFeedlistFromCursor(context);		
	}
	
	private void extractFeedlistFromCursor(Context context) {
		PodDBAdapter adapter = new PodDBAdapter(context);
		if(feedlistCursor.moveToFirst()) {
			do {
				Feed feed = new Feed();
				
				feed.id = feedlistCursor.getLong(feedlistCursor.getColumnIndex(PodDBAdapter.KEY_ID));
				feed.title = feedlistCursor.getString(feedlistCursor.getColumnIndex(PodDBAdapter.KEY_TITLE));
				feed.link = feedlistCursor.getString(feedlistCursor.getColumnIndex(PodDBAdapter.KEY_LINK));
				feed.description = feedlistCursor.getString(feedlistCursor.getColumnIndex(PodDBAdapter.KEY_DESCRIPTION));
				feed.image = adapter.getFeedImage(feed);
				feed.file_url = feedlistCursor.getString(feedlistCursor.getColumnIndex(PodDBAdapter.KEY_FILE_URL));
				feed.download_url = feedlistCursor.getString(feedlistCursor.getColumnIndex(PodDBAdapter.KEY_DOWNLOAD_URL));
				
				// Get FeedItem-Object
				Cursor itemlistCursor = adapter.getAllItemsOfFeedCursor(feed);
				feed.items = extractFeedItemsFromCursor(context, itemlistCursor);
				
				feeds.add(feed);
			}while(feedlistCursor.moveToNext());
		}
	}
	
	private ArrayList<FeedItem> extractFeedItemsFromCursor(Context context, Cursor itemlistCursor) {
		ArrayList<FeedItem> items = new ArrayList<FeedItem>();
		PodDBAdapter adapter = new PodDBAdapter(context);
		
		if(itemlistCursor.moveToFirst()) {
			do {
				FeedItem item = new FeedItem();
				
				item.id = itemlistCursor.getLong(itemlistCursor.getColumnIndex(PodDBAdapter.KEY_ID));
				item.title = itemlistCursor.getString(itemlistCursor.getColumnIndex(PodDBAdapter.KEY_TITLE));
				item.link = itemlistCursor.getString(itemlistCursor.getColumnIndex(PodDBAdapter.KEY_LINK));
				item.description = itemlistCursor.getString(itemlistCursor.getColumnIndex(PodDBAdapter.KEY_DESCRIPTION));
				item.pubDate = itemlistCursor.getString(itemlistCursor.getColumnIndex(PodDBAdapter.KEY_PUBDATE));
				item.media = adapter.getFeedMedia(itemlistCursor.getLong(itemlistCursor.getColumnIndex(PodDBAdapter.KEY_MEDIA)));
				item.read = (itemlistCursor.getInt(itemlistCursor.getColumnIndex(PodDBAdapter.KEY_READ)) > 0) ? true : false;
				
				items.add(item);
			} while(itemlistCursor.moveToNext());
		}
		return items;
	}
	
	 
	

}
