package net.dongliu.push.client.utils;

/**
 * 鉴于android 和 sun jdk的base64处理不一致...
 * 
 * @author dongliu
 * 
 */
public class Base64Utils {
	private static final int BASELENGTH = 255;
	private static final int LOOKUPLENGTH = 64;
	private static final int TWENTYFOURBITGROUP = 24;
	private static final int EIGHTBIT = 8;
	private static final int SIXTEENBIT = 16;
	private static final int FOURBYTE = 4;
	private static final int SIGN = -128;
	private static final byte PAD = (byte) '=';
	private static byte[] base64Alphabet = new byte[BASELENGTH];
	private static byte[] lookUpBase64Alphabet = new byte[LOOKUPLENGTH];

	static {
		for (int i = 0; i < BASELENGTH; i++) {
			base64Alphabet[i] = -1;
		}
		for (int i = 'Z'; i >= 'A'; i--) {
			base64Alphabet[i] = (byte) (i - 'A');
		}
		for (int i = 'z'; i >= 'a'; i--) {
			base64Alphabet[i] = (byte) (i - 'a' + 26);
		}
		for (int i = '9'; i >= '0'; i--) {
			base64Alphabet[i] = (byte) (i - '0' + 52);
		}
		base64Alphabet['+'] = 62;
		base64Alphabet['/'] = 63;
		for (int i = 0; i <= 25; i++) {
			lookUpBase64Alphabet[i] = (byte) ('A' + i);
		}
		for (int i = 26, j = 0; i <= 51; i++, j++) {
			lookUpBase64Alphabet[i] = (byte) ('a' + j);
		}
		for (int i = 52, j = 0; i <= 61; i++, j++) {
			lookUpBase64Alphabet[i] = (byte) ('0' + j);
		}
		lookUpBase64Alphabet[62] = (byte) '+';
		lookUpBase64Alphabet[63] = (byte) '/';
	}

	/**
	 * base64编码.
	 */
	public static String encode(byte[] data) {
		int lengthDataBits = data.length * EIGHTBIT;
		int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
		int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
		StringBuilder sb;
		if (fewerThan24bits != 0) {
			sb = new StringBuilder((numberTriplets + 1) * 4);
		} else {
			sb = new StringBuilder(numberTriplets * 4);
		}
		byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;
		int dataIndex = 0;
		int i = 0;
		for (i = 0; i < numberTriplets; i++) {
			dataIndex = i * 3;
			b1 = data[dataIndex];
			b2 = data[dataIndex + 1];
			b3 = data[dataIndex + 2];
			l = (byte) (b2 & 0x0f);
			k = (byte) (b1 & 0x03);
			byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
			byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
			byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6) : (byte) ((b3) >> 6 ^ 0xfc);
			sb.append((char) lookUpBase64Alphabet[val1]);
			sb.append((char) lookUpBase64Alphabet[val2 | (k << 4)]);
			sb.append((char) lookUpBase64Alphabet[(l << 2) | val3]);
			sb.append((char) lookUpBase64Alphabet[b3 & 0x3f]);
		}
		// form integral number of 6-bit groups
		dataIndex = i * 3;
		if (fewerThan24bits == EIGHTBIT) {
			b1 = data[dataIndex];
			k = (byte) (b1 & 0x03);
			byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
			sb.append((char) lookUpBase64Alphabet[val1]);
			sb.append((char) lookUpBase64Alphabet[k << 4]);
			sb.append((char) PAD);
			sb.append((char) PAD);
		} else if (fewerThan24bits == SIXTEENBIT) {
			b1 = data[dataIndex];
			b2 = data[dataIndex + 1];
			l = (byte) (b2 & 0x0f);
			k = (byte) (b1 & 0x03);
			byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
			byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
			sb.append((char) lookUpBase64Alphabet[val1]);
			sb.append((char) lookUpBase64Alphabet[val2 | (k << 4)]);
			sb.append((char) lookUpBase64Alphabet[l << 2]);
			sb.append((char) PAD);
		}
		return sb.toString();
	}

	/**
	 * base64解码.
	 */
	public static byte[] decode(String base64Data) {
		if (base64Data.length() == 0) {
			return new byte[0];
		}
		int numberQuadruple = base64Data.length() / FOURBYTE;
		byte decodedData[] = null;
		byte b1 = 0, b2 = 0, b3 = 0, b4 = 0, marker0 = 0, marker1 = 0;
		// Throw away anything not in base64Data
		int encodedIndex = 0;
		int dataIndex = 0;
		int lastData = base64Data.length();
		while (base64Data.charAt(lastData - 1) == PAD) {
			if (--lastData == 0) {
				return new byte[0];
			}
		}
		decodedData = new byte[lastData - numberQuadruple];
		for (int i = 0; i < numberQuadruple; i++) {
			dataIndex = i * 4;
			marker0 = (byte) base64Data.charAt((dataIndex + 2));
			marker1 = (byte) base64Data.charAt((dataIndex + 3));
			b1 = base64Alphabet[base64Data.charAt(dataIndex)];
			b2 = base64Alphabet[base64Data.charAt(dataIndex + 1)];
			if (marker0 != PAD && marker1 != PAD) {
				// No PAD e.g 3cQl
				b3 = base64Alphabet[marker0];
				b4 = base64Alphabet[marker1];
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex + 1] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
				decodedData[encodedIndex + 2] = (byte) (b3 << 6 | b4);
			} else if (marker0 == PAD) {
				// Two PAD e.g. 3c[Pad][Pad]
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
			} else if (marker1 == PAD) {
				// One PAD e.g. 3cQ[Pad]
				b3 = base64Alphabet[marker0];
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex + 1] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
			}
			encodedIndex += 3;
		}
		return decodedData;
	}

}