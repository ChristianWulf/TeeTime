package teetime.util.concurrent.queue.takestrategy;

import java.util.Queue;

public interface TakeStrategy<E>
{
	void signal();

	E waitPoll(Queue<E> q) throws InterruptedException;
}
