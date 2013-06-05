/**
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_realtime.trip_updates;

import java.util.ArrayList;

import javax.inject.Singleton;

import com.mongodb.DBObject;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;

import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeLibrary;

/**
 * A list of the most recent vehicle updates. The list has one element
 * per vehicle. When a new update is inserted, if it corresponds to a 
 * vehicle already in the list, and has a newer timestamp, then the new 
 * update replaces the old. If the new update does not correspond to
 * any existing vehicles, then add it to the list.
 * 
 * @author jlynn
 */
@Singleton
public class UpdateList {
    ArrayList<Update> updates = new ArrayList<Update>();
    ArrayList<Object> busIDs = new ArrayList<Object>();

    public void addUpdate(Update newUpdate) {
	/* New vehicle? Add new Update to list.
	   Old vehicle + new timestamp? Replace old Update w/ new.
	   Old vehicle + old timestamp? Do not add to list.
	 */
	long newTime = newUpdate.getDepTime();
	String newId = newUpdate.getId();
	boolean replace = false;
	boolean addUpdate = true;
	int iReplace = 0;

	int listLen = updates.size();
	for (int i = 0; i < listLen; i++) {
	    Update prevUpdate = updates.get(i);
	    long prevTime = prevUpdate.getDepTime();
	    String prevId = prevUpdate.getId();

	    if (prevId.equals(newId)) {
		addUpdate = false;
		if (prevTime < newTime) {
		    // This bus update replaces a previous one in the feed.
		    iReplace = i;
		    replace = true;
		}
	    }
	}
	if (replace) {
	    // Old vehicle + updated timestamp:
	    updates.set(iReplace, newUpdate);
	}
	else if (addUpdate) {
	    // New vehicle:
	    updates.add(newUpdate);
	}
	return;
    }

    public FeedMessage getUpdateFeedMessage() {
	/**
	 * The FeedMessage.Builder is what we will use to build up 
	 * our GTFS-realtime feed. Add all updates to the feed
	 * and then build and return the result.
	 */
	FeedMessage.Builder feedMessage = 
	    GtfsRealtimeLibrary.createFeedMessageBuilder();
	
	int listLen = updates.size();
	for (int i = 0; i < listLen; i++) {
	    Update newUpdate = updates.get(i);
	    FeedEntity.Builder newEnt = newUpdate.getFeedEntityBuilder();
	    feedMessage.addEntity(newEnt);
	}

	return feedMessage.build();
    }

    public long maxTime() {
	/**
	 * Returns latest timestamp of any vehicle updates.
	 */
	long maxStamp = 0L;

	int listLen = updates.size();
	for (int i = 0; i < listLen; i++) {
	    Update update = updates.get(i);
	    long stamp = update.getDepTime();
	    if (stamp > maxStamp) {
		maxStamp = stamp;
	    }
	}
	return maxStamp;
    }

    public void setBusIDs(ArrayList<Object> newBusIDs) {
	busIDs = newBusIDs;
    }

    public ArrayList getBusIDs() {
	return busIDs;
    }

    public void clearOld(long ageLim) {
	Long timeNow = System.currentTimeMillis();

	ArrayList<Update> newUpdates = new ArrayList<Update>();
	
	for (Update update : updates) {
	    if (timeNow - update.getDepTime() < ageLim) {
		newUpdates.add(update);
	    }
	}

	updates = newUpdates;
    }
}