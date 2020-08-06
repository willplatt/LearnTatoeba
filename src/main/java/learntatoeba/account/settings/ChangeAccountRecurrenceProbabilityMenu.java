package learntatoeba.account.settings;

import learntatoeba.account.Account;
import learntatoeba.account.AccountManager;
import learntatoeba.Menu;
import learntatoeba.Terminal;

public class ChangeAccountRecurrenceProbabilityMenu extends Menu {
	private final Account account;
	private final Menu previousMenu;
	private final Menu nextMenu;
	
	public ChangeAccountRecurrenceProbabilityMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		Terminal.println("\nThe recurrence probability for your account specifies how often you'll see the same sentences across practice sessions. For example, when a sentence is recommended for you and your recurrence probability is set to 0.3, then it will only appear in 30% of your practice sessions. It's chance of recurring is 0.3.");
		askUserAYesNoQuestion("The current recurrence probability for this account is " + account.getRecurrenceProbability() + ". Would you like to change it?",
				nextMenu::run,
				this::askUserToChangeRecurrenceProbability
		);
	}
	
	private void askUserToChangeRecurrenceProbability() {
		askUserAQuestion("Specify a new recurrence probability. This must be a number greater than 0 and less than or equal to 1.",
				previousMenu::run,
				newProbability -> {
					boolean setProbabilitySuccessful = AccountManager.setRecurrenceProbability(account, newProbability);
					if (setProbabilitySuccessful) {
						Terminal.println("Your account's recurrence probability has been changed to " + account.getRecurrenceProbability() + ".");
						nextMenu.run();
					} else {
						Terminal.println("You did not enter a valid probability. Please try again.");
						run();
					}
				}
		);
	}
}
