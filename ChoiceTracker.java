
/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.SystemState;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * This listener is a generic choice tracker tool. When JPF finishes it produces
 * a list of choice values that can be used to create readable replay scripts
 * etc.
 * 
 * How to use this class: This listener only displays output if
 * 'propertyVioalted' occurs, i.e., in your app, the ChoiceTracker tracks the
 * choices made leading to an exception.
 * 
 * There are some customization options available for the user, add these to
 * your .jpf or config file. Usage: choice.<action>=<option>
 * 
 * choice.excludes : provide a comma separated list of types to exclude from the
 * tracker
 * 
 * choice.class : provide a comma separated list of types to keep track of
 * (leave empty for all types)
 * 
 * choice.format : choose between formats CG and CHOICE
 * 
 * choice.show_location : proivde a boolean to toggle on or off, shows location
 * of choices, default is true
 * 
 * @author unknown
 * @author Anji Tong
 */
public class ChoiceTracker extends ListenerAdapter implements PublisherExtension {
	private enum Format {
		CG, CHOICE
	};

	private VM vm;

	protected PrintWriter writer;
	private Class<?>[] cgClasses;
	private boolean isReportExtension;

	private boolean showLocation;
	private Format format;
	private String[] excludes;

	private final static int CASE_CHOICE_INDEX = 16;
	private final static int CHOICEGENERATOR_CHOICE_INDEX = 23;
	private final static String FOUR_SPACES_LEFT_INDENT_INTEGER = "%4d: ";

	/**
	 * Initializes this listener without a given file name to be written to.
	 *
	 * @param config for JPF's configuration
	 * @param jpf    JPF
	 */
	public ChoiceTracker(Config config, JPF jpf) {
		vm = jpf.getVM();

		String traceFileName = config.getString("choice.trace");
		System.out.println("file trace name: " + traceFileName);
		if (traceFileName == null) {
			isReportExtension = true;
			jpf.addPublisherExtension(ConsolePublisher.class, this);
			// writer is going to be set later
		} else {
			try {
				writer = new PrintWriter(traceFileName);
			} catch (FileNotFoundException fnfx) {
				System.err.println("cannot write choice trace to file: " + traceFileName);
				writer = new PrintWriter(System.out);
			}
		}

		excludes = config.getStringArray("choice.exclude");
		cgClasses = config.getClasses("choice.class");

		format = config.getEnum("choice.format", Format.values(), Format.CG);
		showLocation = config.getBoolean("choice.show_location", true);
	}

	/**
	 * Sets the global collection of excluded strings.
	 * 
	 * @param excludedString String... - array of strings to be excluded
	 */
	public void setExcludes(String... excludedString) {
		excludes = excludedString;
	}

	/**
	 * Checks to see if the ChoiceGenerator in question is among the list of allowed
	 * ChoiceGenerators, if no list is provided assume true for all choices.
	 *
	 * @param choiceGenerator ChoiceGenerator the ChoiceGenerator class
	 */
	private boolean isRelevantCG(ChoiceGenerator<?> choiceGenerator) {
		if (cgClasses == null) {
			return true;
		} else {
			for (Class<?> cls : cgClasses) {
				if (cls.isAssignableFrom(choiceGenerator.getClass())) {
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * Appends error propertyViolated and its details to written file if encountered
	 * during a search.
	 * 
	 * @param search JPF's search
	 */
	@Override
	public void propertyViolated(Search search) {

		if (!isReportExtension) {

			writer.print("// application: ");
			writer.println(search.getVM().getSUTDescription());

			if (cgClasses == null) {
				writer.println("// trace over all CG classes");
			} else {
				writer.print("// trace over CG types: ");
				for (Class<?> cgClass : cgClasses) {
					writer.print(cgClass.getName());
					writer.print(' ');
				}
				writer.println();
			}

			writer.println("//------------------------- choice trace");
			printChoices();

			writer.println("//------------------------- end choice trace");
			writer.flush();
		}
	}

	/**
	 * After JPF finishes, prints all choices to written file.
	 */
	public void printChoices() {
		int i = 0;
		SystemState systemState = vm.getSystemState();
		ChoiceGenerator<?>[] cgStack = systemState.getChoiceGenerators();

		for (ChoiceGenerator<?> choiceGenerator : cgStack) {
			if (isRelevantCG(choiceGenerator) && !choiceGenerator.isDone()) {
				Object choice = choiceGenerator.getNextChoice();
				if (choice != null) {
					if (excludes != null) {
						for (String exludedString : excludes) {
							if (!choice.toString().startsWith(exludedString)) {
								printRelevant(choice, choiceGenerator, i++);
							}
						}

					}

					printRelevant(choice, choiceGenerator, i++);
				}
			}
		}

	}

	/**
	 * Refactored duplicate printing logic for whether or not there are exclusions
	 * Refactored from switch cases, breaks, and continues
	 * 
	 * @param choice          Object - the current choice
	 * @param choiceGenerator ChoiceGenerator - the ChoiceGenerator class
	 * @param i               int - the current choice index
	 */
	private void printRelevant(Object choice, ChoiceGenerator<?> choiceGenerator, int i) {
		String nextLine = null;
		if (format.equals(Format.CHOICE)) {
			nextLine = choice.toString();
			if (nextLine.startsWith("gov.nasa.jpf.vm.")) {
				nextLine += nextLine.substring(CASE_CHOICE_INDEX);
			}
		} else if (format.equals(Format.CG)) {
			nextLine = choiceGenerator.toString();
			if (nextLine.startsWith("gov.nasa.jpf.vm.choice.")) {
				nextLine = nextLine.substring(CHOICEGENERATOR_CHOICE_INDEX);
			}
		}

		if (nextLine != null) {
			writer.print(String.format(FOUR_SPACES_LEFT_INDENT_INTEGER, i));
			writer.print(nextLine);

			if (showLocation) {
				String sourceLocation = choiceGenerator.getSourceLocation();
				if (sourceLocation != null) {
					writer.println();
					writer.print(" \tat ");
					writer.print(sourceLocation);
				}
			}
			writer.println();
		}
	}

	// --- the PublisherExtension interface
	/**
	 * Appends error publishPropertyViolation and its details to written file if
	 * encountered during a run.
	 * 
	 * @param publisher JPF's Publisher
	 */
	@Override
	public void publishPropertyViolation(Publisher publisher) {
		writer = publisher.getOut();
		publisher.publishTopicStart("choice trace " + publisher.getLastErrorId());
		printChoices();
	}

}
