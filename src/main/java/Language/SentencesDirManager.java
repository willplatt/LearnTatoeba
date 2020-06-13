package Language;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import static Constants.Paths.INSTALL_DIR;

public class SentencesDirManager {
	public static final File SENTENCES_DIR = new File(INSTALL_DIR, "sentences");
	public static final File LINKS_FILE = new File(SENTENCES_DIR, "links.csv");
	
	public static boolean hasFileForLanguage(Language language) {
		File sentencesFile = new File(SENTENCES_DIR, language.getTatoebaCode() + "_sentences.tsv");
		return sentencesFile.exists();
	}
	
	public static void downloadSentenceLinks() throws IOException {
		if (!SENTENCES_DIR.exists() || !SENTENCES_DIR.isDirectory()) {
			boolean dirCreationSuccessful = SENTENCES_DIR.mkdir();
			if (!dirCreationSuccessful) {
				throw new IOException("Directory \"" + SENTENCES_DIR + "\" could not be created.");
			}
		}
		File bZipFile = new File(SENTENCES_DIR, "links.tar.bz2");
		downloadFile("https://downloads.tatoeba.org/exports/links.tar.bz2", bZipFile);
		File targetTarFile = new File(SENTENCES_DIR, "links.tar");
		extractBZip2(bZipFile, targetTarFile);
		extractTarToLinksFile(targetTarFile);
	}
	
	public static void downloadFileForLanguage(Language language) throws IOException {
		if (!LINKS_FILE.exists()) {
			downloadSentenceLinks();
		}
		String languageCode = language.getTatoebaCode();
		File bZipFile = new File(SENTENCES_DIR, languageCode + "_sentences.tsv.bz2");
		downloadFile("https://downloads.tatoeba.org/exports/per_language/" + languageCode + "/" + languageCode + "_sentences.tsv.bz2", bZipFile);
		File targetExtractionFile = new File(SENTENCES_DIR, languageCode + "_sentences.tsv");
		extractBZip2(bZipFile, targetExtractionFile);
		addLinkedIdsToSentences(targetExtractionFile);
	}
	
	private static void downloadFile(String urlStr, File file) throws IOException {
		URL url = new URL(urlStr);
		try (ReadableByteChannel urlChannel = Channels.newChannel(url.openStream());
		     FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			fileOutputStream.getChannel().transferFrom(urlChannel, 0, Long.MAX_VALUE);
		}
	}
	
	private static void extractBZip2(File bZipFile, File targetExtractionFile) throws IOException {
		try (BZip2CompressorInputStream compressedInputStream = new BZip2CompressorInputStream(new BufferedInputStream(Files.newInputStream(bZipFile.toPath())));
		     OutputStream targetOutputStream = Files.newOutputStream(targetExtractionFile.toPath())) {
			final byte[] buffer = new byte[1024];
			int n;
			while (-1 != (n = compressedInputStream.read(buffer))) {
				targetOutputStream.write(buffer, 0, n);
			}
		}
	}
	
	private static void extractTarToLinksFile(File tarFile) throws IOException {
		try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new BufferedInputStream(Files.newInputStream(tarFile.toPath())));
		     OutputStream targetOutputStream = Files.newOutputStream(LINKS_FILE.toPath())) {
			tarInputStream.getNextTarEntry();
			byte[] buffer = new byte[1024];
			int n;
			while (-1 != (n = tarInputStream.read(buffer))) {
				targetOutputStream.write(buffer, 0, n);
			}
		}
	}
	
	private static void addLinkedIdsToSentences(File sentencesFile) throws IOException {
		File tempSentencesFile = new File(SENTENCES_DIR, "temp.tsv");
		try (BufferedReader sentencesReader = Files.newBufferedReader(sentencesFile.toPath());
		     BufferedReader linksReader = Files.newBufferedReader(SentencesDirManager.LINKS_FILE.toPath());
		     BufferedWriter bufferedTempWriter = Files.newBufferedWriter(tempSentencesFile.toPath())) {
			String sentenceLine;
			int linkId = 0;
			while ((sentenceLine = sentencesReader.readLine()) != null) {
				int sentenceId = getIdAtStartOfLine(sentenceLine);
				String linkLine = null;
				while (linkId < sentenceId && (linkLine = linksReader.readLine()) != null) {
					linkId = getIdAtStartOfLine(linkLine);
				}
				String lineAppendage = getTabSeparatedLinkedIds(sentenceId, linkId, linkLine, linksReader);
				bufferedTempWriter.write(sentenceLine + lineAppendage + "\n");
			}
		}
		Files.delete(sentencesFile.toPath());
		Files.move(tempSentencesFile.toPath(), sentencesFile.toPath());
	}
	
	private static int getIdAtStartOfLine(String line) {
		int indexOfFirstTab = line.indexOf('\t');
		return Integer.parseInt(line.substring(0, indexOfFirstTab));
	}
	
	private static String getTabSeparatedLinkedIds(int sentenceId, int linkId, String linkLine, BufferedReader linksReader) throws IOException {
		String linkedIds = "";
		while (linkId == sentenceId && linkLine != null) {
			int linkedId = getIdAtEndOfLine(linkLine);
			linkedIds += "\t" + linkedId;
			linkLine = linksReader.readLine();
			if (linkLine != null) {
				linkId = getIdAtStartOfLine(linkLine);
			}
		}
		return linkedIds;
	}
	
	private static int getIdAtEndOfLine(String line) {
		int indexOfLastTab = line.lastIndexOf('\t');
		return Integer.parseInt(line.substring(indexOfLastTab + 1));
	}
}
