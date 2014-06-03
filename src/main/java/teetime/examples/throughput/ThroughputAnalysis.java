/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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
 ***************************************************************************/
package teetime.examples.throughput;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import teetime.framework.concurrent.StageTerminationPolicy;
import teetime.framework.concurrent.WorkerThread;
import teetime.framework.core.Analysis;
import teetime.framework.core.IPipeline;
import teetime.framework.core.IStage;
import teetime.framework.core.Pipeline;
import teetime.framework.sequential.QueuePipe;
import teetime.stage.NoopFilter;
import teetime.stage.basic.ObjectProducer;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public class ThroughputAnalysis<T> extends Analysis {

	private static final int SECONDS = 1000;

	private WorkerThread workerThread;

	private int numNoopFilters;

	private int numInputObjects;

	private Callable<T> inputObjectCreator;

	@Override
	public void init() {
		super.init();
		final IPipeline pipeline = this.buildPipeline(this.numNoopFilters);

		this.workerThread = new WorkerThread(pipeline, 0);
		this.workerThread.terminate(StageTerminationPolicy.TERMINATE_STAGE_AFTER_UNSUCCESSFUL_EXECUTION);
	}

	/**
	 * @param numNoopFilters
	 * @since 1.10
	 */
	private IPipeline buildPipeline(final int numNoopFilters) {
		@SuppressWarnings("unchecked")
		final NoopFilter<T>[] noopFilters = new NoopFilter[numNoopFilters];
		// create stages
		final ObjectProducer<T> objectProducer = new ObjectProducer<T>(this.numInputObjects, this.inputObjectCreator);
		for (int i = 0; i < noopFilters.length; i++) {
			noopFilters[i] = new NoopFilter<T>();
		}

		// add each stage to a stage list
		final List<IStage> startStages = new LinkedList<IStage>();
		startStages.add(objectProducer);

		final List<IStage> stages = new LinkedList<IStage>();
		stages.add(objectProducer);
		stages.addAll(Arrays.asList(noopFilters));

		// connect stages by pipes
		QueuePipe.connect(objectProducer.outputPort, noopFilters[0].inputPort);
		for (int i = 1; i < noopFilters.length; i++) {
			QueuePipe.connect(noopFilters[i - 1].outputPort, noopFilters[i].inputPort);
		}

		final Pipeline pipeline = new Pipeline();
		pipeline.setStartStages(startStages);
		pipeline.setStages(stages);
		return pipeline;
	}

	@Override
	public void start() {
		super.start();

		this.workerThread.start();
		try {
			this.workerThread.join(60 * SECONDS);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("SchedulingOverhead: " + TimeUnit.NANOSECONDS.toMillis(this.workerThread.computeSchedulingOverheadInNs()) + " ms");
	}

	public int getNumNoopFilters() {
		return this.numNoopFilters;
	}

	public void setNumNoopFilters(final int numNoopFilters) {
		this.numNoopFilters = numNoopFilters;
	}

	/**
	 * @since 1.10
	 */
	public void setInput(final int numInputObjects, final Callable<T> inputObjectCreator) {
		this.numInputObjects = numInputObjects;
		this.inputObjectCreator = inputObjectCreator;
	}
}
