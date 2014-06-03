package teetime.framework.core;

/**
 * 
 * @author Christian Wulf
 * 
 * @param <S>
 *            the stage, this port belongs to<br>
 *            <i>(used for ensuring type safety)</i>
 * @param <T>
 */
public interface IInputPort<S extends IStage, T> extends IPort<S, T> {

	/**
	 * @since 1.10
	 */
	enum State {
		OPEN, CLOSING
	}

	/**
	 * @since 1.10
	 */
	public abstract State getState();

	/**
	 * @since 1.10
	 */
	public abstract void setState(final State state);

	/**
	 * @since 1.10
	 */
	public abstract void setPortListener(final IPortListener<S> stageListener);

	/**
	 * @since 1.10
	 */
	public void close();
}
