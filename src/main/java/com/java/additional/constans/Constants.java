package com.java.additional.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
    private final static List<Character> ALPHABET_EN = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z');

    private final static List<Character> ALPHABET_UA = Arrays.asList('А', 'Б', 'В', 'Г', 'Ґ', 'Д', 'Е', 'Є', 'Ж', 'З',
            'И', 'І', 'Ї', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ь', 'Ю',
            'Я', 'а', 'б', 'в', 'г', 'ґ', 'д', 'е', 'є', 'ж', 'з', 'и', 'і', 'ї', 'й', 'к',
            'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ь', 'ю', 'я');

    private final static List<Character> SYMBOLS = Arrays.asList('.', ',', '«', '»', '"', '\'', ':', '!', '?', ' ');

    public final static List<String> COMMON_WORDS_EN = Arrays.asList("the", "and", "is", "in", "it", "you", "that", "he", "was", "for",
            "on", "are", "with", "as", "I", "his", "they", "be", "at", "one",
            "have", "this", "from", "or", "had", "by", "hot", "word", "but", "what",
            "some", "we", "can", "out", "other", "were", "all", "there", "when", "up",
            "use", "your", "how", "said", "an", "each", "she", "which", "do", "their");

    public final static List<String> COMMON_WORDS_UA = Arrays.asList("на", "що", "як", "він", "вона", "це", "вони",
            "від", "до", "так", "або", "але", "не", "я", "ти", "ми", "ви",
            "від", "до", "цей", "той", "який", "свій", "якщо", "коли", "де", "тут",
            "там", "тоді", "теж", "ось", "цього", "того", "тому", "мене", "тебе", "нас");

    public static List<Character> finalAlphabet = new ArrayList<>();

    private static void creatingAlphSymb(List<Character> alphabet) {
        finalAlphabet.addAll(alphabet);
        finalAlphabet.addAll(SYMBOLS);
    }

    public static boolean isEnglish(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                if (ALPHABET_EN.contains(text.charAt(i))) {
                    creatingAlphSymb(ALPHABET_EN);
                    return true;
                } else if (ALPHABET_UA.contains(text.charAt(i))) {
                    creatingAlphSymb(ALPHABET_UA);
                    return false;
                }
            }
        }
        throw new RuntimeException("Unsupported language");
    }

    public static int containsCounter(List<String> commonWordsLang, String text){
        int counter = 0;
        for (String s : commonWordsLang) {
            if (text.contains(s)) {
                counter++;
            }
        }
        return counter;
    }
}