package com.ivan;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Application {

    static class Game {

        private String [] points;

        private Random random = new Random();

        private int indexComputerChoice;

        private int indexUserChoice;

        public Game(String [] arr) {
            points = arr;
            computerMakeChoice();
        }

        public void printMenu() {
            System.out.println("Available moves:");
            AtomicInteger count = new AtomicInteger();
            Arrays.stream(points).forEach(str ->
                    System.out.println((count.incrementAndGet()) + " - " + str));
            System.out.println(0 + " - exit");
        }

        public boolean userMakeChoice() {
            try(Scanner scanner = new Scanner(System.in)) {
                int newValue;
                while(true) {
                    System.out.print(String.format("Enter your move [%s...%s, 0]: ", 1, points.length));
                    if(scanner.hasNextInt()) {
                        if ((newValue = scanner.nextInt() - 1) >= 0 && newValue < points.length) {
                            indexUserChoice = newValue;
                            return true;
                        } else if(newValue == -1) {
                            return false; // exit from game
                        }
                    }
                    System.out.println("Uncorrected value ... try again");
                    scanner.nextLine();
                }
            }
        }

        public void computerMakeChoice() {
            // генерировать случайно ... выбор PC
            indexComputerChoice = random.nextInt(points.length);
        }

        public int getIndexComputerChoice() {
            return indexComputerChoice;
        }

        public int getIndexUserChoice() {
            return indexUserChoice;
        }

        private void showResults() {
            System.out.println("Your move: " + points[getIndexUserChoice()]);
            System.out.println("Computer move: " + points[getIndexComputerChoice()]);
        }

        public void playing() {
            int valPC = getIndexComputerChoice();
            int [] arrWinFall = new int[points.length];
            arrWinFall[valPC] = -1;
            for(int i = 0, j = 0; i < (points.length - 1) / 2; i++) {
                if(valPC+1 < points.length) {
                    arrWinFall[valPC+1] = 1;
                    valPC++;
                } else
                    arrWinFall[j++] = 1;
            }
//            [0, -1, 1, 1, 0]
//            0-проиграл, 1-выйграл, -1-ничья

            showResults();
            if(-1 == arrWinFall[getIndexUserChoice()])
                System.out.println("ничья");
            else if(0 == arrWinFall[getIndexUserChoice()])
                System.out.println("You lose.");
            else
                System.out.println("You win!");
        }
    }

    static class HMAC {
        static public byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
            byte [] hmacSha256;
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
                mac.init(secretKeySpec);
                hmacSha256 = mac.doFinal(message);
            } catch (Exception e) {
                throw new RuntimeException("Failed to calculate hmac-sha256", e);
            }
            return hmacSha256;
        }
    }

    static byte [] generateSecretKey() {
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte [] values = new byte[32]; // 256 bit == 32 byte
            random.nextBytes(values);
            return values;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    static String convertBytesToHex(byte [] values) {
        StringBuilder sb = new StringBuilder();
        for (byte b : values) {
            sb.append(String.format("%X", b));
        }
        return sb.toString();
    }

    static boolean checkArgs(String ... args) {
        if(!(args.length >= 3 && args.length % 2 != 0)) {
            return false;
        }
        Set<String> set = new HashSet<>();
        for (String arg : args) {
            if (!set.add(arg))
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        if(checkArgs(args)) {
            byte[] secretKey = generateSecretKey();
            Game game = new Game(args);
            byte[] hMACAsBytes = HMAC.calcHmacSha256(secretKey, args[game.getIndexComputerChoice()].getBytes());
            System.out.println("HMAC: " + convertBytesToHex(hMACAsBytes));
            game.printMenu();
            // step from user
            if (game.userMakeChoice()) { // user set value
                game.playing();
            }
            System.out.println("Key: " + convertBytesToHex(secretKey));
        } else {
            System.err.println("Error: You should set 3 or more parameters and count of parameters should be odd!");
        }
    }
}
