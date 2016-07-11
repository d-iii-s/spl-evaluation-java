package cz.cuni.mff.d3s.spl;

import cz.cuni.mff.d3s.spl.data.Revision;
import cz.cuni.mff.d3s.spl.data.readers.*;
import cz.cuni.mff.d3s.spl.formula.SplFormula;
import cz.cuni.mff.d3s.spl.interpretation.WelchTestInterpretation;

import java.util.List;
import java.util.Map;


/**
 * Type of data reader.
 */
enum DataReaderType {
	RawJson,
	LineOriented,
	NumberOriented
}

/**
 * Main class of the application.
 */
public class Main {
	/**
	 * Significance level for formula evaluation. May be passed as an argument
	 * in future versions.
	 */
	private static final double SIGNIFICANCE_LEVEL = 0.95;

	/**
	 * Entry point of the application.
	 *
	 * @param args Command line arguments. Exact semantics depends on used
	 *             data reader. Number of arguments and their semantics may
	 *             be changed in future.
	 */
	public static void main(String[] args) {
		Map<String, List<Revision>> data = null;

		try {
			DataReader reader = getDataReader(DataReaderType.RawJson);
			//DataReader reader = getDataReader(DataReaderType.LineOriented);

			data = reader.readData(args);
		} catch (DataReader.ReaderException e) {
			System.out.println("Error reading data: " + e.getMessage());
			System.exit(1);
		}

		Formula improved = SplFormula.create("new < old");
		improved.setInterpretation(new WelchTestInterpretation());

		System.out.printf("Looking for regressions and suspicious data...\n");

		// for each test method
		for (Map.Entry<String, List<Revision>> benchmark : data.entrySet()) {
			System.out.printf(" benchmark: %s...\n", benchmark.getKey());

			// for each pair of following measurements check regression
			List<Revision> revisions = benchmark.getValue();
			for (int i = 0; i < revisions.size() - 1; i++) {
				improved.bind("old", revisions.get(i).data);
				improved.bind("new", revisions.get(i + 1).data);

				Result result = improved.evaluate(SIGNIFICANCE_LEVEL);
				if (result != Result.TRUE) {
					System.out.printf("  - possible regression between %s and %s!\n",
							revisions.get(i).name, revisions.get(i + 1).name);
				}
			}
		}
	}

	/**
	 * Create data reader instance according to argument type.
	 *
	 * @param type Specifies which data reader to create.
	 * @return Instance of requested data reader.
	 */
	private static DataReader getDataReader(DataReaderType type) throws DataReader.ReaderException {
		switch (type) {
			case RawJson:
				return new StructuredDataReader<RawJsonRevisionReader>(new RawJsonRevisionReader.RevisionFactory());
			case LineOriented:
				return new PlainDataReader<LineOrientedRevisionReader>(new LineOrientedRevisionReader.RevisionFactory());
			case NumberOriented:
				return new PlainDataReader<NumbersOnlyRevisionReader>(new NumbersOnlyRevisionReader.RevisionFactory());
			default:
				throw new DataReader.ReaderException("Unknown data reader!");
		}
	}
}
