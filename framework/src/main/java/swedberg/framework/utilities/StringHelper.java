package swedberg.framework.utilities;

public class StringHelper {
    public static String reverse(String input) {
        if (input == null) return null;
        return new StringBuilder(input).reverse().toString();
    }

    public static boolean isPalindrome(String input) {
        if (input == null) return false;
        String cleaned = input.replaceAll("\\s+", "").toLowerCase();
        return cleaned.equals(new StringBuilder(cleaned).reverse().toString());
    }
}
