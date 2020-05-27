import java.io.IOException;
import java.io.RandomAccessFile;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RandomAccessReaderHelper {
	public static String readUtf8Line(RandomAccessFile reader) throws IOException {
		String rawEncodedString = reader.readLine();
		if (rawEncodedString == null) {
			return null;
		}
		return new String(rawEncodedString.getBytes(ISO_8859_1), UTF_8);
	}
}
