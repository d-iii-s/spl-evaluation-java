package cz.cuni.mff.d3s.spl.utils;

/**
 *
 */
public interface Factory<T> {
	/**
	 * Factory method for creating generic type class instance.
	 *
	 * @return New class instance.
	 */
	T getInstance();
}
