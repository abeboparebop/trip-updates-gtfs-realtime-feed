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

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;

/**
 * A simple Java bean describing one vehicle's location.
 * 
 * @author jlynn
 */
public class Update {

  private final String id;
  private final String trip_id;

  private final String stop_id;
  private final int stop_sequence;

  private final long dep_time;
  private final long dep_delay;

  public Update(String id, String trip_id, String stop_id, 
      int stop_sequence, long dep_time, long dep_delay) {
    this.id = id;
    this.trip_id = trip_id;
    this.stop_id = stop_id;
    this.stop_sequence = stop_sequence;
    this.dep_time = dep_time;
    this.dep_delay = dep_delay;
  }

  public Update(DBObject obj) {
    /* Inspect the DBObject to make sure it has the right fields,
	   then use default constructor. */

    String id = (String) obj.get("device_id");
    String trip_id = (String) obj.get("trip_id");

    String stop_id = (String) obj.get("stop_id");
    int stop_sequence = (int) objectToDouble(obj.get("stop_sequence"));

    long dep_time = (long) objectToDouble(obj.get("time"));
    long dep_delay = (long) objectToDouble(obj.get("delay"));
    
    this.id = id;
    this.trip_id = trip_id;
    this.stop_id = stop_id;
    this.stop_sequence = stop_sequence;
    this.dep_time = dep_time;
    this.dep_delay = dep_delay;
  }

  public String getId() {
    return id;
  }
  public String getTripId() {
    return trip_id;
  }
  public String getStopId() {
    return stop_id;
  }
  public int getStopSequence() {
    return stop_sequence;
  }
  public long getDepTime() {
    return dep_time;
  }
  public long getDepDelay() {
    return dep_delay;
  }

  public FeedEntity.Builder getFeedEntityBuilder() {
    /**
     * Create FeedEntity.Builder for constructing the actual GTFS-realtime
     * locations feed from the location details.
     */
    FeedEntity.Builder new_ent = FeedEntity.newBuilder();

    /* Basic structure for a TripUpdate FeedEntity:

	   FeedEntity entity {
	     String id
	     TripUpdate {
	       TripDescriptor {
	         trip_id
	       }
	       StopTimeUpdate {
	         stop_id
		 stop_sequence
		 departure {
		   time
		   delay
		 }
	       }
	     }
	   }

     */
    new_ent.setId(id);

    TripUpdate.Builder new_up = TripUpdate.newBuilder();

    TripDescriptor.Builder new_desc = TripDescriptor.newBuilder();
    new_desc.setTripId(trip_id);

    new_up.setTrip(new_desc);

    StopTimeUpdate.Builder new_stu = StopTimeUpdate.newBuilder();
    new_stu.setStopId(stop_id);
    new_stu.setStopSequence(stop_sequence);

    // Divide by 1000 b/c MongoDB feed arrives in ms, while Google's 
    // protobuf wants seconds.
    StopTimeEvent.Builder new_ste = StopTimeEvent.newBuilder();
    long time = (long) (((double) dep_time)/1000.0);
    int delay = (int) (((double) dep_delay)/1000.0);
    new_ste.setTime(time);
    new_ste.setDelay(delay);

    new_stu.setDeparture(new_ste);

    new_up.addStopTimeUpdate(new_stu);

    new_ent.setTripUpdate(new_up);

    return new_ent;
  }

  private double objectToDouble(Object obj) {
    String str = obj.toString();
    double d = Double.valueOf(str).doubleValue();
    return d;
  }
}