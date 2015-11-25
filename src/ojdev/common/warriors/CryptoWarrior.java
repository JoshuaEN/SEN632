package ojdev.common.warriors;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import ojdev.common.Armory;
import ojdev.common.actions.Action;
import ojdev.common.exceptions.CryptoKeyProblemUnlockException;
import ojdev.common.exceptions.CryptoException;
import ojdev.common.weapons.Weapon;
import ojdev.common.weapons.WeaponDamageType;

public class CryptoWarrior extends WarriorBase {

	private static final long serialVersionUID = 1814946151379871816L;
	
	private transient SecretKey key;
	private transient String keySalt;
	private transient int keyIterations = DEFAULT_KEY_ITERATIONS;
	
	/*
	 * This is REALLY low,
	 * it's this low because file saving is part of the event dispatch thread,
	 * which in of itself is already not ideal even without having an effective
	 * number of iterations for an algorithm which very intent is to increase
	 * the time required to brute force passwords by adding work.
	 */
	public static final int DEFAULT_KEY_ITERATIONS = 1000;
	
	public static final String TYPE_NAME = "Crypto Warrior";
	
	public static final String FILE_EXTENSION = "wsec";
	
	public static final List<Weapon> USEABLE_WEAPONS;
	
	
	
	static {
		List<Weapon> tempUsableWeapons = new ArrayList<Weapon>();
		
		tempUsableWeapons.add(Armory.NO_WEAPON);
		tempUsableWeapons.add(Armory.GREAT_SWORD);
		tempUsableWeapons.add(Armory.SWORD);
		tempUsableWeapons.add(Armory.SPEAR);
		tempUsableWeapons.add(Armory.HALBERD);
		
		USEABLE_WEAPONS = Collections.unmodifiableList(tempUsableWeapons);
	}

	public CryptoWarrior(String name, String originLocation, String description, int health, Weapon equippedWeapon, String password) throws UnusableWeaponException {
		super(name, originLocation, description, health, equippedWeapon);
		try {
			this.keySalt = KeyDerivation.getSalt();
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
		this.key = getKey(password);
	}
	
	public CryptoWarrior(String name, String originLocation, String description, int health, String password) throws UnusableWeaponException {
		this(name, originLocation, description, health, Armory.NO_WEAPON, password);
	}
	
	public CryptoWarrior(Map<String, String> outValues) throws UnusableWeaponException {
		super(contstructHelper(outValues));
		keySalt = outValues.get("unlockKeySalt");
		keyIterations = Integer.parseInt(outValues.get("unlockKeyIterations"));
		key = getKey(outValues.get("unlockKey"));
	}
	
	public SecretKey getKey() {
		return key;
	}
	
	public void setKey(SecretKey key) {
		this.key = key;
	}
	
	public String getSalt() {
		return keySalt;
	}
	
	public void setSalt(String salt) {
		keySalt = salt;
	}
	
	public int getIterations() {
		return keyIterations;
	}
	
	public void setIterations(int iterations) {
		keyIterations = iterations;
	}
	
	@Override
	public String getFileName() {
		try {
			return String.format("%s.%s", SHA256.getHash(getName()), getFileExtension());
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
	}
	
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	@Override
	public String getFileExtension() {
		return FILE_EXTENSION;
	}

	@Override
	public List<Weapon> getUsableWeapons() {
		return USEABLE_WEAPONS;
	}
	
	@Override
	public int getDamageDone(Weapon attackersWeapon, Action attackersAction, Action ourAction) {
		int base = super.getDamageDone(attackersWeapon, attackersAction, ourAction);
		
		int result = base;
		
		if(base <= 0) {
			return base;
		}
		
		WeaponDamageType damageType = attackersWeapon.getDamageTypeForAction(attackersAction);
		
		// Crypto warrior has a very heavy suit that provides very good protection from most attacks,
		// however, because it is heavy and low on power, blunt impacts can
		// be quite dangerous for the occupant
		if(damageType == WeaponDamageType.BLUNT) {
			result = base * 2;
		} else {
			result = base / 2;
		}
		
		return result;
	}
	
	/**
	 * Get around Java's restriction on the call to this() or super() constructor to be the first statement.
	 * 
	 * This code loads the crypto related data, uses it to decrypt the stored
	 * encrypted data, then parses that newly decrypted data,
	 * verifies the integrity of the decrypted data, and finally
	 * passes the data onto the constructor for the CryptoWarrior to create
	 * a new instance of the object.
	 */
	private static Map<String, String> contstructHelper(Map<String, String> values) {
		String unlockKey = values.get("unlockKey");
		String unlockKeySalt = values.get("unlockKeySalt");
		String unlockKeyIterationsString = values.get("unlockKeyIterations");
		String lockedContent = values.get("crypt");
		
		if(unlockKey == null) {
			throw new CryptoKeyProblemUnlockException("Missing required field: unlockKey");
		}
		
		if(unlockKeySalt == null) {
			throw new IllegalArgumentException("Missing required field: unlockKeySalt");
		}
		
		
		int unlockKeyIterations;
		
		if(unlockKeyIterationsString == null) {
			throw new IllegalArgumentException("Missing required field: unlockKeyIterations");
		} else {
			try {
				unlockKeyIterations = Integer.parseInt(unlockKeyIterationsString);
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("Invalid required field (bad number): unlockKeyIterations", e);
			}
		}
		
		if(lockedContent == null) {
			throw new IllegalArgumentException("Missing required field: crypt");
		}
		
		SecretKey secretKey;
		String rawDecryptedValues;
		try {
			secretKey = KeyDerivation.generatePasswordHash(unlockKey, unlockKeyIterations, unlockKeySalt);

			rawDecryptedValues = EncryptionDecryptionAES.decrypt(lockedContent, secretKey);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			throw new CryptoException(e);
		}
				
		String checksum = rawDecryptedValues.substring(0, 64);
		rawDecryptedValues = rawDecryptedValues.substring(64);
		
		String loadedDataChecksum;
		try {
			loadedDataChecksum = SHA256.getHash(rawDecryptedValues);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
		
		if(checksum.equals(loadedDataChecksum) != true) {
			throw new CryptoException("Checksums do not match");
		}
		
		Map<String, String> newValues = readInputFromOutputStream(new Scanner(new StringReader(rawDecryptedValues)));
		
		
		for(Entry<String, String> entry : newValues.entrySet()) {
			values.put(entry.getKey(), entry.getValue());
		}

		return newValues;
	}
	
	@Override
	protected void writeToOutputStream(Formatter writer) {
		StringWriter encryptWriter = new StringWriter();
		super.writeToOutputStream(new Formatter(encryptWriter));
		
		String checksum;
		String encryptedText;
		
		try {
			checksum = SHA256.getHash(encryptWriter.toString());
			encryptedText = EncryptionDecryptionAES.encrypt(checksum + encryptWriter.toString(), key);
		} catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new CryptoException(e);
		}
		
		writeValueToOutputStream(writer, "type", getClass().getName());
		writeValueToOutputStream(writer, "crypt", encryptedText);
		writeValueToOutputStream(writer, "unlockKeySalt", keySalt);
		writeValueToOutputStream(writer, "unlockKeyIterations", keyIterations);
		writer.flush();
	}
	
	private SecretKey getKey(String password) {

		try {
			return KeyDerivation.generatePasswordHash(password, keyIterations, keySalt);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException(e);
		}
	}
}

// From http://javapapers.com/java/java-symmetric-aes-encryption-decryption-using-jce/
class EncryptionDecryptionAES {
	private static Cipher cipher;

	static {
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch(Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static String encrypt(String plainText, SecretKey secretKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		
		byte[] plainTextByte = plainText.getBytes();
		SecretKeySpec spec = new SecretKeySpec(secretKey.getEncoded(), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, spec);
		byte[] encryptedByte = cipher.doFinal(plainTextByte);
		Base64.Encoder encoder = Base64.getEncoder();
		String encryptedText = encoder.encodeToString(encryptedByte);
		return encoder.encodeToString(cipher.getIV()) + ":" + encryptedText;
	}

	public static String decrypt(String encryptedText, SecretKey secretKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		String[] parts = encryptedText.split(":"); 
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] iv = decoder.decode(parts[0]);
		byte[] encryptedTextByte = decoder.decode(parts[1]);
		SecretKeySpec spec = new SecretKeySpec(secretKey.getEncoded(), "AES");
		cipher.init(Cipher.DECRYPT_MODE, spec, new IvParameterSpec(iv));
		byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
		String decryptedText = new String(decryptedByte);
		return decryptedText;
	}
}

// From http://howtodoinjava.com/2013/07/22/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
class KeyDerivation
{	
    static SecretKey generatePasswordHash(String password, int iterations, String saltStr) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        char[] chars = password.toCharArray();
        byte[] salt = saltStr.getBytes();
         
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 16 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(hash, "PBKDF2WithHmacSHA1");
    }
     
    static String getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstanceStrong();//.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(salt);
    }
     
//    static String toHex(byte[] array) throws NoSuchAlgorithmException
//    {
//        BigInteger bi = new BigInteger(1, array);
//        String hex = bi.toString(16);
//        int paddingLength = (array.length * 2) - hex.length();
//        if(paddingLength > 0)
//        {
//            return String.format("%0"  +paddingLength + "d", 0) + hex;
//        }else{
//            return hex;
//        }
//    }
     
//    static boolean validatePassword(String originalPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException
//    {
//        String[] parts = storedPassword.split(":");
//        int iterations = Integer.parseInt(parts[0]);
//        byte[] salt = fromHex(parts[1]);
//        byte[] hash = fromHex(parts[2]);
//         
//        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
//        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//        byte[] testHash = skf.generateSecret(spec).getEncoded();
//         
//        int diff = hash.length ^ testHash.length;
//        for(int i = 0; i < hash.length && i < testHash.length; i++)
//        {
//            diff |= hash[i] ^ testHash[i];
//        }
//        return diff == 0;
//    }
//    
//    static SecretKey getSecretKeyFromHashedPassword(String hashedPassword) throws NoSuchAlgorithmException {
//    	String[] parts = hashedPassword.split(":");
//        int iterations = Integer.parseInt(parts[0]);
//        byte[] salt = fromHex(parts[1]);
//        byte[] hash = fromHex(parts[2]);
//        
//        return new SecretKeySpec(hash, "PBKDF2WithHmacSHA1");
//    }
//    
//    static byte[] fromHex(String hex) throws NoSuchAlgorithmException
//    {
//        byte[] bytes = new byte[hex.length() / 2];
//        for(int i = 0; i<bytes.length ;i++)
//        {
//            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
//        }
//        return bytes;
//    }
}

// From http://howtodoinjava.com/2013/07/22/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
class SHA256
{
	static String getHash(String input) throws NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] bytes = md.digest(input.getBytes());
		StringBuilder sb = new StringBuilder();
		for(int i=0; i< bytes.length ;i++)
		{
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

        return sb.toString();
	}
}
