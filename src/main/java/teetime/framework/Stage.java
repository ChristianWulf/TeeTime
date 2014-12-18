package teetime.framework;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import teetime.framework.signal.ISignal;
import teetime.framework.validation.InvalidPortConnection;

/**
 * Represents a minimal Stage, with some pre-defined methods.
 * Implemented stages need to adapt all abstract methods with own implementations.
 */
@SuppressWarnings("PMD.AbstractNaming")
public abstract class Stage {

	private static final ConcurrentMap<String, Integer> INSTANCES_COUNTER = new ConcurrentHashMap<String, Integer>();

	private final String id;
	/**
	 * A unique logger instance per stage instance
	 */
	@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
	protected final Logger logger;

	private Thread owningThread;

	protected Stage() {
		this.id = this.createId();
		this.logger = LoggerFactory.getLogger(this.getClass().getCanonicalName() + ":" + id);
	}

	/**
	 * @return an identifier that is unique among all stage instances. It is especially unique among all instances of the same stage type.
	 */
	public String getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ": " + this.getId();
	}

	private String createId() {
		String simpleName = this.getClass().getSimpleName();

		Integer numInstances = INSTANCES_COUNTER.get(simpleName);
		if (null == numInstances) {
			numInstances = 0;
		}

		String newId = simpleName + "-" + numInstances;
		INSTANCES_COUNTER.put(simpleName, ++numInstances);
		return newId;
	}

	@SuppressWarnings("PMD.DefaultPackage")
	static void clearInstanceCounters() {
		INSTANCES_COUNTER.clear();
	}

	// public abstract Stage getParentStage();
	//
	// public abstract void setParentStage(Stage parentStage, int index);

	/**
	 * This should check, if the OutputPorts are connected correctly. This is needed to avoid NullPointerExceptions and other errors.
	 *
	 * @param invalidPortConnections
	 *            <i>(Passed as parameter for performance reasons)</i>
	 */
	public abstract void validateOutputPorts(List<InvalidPortConnection> invalidPortConnections);

	protected abstract void executeWithPorts();

	protected abstract void onSignal(ISignal signal, InputPort<?> inputPort);

	protected abstract TerminationStrategy getTerminationStrategy();

	protected abstract void terminate();

	protected abstract boolean shouldBeTerminated();

	public Thread getOwningThread() {
		return owningThread;
	}

	public void setOwningThread(final Thread owningThread) {
		this.owningThread = owningThread;
	}

	protected abstract InputPort<?>[] getInputPorts();
}
