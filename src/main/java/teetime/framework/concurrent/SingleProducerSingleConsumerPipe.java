package teetime.framework.concurrent;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;

import teetime.framework.core.AbstractPipe;
import teetime.framework.core.IInputPort;
import teetime.framework.core.IOutputPort;
import teetime.framework.core.ISink;
import teetime.framework.core.ISource;
import teetime.util.concurrent.workstealing.CircularWorkStealingDeque;

public class SingleProducerSingleConsumerPipe<T> extends AbstractPipe<T> {

	// BETTER use a cache-aware queue (see the corresponding paper)
	final BlockingQueue<T> queue = new LinkedBlockingDeque<T>();

	private final Callable<T> blockingTake = new Callable<T>() {
		public T call() throws Exception {
			return SingleProducerSingleConsumerPipe.this.queue.take();
		}
	};
	private final Callable<T> nonBlockingTake = new Callable<T>() {
		public T call() throws Exception {
			return SingleProducerSingleConsumerPipe.this.queue.poll();
		}
	};
	private volatile Callable<T> take = this.blockingTake;

	public static <S0 extends ISource, S1 extends ISink<S1>, T> void connect(final IOutputPort<S0, T> sourcePort, final IInputPort<S1, T> targetPort) {
		final SingleProducerSingleConsumerPipe<T> pipe = new SingleProducerSingleConsumerPipe<T>();
		pipe.setSourcePort(sourcePort);
		pipe.setTargetPort(targetPort);
	}

	public void putMultiple(final List<T> elements) {
		this.queue.addAll(elements);
	}

	public T read() {
		return this.queue.peek();
	}

	public List<?> tryTakeMultiple(final int numElementsToTake) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void putInternal(final T token) {
		this.queue.add(token);
	}

	@Override
	protected T tryTakeInternal() {
		try {
			return this.take.call();
		} catch (final Exception e) {
			return null;
		}
	}

	public T take() {
		final T token = this.tryTake();
		if (token == null) {
			throw CircularWorkStealingDeque.DEQUE_IS_EMPTY_EXCEPTION;
		}
		return token;
	}

	public boolean isEmpty() {
		return this.queue.isEmpty();
	}

	@Override
	public void close() {
		this.take = this.nonBlockingTake;
		super.close();
		this.getTargetPort().getOwningStage().getOwningThread().interrupt();
	}

}
