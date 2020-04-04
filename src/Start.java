import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Start {
    private static Scanner scanner;
    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        System.out.println("Welcome to spaced repetition!");
        AccountManager.loadAccounts();
        int numberOfAccounts = AccountManager.getNumberOfAccounts();
        if (numberOfAccounts > 0) {
            System.out.println("Choose an account to practice with:");
            List<List<String>> finalOptions = new ArrayList<>(1);
            List<String> createAccountOption = new ArrayList<>(1);
            createAccountOption.add("*");
            createAccountOption.add("Create a new account");
            finalOptions.add(createAccountOption);
            String userChoice = giveUserAChoice(AccountManager.getAccountNames(), finalOptions);
            if (userChoice.equals("*")) {
                createNewAccountMenu();
            } else {
                System.out.println("This path is under development.");
            }
        } else {
            System.out.println("No accounts could be found on this machine.");
            List<String> numberedOptions = new ArrayList<>(1);
            numberedOptions.add("Create a new account");
            List<List<String>> finalOptions = new ArrayList<>(1);
            List<String> exitOption = new ArrayList<>(1);
            exitOption.add("*");
            exitOption.add("Exit");
            finalOptions.add(exitOption);
            String userChoice = giveUserAChoice(numberedOptions, finalOptions);
            if (userChoice.equals("1")) {
                createNewAccountMenu();
            }
        }
    }
    
    private static void createNewAccountMenu() {
        System.out.println(
                "Okay, let's create a new account.\n" +
                "What do you want to name it?"
        );
        String newAccountName = scanner.next();
        boolean accountCreationSuccessful = AccountManager.createNewAccount(newAccountName);
        if (accountCreationSuccessful) {
            System.out.println("Your new account has been created!");
        } else {
            System.out.println(
                    "Your account couldn't be created :(\n" +
                    "Perhaps that name is already taken, or perhaps this program does not have permission to write to the directory."
            );
        }
    }
    
    private static String giveUserAChoice(List<String> numberedOptions, List<List<String>> finalOptions) {
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
        int optionNumber = 1;
        for (String option: numberedOptions) {
            validOptions.add(String.valueOf(optionNumber));
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
