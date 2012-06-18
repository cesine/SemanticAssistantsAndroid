/*
Semantic Assistants -- http://www.semanticsoftware.info/semantic-assistants

This file is part of the Semantic Assistants architecture.

Copyright (C) 2012, 2013 Semantic Software Lab, http://www.semanticsoftware.info
Rene Witte
Bahar Sateli

The Semantic Assistants architecture is free software: you can
redistribute and/or modify it under the terms of the GNU Affero General
Public License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package info.semanticsoftware.semassist.server.core.security.encryption;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Provides utility methods for encryption.
 * @author Bahar Sateli
 * */
public class EncryptionUtils {

	/** Class singleton object */
	private static EncryptionUtils instance = null;
	/** KeyPair encryption algorithm */
	private final static String algorithm = "RSA";
	/** KeyPair key size (bits) */
	private final static int keySize = 1024;

	/** Publickey specs */
	private RSAPublicKeySpec pubKeySpec = null;
	/** PrivateKey specs */
	private RSAPrivateKeySpec priKeySpec = null;

	/** Protected class constructor to defeat instantiation. */
	protected EncryptionUtils(){
		// Defeat instantiation
	}

	/** Returns the class singleton object.
	 * @return the singleton object */
	public static EncryptionUtils getInstance(){
		if ( instance == null ){
			instance = new EncryptionUtils();
		}
		return instance;
	}

	/**
	 * Generates a key-pair based on the specified algorithm.
	 * @return KeyPair or null if an exception is thrown
	 */
	public KeyPair generateKeyPair(){
		KeyPair pair = null;
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance(algorithm);
			kpg.initialize(keySize);
			pair = kpg.generateKeyPair();
			return pair;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return pair;
	}

	/**
	 * Extracts a public key from the provided KeyPair.
	 * @param pair a key-pair
	 * @return PublicKey or null
	 */
	public PublicKey getPublicKey(final KeyPair pair){
		PublicKey pubKey = null;
		try {
			PublicKey publicKey = pair.getPublic();
			KeyFactory factory = KeyFactory.getInstance(algorithm);
			pubKeySpec = factory.getKeySpec(publicKey, RSAPublicKeySpec.class);
			pubKey = factory.generatePublic(pubKeySpec);
			return pubKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return pubKey;
	}

	/**
	 * Extracts a private key from the provided KeyPair.
	 * @param pair a key-pair
	 * @return PrivateKey or null
	 */
	public PrivateKey getPrivateKey(final KeyPair pair){
		PrivateKey priKey = null;
		try {
			PrivateKey privateKey = pair.getPrivate();
			KeyFactory factory = KeyFactory.getInstance(algorithm);
			priKeySpec = factory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
			priKey = factory.generatePrivate(priKeySpec);
			return priKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return priKey;
	}

	/** Gets the modulus part of the provided publickey.
	 * @param key the PublicKey
	 * @return Big integer representation of the modulus */
	public BigInteger getModulus(final PublicKey key){
		return pubKeySpec.getModulus();
	}

	/** Gets the public exponent of the provided publickey.
	 * @param key the PublicKey
	 * @return Big integer representation of the public exponent */
	public BigInteger getPubEx(final PublicKey key){
		return pubKeySpec.getPublicExponent();
	}

	/** Gets the private exponent of the provided privatekey.
	 * @param key the PrivateKey
	 * @return Big integer representation of the private exponent */
	public BigInteger getPriEx(final PrivateKey key){
		return priKeySpec.getPrivateExponent();
	}

	/** Decrypts the encrypted input with the provided session key.
	 * @param input encrypted data
	 * @param sessionKey session key to use
	 * @param siv session key initialization vector
	 * @return decrypted input string 
	 */
	public String decryptInputData(String input, final byte[] sessionKey, final byte[] siv){
		String decryptedText = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
			SecretKeySpec keySpec = new SecretKeySpec(sessionKey, "AES");
			cipher.init(Cipher.DECRYPT_MODE,keySpec, new IvParameterSpec(siv));
			byte[] decryptedData = cipher.doFinal(Base64.decodeBase64(input));
			decryptedText = new String(decryptedData, "UTF-8");
			return decryptedText;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return decryptedText;
	}

	/** Decrypts the client's session key using his PrivateKey.
	 * @param cipherData encrypted session key string
	 * @param key the private key to use
	 * @return decrypted session key in a byte array */
	public byte[] decryptSessionKey(String cipherData, final PrivateKey key){
		byte[] original = null;
		try {
			Cipher cipher;
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			original = cipher.doFinal(Base64.decodeBase64(cipherData));
			SecretKey secretKey =  new SecretKeySpec (original, "AES");
			return secretKey.getEncoded();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return original;
	}

	public String decryptTest(String test, final PrivateKey key) {
		try {
			Cipher cipher;
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] original = cipher.doFinal(Base64.decodeBase64(test));
			
			return new String(original, "UTF-8");
			//return original;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}