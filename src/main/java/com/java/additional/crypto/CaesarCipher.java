package com.java.additional.crypto;

import com.java.additional.constants.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CaesarCipher {
    private String cryptomatic(String srcText, int key, String command) {
        if (command.equals("DECRYPT")) {
            key *= -1;
        }
        key %= Constants.finalAlphabet.size();
        int finalIndex = 0;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < srcText.length(); i++) {
            if (!Constants.finalAlphabet.contains(srcText.charAt(i))) {
                result.append(srcText.charAt(i));
            } else {
                finalIndex = getIndex(srcText, key, i);
                result.append(Constants.finalAlphabet.get(finalIndex));
            }
        }
        return result.toString();
    }

    private int getIndex(String srcText, int key, int i) {
        int finalIndex;
        finalIndex = Constants.finalAlphabet.indexOf(srcText.charAt(i)) + key;
        if (finalIndex >= Constants.finalAlphabet.size()) {
            finalIndex %= Constants.finalAlphabet.size();
        } else if (finalIndex < 0) {
            finalIndex = Constants.finalAlphabet.size() + finalIndex;
        }
        return finalIndex;
    }

    public void cipherWriter(String srcText, int key, String command, Path tgtFile) {
        try {
            Files.writeString(tgtFile, cryptomatic(srcText, key, command));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void brute(String srcText, Path tgtFile, boolean isEng) {
        int keyOfMaxCount = 0;
        int maxCount = Integer.MIN_VALUE;
        for (int i = 1; i < Constants.finalAlphabet.size(); i++) {
            String text = cryptomatic(srcText, i, "DECRYPT");
            int currCount;
            if (isEng) {
                currCount = Constants.containsCounter(Constants.COMMON_WORDS_EN, text);
            } else {
                currCount = Constants.containsCounter(Constants.COMMON_WORDS_UA, text);
            }
            if (currCount > maxCount) {
                maxCount = currCount;
                keyOfMaxCount = i;
            }
        }
        if (keyOfMaxCount == 0){
            System.out.println("Unable to identify key of your Cipher or text wasn't encrypted");
        } else {
            cipherWriter(srcText, keyOfMaxCount, "DECRYPT", tgtFile);
            System.out.println("Proposal key is " + keyOfMaxCount);
        }
    }


}