package teetime.framework.pipe;

import java.util.LinkedList;

import teetime.framework.InputPort;
import teetime.framework.OutputPort;

public class OrderedGrowablePipe extends AbstractIntraThreadPipe {

	private final LinkedList<Object> elements;

	<T> OrderedGrowablePipe(final OutputPort<? extends T> sourcePort, final InputPort<T> targetPort, final int capacity) {
		super(sourcePort, targetPort);
		this.elements = new LinkedList<Object>();
	}

	@Deprecated
	public static <T> void connect(final OutputPort<? extends T> sourcePort, final InputPort<T> targetPort) {
		final IPipe pipe = new OrderedGrowablePipe(null, null, 100000);
		pipe.connectPorts(sourcePort, targetPort);
	}

	@Override
	public boolean add(final Object element) {
		return this.elements.offer(element);
	}

	@Override
	public Object removeLast() {
		return this.elements.poll();
	}

	@Override
	public boolean isEmpty() {
		return this.elements.isEmpty();
	}

	@Override
	public Object readLast() {
		return this.elements.peek();
	}

	@Override
	public int size() {
		return this.elements.size();
	}

}
