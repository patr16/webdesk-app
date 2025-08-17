package com.nic.webdesk;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.security.MessageDigest;
import java.security.SecureRandom;
public class LoginEncrypt {

        private static final int SALT_LENGTH = 16; // Length of salt in bytes
    static long start = System.currentTimeMillis();
        // Method to generate a random salt
        private static byte[] generateRandomSalt(int length) {
            byte[] salt = new byte[length];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(salt);
            return salt;
        }

        // Metodo per convertire un byte array in una stringa esadecimale
        private static String toHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }

        // Metodo per convertire una stringa esadecimale in un byte array
        private static byte[] fromHexString(String hexString) {
            int len = hexString.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                        + Character.digit(hexString.charAt(i + 1), 16));
            }
            return data;
        }

        //------------------------------------------------- encrypt
        // Method to encrypt a password
        public static String encrypt(String credential) {
            
            System.out.println("Encrypt - credential: " + credential);
            // Generate a random salt
            byte[] salt = generateRandomSalt(SALT_LENGTH);
            // Create an instance of Argon2
            // iterations   server profile 4-10             mobile profile 1-2
            // memory       server profile 65536 (64 mb)    mobile profile 8192-16384 (8-16 mb)
            // parallelism  erver profile  2-4              mobile profile 1
            Argon2BytesGenerator argon2 = new Argon2BytesGenerator();
            Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                    .withIterations(1)
                    .withMemoryAsKB(8192)
                    .withParallelism(1)
                    .withSalt(salt)
                    .build();
            // Calculate the hash of the password
            byte[] hashBytes = new byte[32]; // Adjust the size as needed
            argon2.init(parameters);
            argon2.generateBytes(credential.getBytes(), hashBytes);
            //------------------------------ Verify
            boolean myVerify;
            myVerify = verify(credential,toHexString(salt),toHexString(hashBytes));
            // Return the salt and hash concatenated
            return toHexString(salt) + ":" + toHexString(hashBytes);
        }

        //------------------------------------------------- verify
        // Method to verify a password
        public static boolean verify(String credential, String saltCredential, String hashCredential) {
            byte[] salt = fromHexString(saltCredential);
            byte[] hashBytes = fromHexString(hashCredential);
            // Create an instance of Argon2
            Argon2BytesGenerator argon2 = new Argon2BytesGenerator();
            Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                    .withIterations(1)
                    .withMemoryAsKB(8192)
                    .withParallelism(1)
                    .withSalt(salt)
                    .build();
            // Verify the password
            byte[] inputHashBytes = new byte[32]; // Adjust the size as needed
            argon2.init(parameters);
            argon2.generateBytes(credential.getBytes(), inputHashBytes);
            boolean verified = MessageDigest.isEqual(hashBytes, inputHashBytes);
            return verified;
        }
    }