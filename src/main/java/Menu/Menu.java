package Menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public abstract class Menu {
	private static final String EXIT_COMMAND = "exit";
	private static final String BACK_COMMAND = "back";
	private static final Scanner SCANNER = new Scanner(System.in);
	
	public abstract void run();
	
	protected static String getNextLine() {
		return SCANNER.nextLine();
	}
	
	protected static void askUserAQuestion(String question, Runnable procedureIfBack, Consumer<String> continueProcedure) {
		System.out.println(question);
		String answer = SCANNER.nextLine();
		if (answer.toLowerCase().equals(EXIT_COMMAND)) {
			System.exit(0);
		} else if (answer.toLowerCase().equals(BACK_COMMAND)) {
			procedureIfBack.run();
		} else {
			continueProcedure.accept(answer);
		}
	}
	
	protected static void askUserAYesNoQuestion(String question, Runnable procedureIfNo, Runnable procedureIfYes) {
		askUserAYesNoQuestion(question, procedureIfNo, procedureIfNo, procedureIfYes);
	}
	
	protected static void askUserAYesNoQuestion(String question, Runnable procedureIfBack, Runnable procedureIfNo, Runnable procedureIfYes) {
		System.out.println(question);
		String answer = SCANNER.nextLine().toLowerCase();
		if (answer.equals(EXIT_COMMAND)) {
			System.exit(0);
		} else if (answer.equals(BACK_COMMAND)) {
			procedureIfBack.run();
		} else if (List.of("no", "n").contains(answer)) {
			procedureIfNo.run();
		} else if (List.of("yes", "y").contains(answer)) {
			procedureIfYes.run();
		} else {
			System.out.println("Please type yes or no.");
			askUserAYesNoQuestion(question, procedureIfNo, procedureIfYes);
		}
	}
	
	protected static void giveUserAChoice(List<String> numberedOptions, Runnable procedureIfBack, Consumer<String> continueProcedure) {
		giveUserAChoice(numberedOptions, new ArrayList<>(), procedureIfBack, continueProcedure);
	}
	
	protected static void giveUserAChoice(List<String> numberedOptions, List<List<String>> finalOptions, Runnable procedureIfBack, Consumer<String> continueProcedure) {
		List<String> validOptions = validOptions(numberedOptions, finalOptions);
		String formattedOptions = formatOptions(numberedOptions, finalOptions);
		System.out.print(formattedOptions);
		String userChoice = null;
		boolean choiceIsValid = false;
		while (!choiceIsValid) {
			userChoice = getUserChoice();
			choiceIsValid = validOptions.contains(userChoice) ||
					userChoice.toLowerCase().equals(EXIT_COMMAND) ||
					userChoice.toLowerCase().equals(BACK_COMMAND);
			if (!choiceIsValid) {
				System.out.println("Your choice was invalid. Please try again.");
			}
		}
		if (userChoice.toLowerCase().equals(EXIT_COMMAND)) {
			System.exit(0);
		} else if (userChoice.toLowerCase().equals(BACK_COMMAND)) {
			procedureIfBack.run();
		} else {
			continueProcedure.accept(userChoice);
		}
	}
	
	private static List<String> validOptions(List<String> numberedOptions, List<List<String>> finalOptions) {
		List<String> validOptions = new ArrayList<>(numberedOptions.size() + finalOptions.size());
		for (int i = 1; i < numberedOptions.size() + 1; i++) {
			validOptions.add(String.valueOf(i));
		}
		for (List<String> finalOption : finalOptions) {
			validOptions.add(finalOption.get(0));
		}
		return validOptions;
	}
	
	private static String formatOptions(List<String> numberedOptions, List<List<String>> finalOptions) {
		String formattedOptions = "";
		int optionNumber = 1;
		for (String option : numberedOptions) {
			formattedOptions += "(" + optionNumber + ") " + option + "\n";
			optionNumber++;
		}
		for (List<String> finalOption : finalOptions) {
			formattedOptions += "(" + finalOption.get(0) + ") " + finalOption.get(1) + "\n";
		}
		return formattedOptions;
	}
	
	private static String getUserChoice() {
		System.out.print("Your choice: ");
		return SCANNER.nextLine();
	}
}
