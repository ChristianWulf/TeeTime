/**
 * Copyright © 2015 Christian Wulf, Nelson Tavares de Sousa (http://teetime-framework.github.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package teetime.examples.wordcounter;

import java.io.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.primitives.Longs;

import teetime.framework.AbstractPort;
import teetime.framework.Execution;
import teetime.framework.TeeTimeService;
import teetime.framework.pipe.IMonitorablePipe;
import teetime.framework.scheduling.globaltaskqueue.GlobalTaskQueueScheduling;
import teetime.stage.basic.distributor.strategy.NonBlockingRoundRobinStrategy;
import teetime.stage.util.CountingMap;
import teetime.util.StopWatch;

public class WordCounterTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(WordCounterTest.class);

	public static void writeTimingsToFile(final File outputFile, final long[] timings) throws UnsupportedEncodingException, FileNotFoundException {
		final PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile, true), 8192 * 8), false, "UTF-8");
		try {
			final Joiner joiner = com.google.common.base.Joiner.on(' ');
			final String timingsString = joiner.join(Longs.asList(timings));
			ps.println(timingsString);
		} finally {
			ps.close();
		}
	}

	public static void main(final String[] args) throws UnsupportedEncodingException, FileNotFoundException {
		String numWorkerThreadsParam = (args.length > 0) ? args[0] : "3";
		String numWarmUpsParam = (args.length > 1) ? args[1] : "1";
		String fileNameParam = (args.length > 2) ? args[2] : "no default file name";
		String monitoringEnabledParam = (args.length > 3) ? args[3] : "true";

		int numWorkerThreads = parseAsInteger(numWorkerThreadsParam, 3);
		LOGGER.info("# worker threads: " + numWorkerThreads);

		int numWarmUps = parseAsInteger(numWarmUpsParam, 1);
		LOGGER.info("# warm ups: " + numWarmUps);

		final String fileName = fileNameParam;
		final File testFile = new File(fileName);

		boolean monitoringEnabled = Boolean.valueOf(monitoringEnabledParam);

		final long[] timings = new long[1];
		final StopWatch stopWatch = new StopWatch();

		for (int i = 0; i < numWarmUps; i++) {
			LOGGER.info("Warm up #" + i);
			final WordCounterConfiguration wcc = new WordCounterConfiguration(numWorkerThreads, testFile);
			final TeeTimeService scheduling = new GlobalTaskQueueScheduling(numWorkerThreads, wcc);
			final Execution<?> analysis = new Execution<WordCounterConfiguration>(wcc, true, scheduling);

			stopWatch.start();
			analysis.executeBlocking();
			stopWatch.end();

			LOGGER.info("duration: " + TimeUnit.NANOSECONDS.toSeconds(stopWatch.getDurationInNs()) + " secs");
		}

		LOGGER.info("Starting analysis...");
		final WordCounterConfiguration wcc = new WordCounterConfiguration(numWorkerThreads, testFile);
		final Execution<?> analysis = new Execution<WordCounterConfiguration>(wcc);

		if (monitoringEnabled) {
			wcc.getMonitoringThread().start();
		}
		stopWatch.start();
		analysis.executeBlocking();
		stopWatch.end();
		wcc.getMonitoringThread().terminate();

		LOGGER.info("duration: " + TimeUnit.NANOSECONDS.toSeconds(stopWatch.getDurationInNs()) + " secs");
		timings[0] = stopWatch.getDurationInNs();

		// results for some words to verify the correctness of the word counter
		final CountingMap<String> map = wcc.getResult();
		System.out.println("vero: " + (map.get("vero") == 3813850) + "->" + map.get("vero") + " should be " + 3813850);
		System.out.println("sit: " + (map.get("sit") == 7627700) + "->" + map.get("sit") + " should be " + 7627700);

		final File outputFile = new File("timings.txt");
		writeTimingsToFile(outputFile, timings);

		// some statistics about the output pipes of the distributor
		System.out.println("distributor pipes:");
		for (final AbstractPort<?> port : wcc.getDistributorPorts()) {
			final IMonitorablePipe spscPipe = (IMonitorablePipe) port.getPipe();
			System.out.println("numWaits: " + spscPipe.getNumWaits());
		}
		System.out.println("distributor waits: " + ((NonBlockingRoundRobinStrategy) wcc.getDistributor().getStrategy()).getNumWaits());

		// some statistics about the output pipes of the distributor
		System.out.println("merger pipes:");
		for (final AbstractPort<?> port : wcc.getMergerPorts()) {
			final IMonitorablePipe spscPipe = (IMonitorablePipe) port.getPipe();
			System.out.println("numWaits: " + spscPipe.getNumWaits());
		}
	}

	private static int parseAsInteger(final String value, final int defaultValue) {
		int numWorkerThreads;
		try {
			numWorkerThreads = Integer.valueOf(value);
		} catch (final NumberFormatException e) {
			numWorkerThreads = defaultValue;
		}
		return numWorkerThreads;
	}
}
