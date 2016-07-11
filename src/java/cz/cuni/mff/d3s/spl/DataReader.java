package cz.cuni.mff.d3s.spl;

import cz.cuni.mff.d3s.spl.data.Revision;

import java.util.List;
import java.util.Map;


/**
 * Provides unified interface to read measured data.
 *
 * Each implementation will specify it's argument semantics. Most
 * of the readers use one of RevisionReaders to fetch single revision
 * data.
 */
public interface DataReader {
	/**
	 * Reads measured data.
	 *
	 * @param args Specific arguments for each reader.
	 * @return Map of benchmark/method/... name ("default" if
	 *          the data doesn't provide custom name) and list
	 *          of data revisions.
	 */
	Map<String, List<Revision>> readData(String[] args) throws ReaderException;


	/**
	 * Exception indicating error when reading data. Possible causes
	 * are IO error, wrong data format, invalid arguments, etc.
	 */
	class ReaderException extends Exception {

		/**
		 * Standard constructor.
		 */
		public ReaderException() {}

		/**
		 * Constructor which specify error message.
		 *
		 * @param message description of error
		 */
		public ReaderException(String message) {
			super(message);
		}

		/**
		 * Specify error message and exception which caused this particular one.
		 *
		 * @param message description of error
		 * @param cause exception which cause this one to be thrown
		 */
		public ReaderException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Construct exception which was thrown as reaction to given one.
		 *
		 * @param cause exception which cause this one to be thrown
		 */
		public ReaderException(Throwable cause) {
			super(cause);
		}
	}
}
