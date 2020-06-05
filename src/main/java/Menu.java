import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class Menu {
	private static Scanner scanner = new Scanner(System.in).useDelimiter("\n");
	
	abstract void run();
	
	protected static String getNextLine() {
		return scanner.next();
	}
	
	protected static String askUserAQuestion(String question) {
		System.out.println(question);
		return scanner.next();
	}
	
	protected static void askUserAYesNoQuestion(String question, Runnable procedureIfNo, Runnable procedureIfYes) {
		askUserAYesNoQuestion(question, procedureIfNo, procedureIfNo, procedureIfYes);
	}
	
	protected static void askUserAYesNoQuestion(String question, Runnable procedureIfBack, Runnable procedureIfNo, Runnable procedureIfYes) {
		System.out.println(question);
		String answer = scanner.next().toLowerCase();
		if (answer.equals("exit")) {
			System.exit(0);
		} else if (answer.equals("back")) {
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
	
	protected static String giveUserAChoice(List<String> numberedOptions) {
		return giveUserAChoice(numberedOptions, new ArrayList<>());
	}
	
	protected static String giveUserAChoice(List<String> numberedOptions, List<List<String>> finalOptions) {
		List<String> validOptions = validOptions(numberedOptions, finalOptions);
		String formattedOptions = formatOptions(numberedOptions, finalOptions);
		System.out.print(formattedOptions);
		String userChoice = null;
		boolean choiceIsValid = false;
		while (!choiceIsValid) {
			userChoice = getUserChoice();
			choiceIsValid = validOptions.contains(userChoice) ||
					userChoice.toLowerCase().equals("exit") ||
					userChoice.toLowerCase().equals("back");
			if (!choiceIsValid) {
				System.out.println("Your choice was invalid. Please try again.");
			}
		}
		return userChoice;
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
		return scanner.next();
	}
}
