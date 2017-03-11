package cz.cuni.mff.d3s.spl.data;

import cz.cuni.mff.d3s.spl.DataSource;

/**
 * One data revision.
 */
public class Revision {
	/**
	 * Name of the revision. Could be file name, version control
	 * system hash, etc.
	 */
	public String name;

	/**
	 * Data for given revision.
	 */
	public DataSource data;

	/**
	 * Constructor.
	 *
	 * @param n Name of the revision.
	 * @param d Revision data.
	 */
	public Revision(String n, DataSource d) {
		name = n;
		data = d;
	}
}
