import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class Menu {
	private static Scanner scanner = new Scanner(System.in);
	
	abstract void run();
	
	protected static String askUserAQuestion(String question) {
		System.out.println(question);
		return scanner.next();
	}
	
	protected static String giveUserAChoice(List<String> numberedOptions, List<List<String>> finalOptions) {
		List<String> validOptions = validOptions(numberedOptions, finalOptions);
		String formattedOptions = formatOptions(numberedOptions, finalOptions);
		System.out.println(formattedOptions);
		String userChoice = null;
		boolean choiceIsValid = false;
		while (!choiceIsValid) {
			userChoice = getUserChoice();
			choiceIsValid = validOptions.contains(userChoice);
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
