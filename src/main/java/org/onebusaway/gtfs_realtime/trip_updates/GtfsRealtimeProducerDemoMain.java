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

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.lang.IllegalArgumentException;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.onebusaway.cli.CommandLineInterfaceLibrary;
import org.onebusaway.guice.jsr250.LifecycleService;
import org.onebusway.gtfs_realtime.exporter.TripUpdatesFileWriter;
import org.onebusway.gtfs_realtime.exporter.TripUpdatesServlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GtfsRealtimeProducerDemoMain {

    private static final String ARG_UPDATES_PATH = "updatesPath";

    private static final String ARG_UPDATES_URL = "updatesUrl";

    private static final String ARG_MONGO_CLIENT = "mongoClient";
    private static final String ARG_DATABASE_NAME = "dbName";
    private static final String ARG_COLLECTION_NAME = "collectionName";

    private static final String ARG_AGELIM = "ageLim";
    
    public static void main(String[] args) throws Exception {
	GtfsRealtimeProducerDemoMain m = new GtfsRealtimeProducerDemoMain();
	m.run(args);
    }
    
    private GtfsRealtimeProviderImpl _provider;
    
    private LifecycleService _lifecycleService;
    
    @Inject
	public void setProvider(GtfsRealtimeProviderImpl provider) {
	_provider = provider;
    }
    
    @Inject
	public void setLifecycleService(LifecycleService lifecycleService) {
	_lifecycleService = lifecycleService;
    }

    public void run(String[] args) throws Exception {

	if (args.length == 0 || CommandLineInterfaceLibrary.wantsHelp(args)) {
	    printUsage();
	    System.exit(-1);
	}

	Options options = new Options();
	buildOptions(options);
	Parser parser = new GnuParser();
	CommandLine cli = parser.parse(options, args);

	Set<Module> modules = new HashSet<Module>();
	GtfsRealtimeProducerDemoModule.addModuleAndDependencies(modules);

	Injector injector = Guice.createInjector(modules);
	injector.injectMembers(this);

	if (cli.hasOption(ARG_MONGO_CLIENT) && 
	    cli.hasOption(ARG_DATABASE_NAME) &&
	    cli.hasOption(ARG_COLLECTION_NAME)) {
	    _provider.setMongo(cli.getOptionValue(ARG_MONGO_CLIENT));
	    _provider.setDB(cli.getOptionValue(ARG_DATABASE_NAME));
	    _provider.setColl(cli.getOptionValue(ARG_COLLECTION_NAME));
	}
	else
	    throw new IllegalArgumentException("Need MongoClient URI, database name, trip updates collection name.");

	if (cli.hasOption(ARG_AGELIM)) {
	    _provider.setAgeLim(cli.getOptionValue(ARG_AGELIM));
	}

	if (cli.hasOption(ARG_UPDATES_URL)) {
	    URL url = new URL(cli.getOptionValue(ARG_UPDATES_URL));
	    TripUpdatesServlet servlet = injector.getInstance(TripUpdatesServlet.class);
	    servlet.setUrl(url);
	}
	if (cli.hasOption(ARG_UPDATES_PATH)) {
	    File path = new File(cli.getOptionValue(ARG_UPDATES_PATH));
	    TripUpdatesFileWriter writer = injector.getInstance(TripUpdatesFileWriter.class);
	    writer.setPath(path);
	}

	_lifecycleService.start();
    }

    private void printUsage() {
	CommandLineInterfaceLibrary.printUsage(getClass());
    }

    protected void buildOptions(Options options) {
	options.addOption(ARG_UPDATES_PATH, true, "trip updates path");
	options.addOption(ARG_UPDATES_URL, true, "trip updates url");
	options.addOption(ARG_MONGO_CLIENT, true, "MongoDB URI");
	options.addOption(ARG_DATABASE_NAME, true, "database name");
	options.addOption(ARG_COLLECTION_NAME, true, "trip updates collection name");
	options.addOption(ARG_AGELIM, true, "age limit for removal, in ms");
    }
}
