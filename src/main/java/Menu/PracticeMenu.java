package Menu;

import Account.Account;
import Language.Language;
import Language.SentenceChooser;
import Language.Sentence;
import Terminal.Terminal;

import java.io.IOException;

public class PracticeMenu extends Menu {
	private final Account account;
	private final Language language;
	private final Menu previousMenu;
	private SentenceChooser sentenceChooser;
	
	public PracticeMenu(Account account, Language language, Menu previousMenu) {
		this.account = account;
		this.language = language;
		this.previousMenu = previousMenu;
	}
	
	@Override
	public void run() {
		Terminal.println(
				"\nYou will now be presented with up to " + getSessionLengthPrintString() + " in " + language.getName() + " until you type 'back' or 'exit'. Vocab updates will be lost if you end the session in any other way.\n" +
				"After reading a sentence, you can do several things:\n" +
				"1) Enter anything beginning with the '#' symbol. This will give you the translation(s) of the sentence into your native language.\n" +
				"2) Enter 'a' to see the authors of the sentence and its translations, as well as Tatoeba URLs for more information about the sentences.\n" +
				"3) Enter a command and move on to the next sentence.\n" +
				"\n" +
				"If your command begins with \"!bX\" for some non-negative whole number X, then the sentence will be blacklisted for X days, meaning you won't see it when practicing on this account until the blacklist expires. To specify a duration in hours, use \"!bXh\". If you want to blacklist the sentence forever, use \"!bInfinite\", or \"!bi\" for short.\n" +
				"If your command doesn't begin with \"!b\", then your account's autoblacklist setting applies. Currently your account's autoblacklist duration is set to " + account.getAutoblacklistDuration().toPrintString() + ".\n" +
				"The rest of the command is of the form \"phrase1: status1, phrase2: status2, ... phraseN: statusN\", which updates the statuses of phrases in your vocabulary. This should be blank if you do not wish to update any statuses.\n" +
				"Note that if you want to apply a manual blacklist while updating statuses, you must separate the two parts of the command with a space like so: \"!b2 wood: 3\".\n" +
				"\n" +
				"A status must be a whole number from 1 to 5, or 98 or 99. These have the following meanings:\n" +
				"\t1: Unknown\n" +
				"\t2: A bit better than unknown\n" +
				"\t3: Still learning\n" +
				"\t4: I nearly know it\n" +
				"\t5: Known\n" +
				"\t98: Ignored (this can be used for names)\n" +
				"\t99: Well-known (this can be used for words that are the same in your native language)\n" +
				"So for example, \"chair: 2, coffee table: 3, stool: 1\" will record that you recognize the word chair, you are on your way to learning the phrase coffee table, and the word stool is unknown to you.\n"
		);
		try {
			sentenceChooser = new SentenceChooser(account, language);
			if (sentenceChooser.vocabIsEmpty()) {
				Terminal.println("\nIt looks like you don't have any " + language.getName() + " vocab yet, so you might want to enter some commands now to add words you know (or partly know). When you're done and want to start practicing, just press enter a blank line.");
				processVocabUpdatesAndGoToNextSentenceWhenUserInputIsBlank();
			} else {
				doAnotherSentence();
			}
		} catch (IOException e) {
			if (sentenceChooser != null) {
				sentenceChooser.close();
			}
			e.printStackTrace();
			previousMenu.run();
		}
	}
	
	private String getSessionLengthPrintString() {
		int sessionLength = account.getSessionLength();
		if (sessionLength == 1) {
			return sessionLength + " sentence";
		} else {
			return sessionLength + " sentences";
		}
	}
	
	private void processVocabUpdatesAndGoToNextSentenceWhenUserInputIsBlank() throws IOException {
		String userInput = getNextLine();
		exitMenuOrDoSomethingElse(userInput,
				() -> {
					if (userInput.equals("")) {
						doAnotherSentence();
					} else {
						boolean updateVocabSuccessful = sentenceChooser.updateVocab(userInput);
						if (!updateVocabSuccessful) {
							Terminal.println("Your request was improperly formatted. Please make sure you typed correctly and try again:");
						}
						processVocabUpdatesAndGoToNextSentenceWhenUserInputIsBlank();
					}
				}
		);
	}
	
	private void doAnotherSentence() throws IOException {
		Sentence sentence = sentenceChooser.getNextSentence();
		if (sentence == null) {
			Terminal.println("Wow! You've been through a lot of sentences! Take a well-deserved break.");
			sentenceChooser.close();
			previousMenu.run();
		} else {
			String sentenceText = sentence.getText();
			printSentence(sentenceText);
			Terminal.println("\t" + sentenceChooser.getSentenceAnnotation(sentenceText));
			askUserHowToProceed(sentence);
		}
	}
	
	private void askUserHowToProceed(Sentence sentence) throws IOException {
		String userInput = getNextLine();
		if (userInput.startsWith("#")) {
			for (Sentence translation : sentence.getTranslations()) {
				printTranslation(translation.getText());
			}
			askUserHowToProceed(sentence);
		} else if (userInput.toLowerCase().equals("a")) {
			printAttributions(sentence);
			askUserHowToProceed(sentence);
		} else {
			processUpdateCommandAndGoToNextSentence(userInput, sentence);
		}
	}
	
	private void printAttributions(Sentence sentence) {
		Terminal.print(getAttributionLine(sentence));
		printSentence(sentence.getText());
		for (Sentence translation : sentence.getTranslations()) {
			Terminal.println(getAttributionLine(translation));
			printTranslation(translation.getText());
		}
	}
	
	private String getAttributionLine(Sentence sentence) {
		String author = sentence.getAuthor();
		String attributionLine = "https://tatoeba.org/eng/sentences/show/" + sentence.getId();
		if (author.equals("\\N")) {
			attributionLine += " with no author:";
		} else {
			attributionLine += " by " + author + ":";
		}
		return attributionLine;
	}
	
	private void printSentence(String sentence) {
		Terminal.print("\n\t");
		if (language.isRightToLeft()) {
			Terminal.printlnRtl(sentence);
		} else {
			Terminal.println(sentence);
		}
	}
	
	private void printTranslation(String translation) {
		Terminal.print("\t");
		if (account.getNativeLanguage().isRightToLeft()) {
			Terminal.printlnRtl(translation);
		} else {
			Terminal.println(translation);
		}
	}
	
	private void processUpdateCommandAndGoToNextSentence(String updateCommand, Sentence sentence) throws IOException {
		exitMenuOrDoSomethingElse(updateCommand,
				() -> {
					boolean updateCommandIsValid = sentenceChooser.updateBlacklistAndVocab(sentence, updateCommand);
					if (updateCommandIsValid) {
						doAnotherSentence();
					} else {
						Terminal.println("Your request was improperly formatted. Please make sure you typed correctly and try again:");
						askUserHowToProceed(sentence);
					}
				}
		);
	}
	
	private void exitMenuOrDoSomethingElse(String userInput, RunnableWithIOException somethingElseProcedure) throws IOException {
		if (userInput.toLowerCase().equals("exit")) {
			sentenceChooser.close();
			System.exit(0);
		} else if (userInput.toLowerCase().equals("back")) {
			sentenceChooser.close();
			previousMenu.run();
		} else {
			somethingElseProcedure.run();
		}
	}
	
	private interface RunnableWithIOException {
		void run() throws IOException;
	}
}
