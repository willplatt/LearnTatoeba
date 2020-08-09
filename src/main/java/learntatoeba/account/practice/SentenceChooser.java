package learntatoeba.account.practice;

import learntatoeba.SentencesDirManager;
import learntatoeba.Terminal;
import learntatoeba.UnicodeString;
import learntatoeba.account.Account;
import learntatoeba.account.BlacklistDuration;
import learntatoeba.language.Language;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static learntatoeba.SentencesDirManager.SUFFIX_OF_SENTENCE_FILES;
import static learntatoeba.account.practice.SentenceFileSearcher.getByteIndexOnLineWithId;
import static learntatoeba.account.practice.SentenceFileSearcher.readLineAt;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

public class SentenceChooser {
	private static final int MAX_SCORE_UPPER_LIMIT = 300;
	private static final String FULLWIDTH_CHAR_REGEX = "[\u1100-\u11FF\u2E80-\u302D\u3030-\u303E\u3040-\u31FF\u3400-\uA4CF\uA500-\uA61F\uA960-\uA97F\uAC00-\uD7FF\uF900-\uFAFF\uFE30-\uFE6F\uFF00-\uFF60\uFFE0-\uFFE6" + Character.toString(0x20000) + "-" + Character.toString(0x3134F) + "]";
	
	private final VocabManager vocabManager;
	private final BlacklistManager blacklistManager;
	private final File sentencesFile;
	private final List<Sentence> nextSentences = new ArrayList<>();
	private final RandomAccessFile nativeTranslationReader;
	private final String wordCharRegex;
	private final boolean isRightToLeft;
	private final boolean treatFullwidthCharsAsWords;
	private final double recurrenceProbability;
	private final int sessionLength;
	
	private BufferedReader sentencesReader;
	private int sentenceScoreUpperLimit = 20;
	private int sentencesChosen = 0;
	
	public SentenceChooser(Account account, Language practiceLanguage) throws IOException {
		this.vocabManager =  new VocabManager(account, practiceLanguage);
		this.blacklistManager =  new BlacklistManager(account, practiceLanguage);
		File translationsFile = new File(SentencesDirManager.SENTENCES_DIR, account.getNativeLanguage().getTatoebaCode() + SUFFIX_OF_SENTENCE_FILES);
		this.nativeTranslationReader = new RandomAccessFile(translationsFile, "r");
		this.sentencesFile =  new File(SentencesDirManager.SENTENCES_DIR, practiceLanguage.getTatoebaCode() + SUFFIX_OF_SENTENCE_FILES);
		this.sentencesReader = Files.newBufferedReader(sentencesFile.toPath(), UTF_8);
		this.wordCharRegex = "[" + unescapeJava(practiceLanguage.getWordCharRegExp()) + "]";
		this.isRightToLeft = practiceLanguage.isRightToLeft();
		this.treatFullwidthCharsAsWords = !practiceLanguage.getTatoebaCode().equals("kor");
		this.recurrenceProbability = account.getRecurrenceProbability();
		this.sessionLength = account.getSessionLength();
	}
	
	public boolean vocabIsEmpty() {
		return vocabManager.isEmpty();
	}
	
	public Sentence getNextSentence() throws IOException {
		while (nextSentences.isEmpty() && sentencesChosen < sessionLength && sentenceScoreUpperLimit < MAX_SCORE_UPPER_LIMIT) {
			Terminal.println("Computing more sentences...");
			computeNextSentencesWithScoresBetween(Math.max(1, sentenceScoreUpperLimit - 20), sentenceScoreUpperLimit);
		}
		if (nextSentences.isEmpty()) {
			return null;
		} else {
			int indexOfFinalSentence = nextSentences.size() - 1;
			Sentence sentence = nextSentences.get(indexOfFinalSentence);
			nextSentences.remove(indexOfFinalSentence);
			return sentence;
		}
	}
	
	public String getSentenceAnnotation(String sentence) {
		if (isRightToLeft) {
			return getRightToLeftSentenceAnnotation(sentence);
		} else {
			return getLeftToRightSentenceAnnotation(sentence);
		}
	}
	
	public boolean updateBlacklistAndVocab(Sentence sentence, String updateCommand) {
		String vocabCommand = updateCommand;
		try {
			if (updateCommand.toLowerCase().startsWith("!b")) {
				int indexOfFirstSpace = updateCommand.indexOf(' ');
				int endOfBlacklistCommand = indexOfFirstSpace == -1 ? updateCommand.length() : indexOfFirstSpace;
				vocabCommand = updateCommand.substring(Math.min(updateCommand.length(), endOfBlacklistCommand + 1));
				String blacklistDurationString = updateCommand.substring(2, endOfBlacklistCommand);
				blacklistManager.blacklist(sentence, new BlacklistDuration(blacklistDurationString));
			} else {
				blacklistManager.autoblacklist(sentence);
			}
		} catch (IllegalArgumentException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("There was a problem modifying the blacklist file.");
		}
		return updateVocab(vocabCommand);
	}
	
	public boolean updateVocab(String updateCommand) {
		return vocabManager.updateVocab(updateCommand);
	}
	
	public void close() {
		try {
			vocabManager.pushUpdatesToFile();
			Terminal.println("Updates saved to file.");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("There was a problem writing your vocab updates to the file. Your vocab changes for this session may not have been saved.");
		}
		try {
			nativeTranslationReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			sentencesReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void computeNextSentencesWithScoresBetween(int minScore, int maxScore) throws IOException {
		String line = null;
		while (nextSentences.size() < 5 && sentencesChosen < sessionLength && (line = sentencesReader.readLine()) != null) {
			if (Math.random() < recurrenceProbability) {
				Sentence sentence = new Sentence(line);
				if (!blacklistManager.isBlacklisted(sentence)) {
					int score = getScoreForSentence(sentence.getText());
					if (score >= minScore && score < maxScore) {
						List<Sentence> translations = getNativeTranslations(line);
						if (!translations.isEmpty()) {
							sentence.addTranslations(translations);
							nextSentences.add(sentence);
							sentencesChosen++;
						}
					}
				}
			}
		}
		if (line == null) {
			sentencesReader.close();
			sentencesReader = Files.newBufferedReader(sentencesFile.toPath(), UTF_8);
			sentenceScoreUpperLimit += 20;
		}
	}
	
	private int getScoreForSentence(String sentence) {
		UnicodeString remainingToScore = trimOuterPunctuation(new UnicodeString(sentence));
		int score = 0;
		while (remainingToScore.length() > 0) {
			UnicodeString phrase = getLongestPhrasePrefix(remainingToScore);
			score += getScoreForPhrase(phrase.toString());
			remainingToScore = remainingToScore.getUnicodeSubstring(phrase.length());
			int wordSeparatorLength = indexOfFirstWordCharOrZero(remainingToScore);
			remainingToScore = remainingToScore.getUnicodeSubstring(wordSeparatorLength);
		}
		return score;
	}
	
	private String getRightToLeftSentenceAnnotation(String sentence) {
		if (Terminal.isEmulatingBidi()) {
			String reversedLeftToRightAnnotation = new StringBuilder(getLeftToRightSentenceAnnotation(sentence)).reverse().toString();
			return reversedLeftToRightAnnotation
					.replace("89", "98")
					.replace("\uFF18\uFF19", "\uFF19\uFF18")
					.replace("\uFF189", "9\uFF18")
					.replace("8\uFF19", "\uFF198");
		} else {
			return "\u200F" + getLeftToRightSentenceAnnotation(sentence) + "\u200F";
		}
	}
	
	private String getLeftToRightSentenceAnnotation(String sentence) {
		UnicodeString unicodeSentence = new UnicodeString(sentence);
		UnicodeString trimmedSentence = trimOuterPunctuation(unicodeSentence);
		int charactersTrimmedFromStart = unicodeSentence.indexOf(trimmedSentence);
		String annotation = " ".repeat(charactersTrimmedFromStart);
		UnicodeString remainingToAnnotate = trimmedSentence;
		while (remainingToAnnotate.length() > 0) {
			UnicodeString phrase = getLongestPhrasePrefix(remainingToAnnotate);
			String phraseAnnotation = getPhraseAnnotation(phrase);
			annotation += phraseAnnotation;
			remainingToAnnotate = remainingToAnnotate.getUnicodeSubstring(phrase.length());
			int wordSeparatorLength = indexOfFirstWordCharOrZero(remainingToAnnotate);
			int annotationOverrun = phraseAnnotation.length() - phrase.length();
			int numberOfSpaces = Math.max(0, wordSeparatorLength - annotationOverrun);
			annotation += " ".repeat(numberOfSpaces);
			remainingToAnnotate = remainingToAnnotate.getUnicodeSubstring(wordSeparatorLength);
		}
		annotation += " ".repeat(Math.max(0, unicodeSentence.length() - annotation.length()));
		return convertAnnotationToFullwidthWhereNecessary(annotation, unicodeSentence);
	}
	
	private UnicodeString trimOuterPunctuation(UnicodeString sentence) {
		int start = 0;
		int end = sentence.length();
		while (start < end && !sentence.getCharacter(start).matches(wordCharRegex)) {
			start++;
		}
		while (start < end && !sentence.getCharacter(end - 1).matches(wordCharRegex)) {
			end--;
		}
		return sentence.getUnicodeSubstring(start, end);
	}
	
	private UnicodeString getLongestPhrasePrefix(UnicodeString sentenceFragment) {
		int wordLength = lengthOfFirstWordIn(sentenceFragment);
		UnicodeString phrase = sentenceFragment;
		while (phrase.length() != wordLength && vocabManager.getStatusOfPhrase(phrase.toString()) == 0) {
			phrase = removeLastWordFrom(phrase);
		}
		return phrase;
	}
	
	private int getScoreForPhrase(String phrase) {
		int status = vocabManager.getStatusOfPhrase(phrase);
		if (status == 98 || status == 99) {
			status = 5;
		}
		int difference = Math.max(0, 4 - status);
		int differenceSquared = difference * difference;
		return 5 * differenceSquared;
	}
	
	private String getPhraseAnnotation(UnicodeString phrase) {
		String status = String.valueOf(vocabManager.getStatusOfPhrase(phrase.toString()));
		if (status.equals("0")) {
			status = "-";
		}
		return status + "-".repeat(Math.max(0, phrase.length() - status.length()));
	}
	
	private int indexOfFirstWordCharOrZero(UnicodeString str) {
		for (int i = 0; i < str.length(); i++) {
			if (str.getCharacter(i).matches(wordCharRegex)) {
				return i;
			}
		}
		return 0;
	}
	
	private int lengthOfFirstWordIn(UnicodeString phrase) {
		int index = 0;
		while (index < phrase.length() && phrase.getCharacter(index).matches(wordCharRegex)) {
			if (treatFullwidthCharsAsWords && phrase.getCharacter(index).matches(FULLWIDTH_CHAR_REGEX)) {
				index++;
				break;
			} else {
				index++;
			}
		}
		return index;
	}
	
	private UnicodeString removeLastWordFrom(UnicodeString phrase) {
		int index = phrase.length();
		while (index > 0 && phrase.getCharacter(index - 1).matches(wordCharRegex)) {
			if (treatFullwidthCharsAsWords && phrase.getCharacter(index - 1).matches(FULLWIDTH_CHAR_REGEX)) {
				index--;
				break;
			} else {
				index--;
			}
		}
		while (index > 0 && !phrase.getCharacter(index - 1).matches(wordCharRegex)) {
			index--;
		}
		return phrase.getUnicodeSubstring(0, index);
	}
	
	private String convertAnnotationToFullwidthWhereNecessary(String annotation, UnicodeString sentence) {
		String newAnnotation = "";
		for (int i = 0; i < sentence.length(); i++) {
			if (sentence.getCharacter(i).matches(FULLWIDTH_CHAR_REGEX)) {
				newAnnotation += convertCharToFullwidth(annotation.charAt(i));
			} else {
				newAnnotation += annotation.charAt(i);
			}
		}
		newAnnotation += annotation.substring(sentence.length());
		return newAnnotation;
	}
	
	private char convertCharToFullwidth(char ch) {
		switch (ch) {
			case '-':
				return '\uFF0D';
			case ' ':
				return '\u3000';
			case '1':
				return '\uFF11';
			case '2':
				return '\uFF12';
			case '3':
				return '\uFF13';
			case '4':
				return '\uFF14';
			case '5':
				return '\uFF15';
			case '8':
				return '\uFF18';
			case '9':
				return '\uFF19';
			default:
				throw new IllegalArgumentException("Unexpected character '" + ch + "'. Cannot convert to fullwidth.");
		}
	}
	
	private List<Sentence> getNativeTranslations(String sentenceLine) throws IOException {
		return getNativeTranslationsForIds(getTranslationIds(sentenceLine));
	}
	
	private List<Integer> getTranslationIds(String sentenceLine) {
		String[] ids = sentenceLine.split("\t");
		List<Integer> translationIds = new ArrayList<>();
		for (int i = 4; i < ids.length; i++) {
			translationIds.add(parseInt(ids[i]));
		}
		return translationIds;
	}
	
	private List<Sentence> getNativeTranslationsForIds(List<Integer> ids) throws IOException {
		Collections.sort(ids);
		List<Sentence> translations = new ArrayList<>();
		long startIndex = 0;
		long fileLength = nativeTranslationReader.length();
		int idsIndex = 0;
		while (idsIndex < ids.size() && startIndex < fileLength - 1) {
			int desiredId = ids.get(idsIndex);
			startIndex = getByteIndexOnLineWithId(desiredId, startIndex, fileLength, nativeTranslationReader);
			Sentence sentence = new Sentence(readLineAt(startIndex, nativeTranslationReader));
			if (desiredId == sentence.getId()) {
				translations.add(sentence);
			}
			idsIndex++;
		}
		return translations;
	}
}
