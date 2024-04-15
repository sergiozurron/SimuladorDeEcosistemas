package simulator.launcher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.factories.Builder;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DynamicSupplyRegionBuilder;
import simulator.factories.Factory;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SheepBuilder;
import simulator.factories.WolfBuilder;
import simulator.misc.Utils;
import simulator.model.Animal;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.model.Simulator;

public class Main {
	private static final ExecMode mode = ExecMode.BATCH;

	// Options
	private static final Double DEFAULT_TIME = 10.0; // seconds
	private static final Double DEFAULT_DELTA_TIME = 0.03; // seconds

	private static Double time = null;
	public static Double deltaTime = null;
	private static String inFile = null;
	private static String outFile = null;
	private static boolean sv = false;

	// factories
	public static Factory<Animal> animalFactory;
	public static Factory<Region> regionFactory;

	public static void main(String[] args) {
		Utils.randomGenerator.setSeed(2147483647L);
		try {
			initFactories();
			parseArgs(args);
			if (mode == ExecMode.BATCH)
				startBatchMode();
			else
				startGUIMode();
		} catch (Exception e) {
			System.err.println("Something went wrong ...");
			System.err.println();
			e.printStackTrace();
		}
	}

	private static void parseArgs(String[] args) {
		Options cmdLineOptions = buildOptions();

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parseDeltaTimeOption(line);
			parseHelpOption(line, cmdLineOptions);
			parseInFileOption(line);
			parseOutFileOption(line);
			parseSvOption(line);
			parseTimeOption(line);

			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				StringBuilder error = new StringBuilder("Illegal arguments:");
				for (String o : remaining)
					error.append(" ").append(o);
				throw new ParseException(error.toString());
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options buildOptions() {
		Options cmdLineOptions = new Options();

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		// input file
		cmdLineOptions
				.addOption(Option.builder("i").longOpt("input").hasArg().desc("Initial configuration file.").build());

		// output file
		cmdLineOptions.addOption(
				Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());

		// steps
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("An real number representing the total simulation time in seconds. Default value: " + DEFAULT_TIME
						+ ".")
				.build());

		// delta time
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
				.desc("A double representing actual time, in seconds, per simulation step. Default value: "
						+ DEFAULT_DELTA_TIME + ".")
				.build());
		// simple viewer
		cmdLineOptions.addOption(
				Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());

		return cmdLineOptions;
	}

	private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parseInFileOption(CommandLine line) throws ParseException {
		inFile = line.getOptionValue("i");
		if (mode == ExecMode.BATCH && inFile == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	private static void parseOutFileOption(CommandLine line) {
		outFile = line.getOptionValue("o");
	}

	private static void parseTimeOption(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", DEFAULT_TIME.toString());
		try {
			time = Double.parseDouble(t);
			assert (time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}

	private static void parseSvOption(CommandLine line) {
		sv = line.hasOption("sv");
	}

	private static void parseDeltaTimeOption(CommandLine line) throws ParseException {
		String dt = line.getOptionValue("dt", DEFAULT_DELTA_TIME.toString());
		try {
			deltaTime = Double.parseDouble(dt);
			assert (time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + dt);
		}
	}

	private static void initFactories() {
		Factory<SelectionStrategy> selectionStrategyFactory;
		List<Builder<SelectionStrategy>> selectionStrategyBuilders = new ArrayList<>();
		selectionStrategyBuilders.add(new SelectFirstBuilder());
		selectionStrategyBuilders.add(new SelectClosestBuilder());
		selectionStrategyBuilders.add(new SelectYoungestBuilder());
		selectionStrategyFactory = new BuilderBasedFactory<>(selectionStrategyBuilders);

		List<Builder<Animal>> animalBuilders = new ArrayList<>();
		animalBuilders.add(new SheepBuilder(selectionStrategyFactory));
		animalBuilders.add(new WolfBuilder(selectionStrategyFactory));
		animalFactory = new BuilderBasedFactory<>(animalBuilders);

		List<Builder<Region>> regionBuilders = new ArrayList<>();
		regionBuilders.add(new DefaultRegionBuilder());
		regionBuilders.add(new DynamicSupplyRegionBuilder());
		regionFactory = new BuilderBasedFactory<>(regionBuilders);
	}
	
	private static JSONObject loadJSONFile(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}

	private static void startBatchMode() throws IOException {
		JSONObject input = loadJSONFile(new FileInputStream(inFile));
		OutputStream oStream;
		oStream = outFile != null ? new FileOutputStream(outFile) : System.out;
		Simulator simulator = new Simulator(input.getInt("cols"), input.getInt("rows"), input.getInt("width"),
				input.getInt("height"), animalFactory, regionFactory);
		Controller controller = new Controller(simulator);
		controller.loadData(input);
		controller.run(time, deltaTime, sv, oStream);

		oStream.close();
	}

	private static void startGUIMode() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("GUI mode is not ready yet ...");
	}

	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private final String tag;
		private final String desc;

		ExecMode(String modeTag, String modeDesc) {
			tag = modeTag;
			desc = modeDesc;
		}

		public String getTag() {
			return tag;
		}

		public String getDesc() {
			return desc;
		}
	}
}