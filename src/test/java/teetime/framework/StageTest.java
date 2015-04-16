/**
 * Copyright (C) 2015 Christian Wulf, Nelson Tavares de Sousa (http://teetime.sourceforge.net)
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
package teetime.framework;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import teetime.framework.pipe.IPipeFactory;
import teetime.framework.pipe.PipeFactoryRegistry.PipeOrdering;
import teetime.framework.pipe.PipeFactoryRegistry.ThreadCommunication;
import teetime.stage.Cache;
import teetime.stage.Counter;
import teetime.stage.InitialElementProducer;

public class StageTest {

	@Test
	public void testId() {
		Stage.clearInstanceCounters();

		Counter<Object> counter0 = new Counter<Object>();
		Counter<Object> counter1 = new Counter<Object>();
		Assert.assertEquals("Counter-0", counter0.getId());
		Assert.assertEquals("Counter-1", counter1.getId());

		for (int i = 0; i < 100; i++) {
			Cache<Object> cache = new Cache<Object>();
			Assert.assertEquals("Cache-" + i, cache.getId());
		}
	}

	@Test
	public void testSetOwningThread() throws Exception {
		TestConfig tc = new TestConfig();
		new Analysis<TestConfig>(tc);
		assertEquals(tc.init.owningThread, tc.delay.owningThread);
	}

	private static class TestConfig extends AnalysisConfiguration {
		final IPipeFactory intraFact = PIPE_FACTORY_REGISTRY.getPipeFactory(ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false);
		public final DelayAndTerminate delay;
		public InitialElementProducer<String> init;

		public TestConfig() {
			init = new InitialElementProducer<String>("Hello");
			delay = new DelayAndTerminate(0);
			intraFact.create(init.getOutputPort(), delay.getInputPort());
			addThreadableStage(init);
		}
	}

	private static class DelayAndTerminate extends AbstractConsumerStage<String> {

		private final long delayInMs;

		public boolean finished;

		public DelayAndTerminate(final long delayInMs) {
			super();
			this.delayInMs = delayInMs;
		}

		@Override
		protected void execute(final String element) {
			try {
				Thread.sleep(delayInMs);
			} catch (InterruptedException e) {
			}
			finished = true;
		}

	}

}
