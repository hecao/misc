package net.dongliu.push.utils;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * 加密/解密工具类
 * 
 * @author dongliu
 * 
 */
public class EncryptUtils {
	private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";
	private static final String ALGORITHM_RSA = "RSA/ECB/PKCS1Padding";
	private static byte[] iv = { 1, 2, 3, 4, 5, 6, 7, 8 };
	private static IvParameterSpec zeroIv = new IvParameterSpec(iv);

	/**
	 * 生成DES密钥.
	 * 
	 * @param seed
	 * @return
	 */
	public static SecretKey generatorDeskey(String seed) {
		KeyGenerator generator;
		try {
			generator = KeyGenerator.getInstance("DES");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		generator.init(new SecureRandom(seed.getBytes()));
		return generator.generateKey();
	}

	/**
	 * des加密.
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static byte[] encryptDes(byte[] data, SecretKey key) {
		if (data == null || data.length == 0) {
			return data;
		}
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
			cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
			return cipher.doFinal(data);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * des加密.
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static byte[] decryptDes(byte[] data, SecretKey key) {
		if (data == null || data.length == 0) {
			return data;
		}
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
			cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
			return cipher.doFinal(data);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 生成RSA公钥/私钥对. 以base64方式编码成字符串.
	 */
	public static String[] getRSAKeys() {
		KeyPairGenerator keyPairGen;
		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		keyPairGen.initialize(1024);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		PublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		PrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		return new String[] { Base64Utils.encode(publicKey.getEncoded()), Base64Utils.encode(privateKey.getEncoded()) };
	}

	/**
	 * 从base64编码的字符串中，恢复RSA pub key.
	 * 
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws Exception
	 */
	public static PublicKey getRSAPublicKey(String keyStr) {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64Utils.decode(keyStr));
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			PublicKey publicKey = keyFactory.generatePublic(keySpec);
			return publicKey;
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 从base64编码的字符串中，恢复RSA priv key.
	 * 
	 * @param seed
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws Exception
	 */
	public static PrivateKey getRSAPrivateKey(String keyStr) {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64Utils.decode(keyStr));
		PrivateKey privateKey;
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			privateKey = keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
		return privateKey;
	}

	/**
	 * RSA加密.
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static byte[] encryptRSA(byte[] data, PublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(data);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * RSA解密.
	 * 
	 * @param data
	 * @param privateKey
	 * @return
	 */
	public static byte[] decryptRSA(byte[] data, PrivateKey privateKey) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(data);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @param args
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (args.length > 0 && args[0].equals("-rsa_key")) {
			String[] keys = getRSAKeys();
			System.out.println("Pub key:" + keys[0]);
			System.out.println("Priv key:" + keys[1]);
		}
	}

}
