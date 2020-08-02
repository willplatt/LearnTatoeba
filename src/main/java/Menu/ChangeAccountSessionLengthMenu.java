package Menu;

import Account.Account;
import Account.AccountManager;
import Terminal.Terminal;

public class ChangeAccountSessionLengthMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public ChangeAccountSessionLengthMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		Terminal.println("\nThe session length for your account specifies the maximum number of sentences you will see in each practice session.");
		askUserAYesNoQuestion("The current session length for this account is " + account.getSessionLength() + ". Would you like to change it?",
				nextMenu::run,
				this::askUserToChangeSessionLength
		);
	}
	
	private void askUserToChangeSessionLength() {
		askUserAQuestion("Specify a new session length. This must be a whole number between 1 and 500. If your sessions are several hundred sentences long, then it's recommended you restart LearnTatoeba between practice sessions to prevent stack overflow.",
				previousMenu::run,
				newSessionLength -> {
					boolean setSessionLengthSuccessful = AccountManager.setSessionLength(account, newSessionLength);
					if (setSessionLengthSuccessful) {
						Terminal.println("Your account's session length has been changed!");
						nextMenu.run();
					} else {
						Terminal.println("You did not enter a valid session length. Please try again.");
						run();
					}
				}
		);
	}
}
