trip-updates-gtfs-realtime-feed
========================================

Uses OneBusAway backend to convert MongoDB data into a GTFS-realtime trip update feed.

## Background

This app is primarily based on Brian Ferris's demo code [here](https://github.com/OneBusAway/onebusaway-gtfs-realtime-alerts-producer-demo) which converts SEPTA alerts into a GTFS-realtime feed. I've modified it to pull data from a MongoDB instance, to be specified by the end user. The code assumes that the data has a particular structure. Specifically, the MongoDB data elements must have (minimally) the following JSON structure:

```
{
  "entity": {
    "id": String,
    "trip_update": {
      "trip": { 
        "trip_id" : String
        },
      "stop_time_update": {
        "stop_id": String,
        "stop_sequence": int,
        "departure": {
          "time": long,
          "delay": int
        }
      }
    }
  }
}
```

## Building

After cloning the repository, run
```
mvn package
```
in root directory. Maven should resolve dependencies.

## Running

From root:

```
java -jar target/onebusaway-gtfs-realtime-trip-updates-0.0.1-SNAPSHOT.jar --updatesUrl=URL --mongoClient=MongoDB_URI --dbName=database_name --collectionName=collection_name
```

To demo locally, use e.g. http://localhost:8080/updates as the updatesURL. A human-readable version of the GTFS-realtime feed will be accessible at http://localhost:8080/updates?debug. Alternatively the user may specify a file with `--updatesPath=path` to which the GTFS-realtime feed will be written.
