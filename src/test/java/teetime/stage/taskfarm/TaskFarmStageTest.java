package teetime.stage.taskfarm;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import teetime.framework.AbstractCompositeStage;
import teetime.framework.AbstractConsumerStage;
import teetime.framework.Analysis;
import teetime.framework.AnalysisConfiguration;
import teetime.framework.OutputPort;
import teetime.framework.Stage;
import teetime.stage.CollectorSink;
import teetime.stage.InitialElementProducer;

@SuppressWarnings("deprecation")
public class TaskFarmStageTest {

	private final static int NUMBER_OF_TEST_ELEMENTS = 1000;

	private class PlusOneInStringStage extends AbstractConsumerStage<Integer> {

		private final OutputPort<String> outputPort = this.createOutputPort();

		@Override
		protected void execute(final Integer element) {
			Integer x = element + 1;
			this.outputPort.send(x.toString());
		}

		public OutputPort<String> getOutputPort() {
			return this.outputPort;
		}
	}

	private class StringDuplicationStage extends AbstractConsumerStage<String> {

		private final OutputPort<String> outputPort = this.createOutputPort();

		@Override
		protected void execute(final String element) {
			this.outputPort.send(element + element);
		}
	}

	private class CompositeTestStage extends AbstractCompositeStage {
		PlusOneInStringStage pOne = new PlusOneInStringStage();
		StringDuplicationStage sDup = new StringDuplicationStage();

		public CompositeTestStage() {
			lastStages.add(sDup);
			connectPorts(pOne.getOutputPort(), sDup.getInputPort());
		}

		@Override
		protected Stage getFirstStage() {
			return pOne;
		}
	}

	private class TestConfiguration extends AnalysisConfiguration {
		private final List<String> collection = new LinkedList<String>();

		public TestConfiguration() {
			this.buildConfiguration();
		}

		private void buildConfiguration() {
			final Stage producerPipeline = this.buildProducerPipeline();
			addThreadableStage(producerPipeline);
		}

		private Stage buildProducerPipeline() {
			List<Integer> values = new LinkedList<Integer>();
			for (int i = 1; i <= NUMBER_OF_TEST_ELEMENTS; i++) {
				values.add(i);
			}
			final InitialElementProducer<Integer> initialElementProducer = new InitialElementProducer<Integer>(values);
			final CompositeTestStage compositeTestStage = new CompositeTestStage();
			final CollectorSink<String> collectorSink = new CollectorSink<String>(collection);

			TaskFarmStage<Integer, String> taskFarmStage = new TaskFarmStage<Integer, String>(compositeTestStage);

			connectIntraThreads(initialElementProducer.getOutputPort(), taskFarmStage.getInputPort());
			connectIntraThreads(taskFarmStage.getOutputPort(), collectorSink.getInputPort());

			return initialElementProducer;
		}

		public List<String> getCollection() {
			return this.collection;
		}
	}

	@Test
	public void simpleTaskFarmStageTest() {
		TestConfiguration configuration = new TestConfiguration();

		final Analysis<AnalysisConfiguration> analysis = new Analysis<AnalysisConfiguration>(configuration);

		analysis.executeBlocking();

		List<String> result = configuration.getCollection();
		assertTrue(result.contains("22"));
		assertTrue(result.contains("33"));
		assertTrue(result.contains("44"));
		assertTrue(result.contains("55"));
		assertTrue(result.contains("66"));
		for (int i = 1; i <= NUMBER_OF_TEST_ELEMENTS; i++) {
			int n = i + 1;
			String s = Integer.toString(n) + Integer.toString(n);
			assertTrue(s, result.contains(s));
		}
		assertTrue(Integer.toString(result.size()), result.size() == NUMBER_OF_TEST_ELEMENTS);
	}
}
