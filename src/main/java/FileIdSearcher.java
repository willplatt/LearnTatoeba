import java.io.IOException;
import java.io.RandomAccessFile;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class FileIdSearcher {
	public static long getByteIndexOnLineWithId(int desiredId, long startIndex, long endIndex, RandomAccessFile reader) throws IOException {
		if (startIndex >= endIndex) {
			return endIndex;
		}
		long middleIndex = (startIndex + endIndex) / 2;
		int id = getFirstIdOfLineAt(middleIndex, reader);
		if (id == desiredId) {
			return middleIndex;
		} else if (id > desiredId) {
			return getByteIndexOnLineWithId(desiredId, startIndex, middleIndex - 1, reader);
		} else {
			return getByteIndexOnLineWithId(desiredId, middleIndex + 1, endIndex, reader);
		}
	}
	
	public static int getFirstIdOfLineAt(long index, RandomAccessFile reader) throws IOException {
		String line = readLineAt(index, reader);
		int indexOfTab = line.indexOf('\t');
		return parseInt(line.substring(0, indexOfTab));
	}
	
	public static int getSecondIdOfLineAt(long index, RandomAccessFile reader) throws IOException {
		String line = readLineAt(index, reader);
		int indexOfTab = line.indexOf('\t');
		return parseInt(line.substring(indexOfTab + 1));
	}
	
	public static String getTextAfterSecondTabOfLineAt(long index, RandomAccessFile reader) throws IOException {
		String line = readLineAt(index, reader);
		int indexOfFirstTab = line.indexOf('\t');
		int indexOfSecondTab = line.indexOf('\t', indexOfFirstTab + 1);
		return line.substring(indexOfSecondTab + 1);
	}
	
	public static String readLineAt(long index, RandomAccessFile reader) throws IOException {
		long recedingIndex = index;
		int lineLength = 1;
		while (recedingIndex + lineLength > index && recedingIndex >= 3) {
			recedingIndex -= 3;
			reader.seek(recedingIndex);
			String line = reader.readLine();
			lineLength = line.getBytes(ISO_8859_1).length + 1;
		}
		reader.seek(recedingIndex + lineLength);
		return RandomAccessReaderHelper.readUtf8Line(reader);
	}
}
