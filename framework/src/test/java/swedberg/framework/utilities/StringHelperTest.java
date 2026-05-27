package swedberg.framework.utilities;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StringHelperTest {

    @Test
    void reverse_reversesString() {
        assertEquals("olleh", StringHelper.reverse("hello"));
    }

    @Test
    void reverse_returnsNullForNull() {
        assertNull(StringHelper.reverse(null));
    }

    @Test
    void reverse_returnsEmptyForEmpty() {
        assertEquals("", StringHelper.reverse(""));
    }

    @Test
    void isPalindrome_detectsPalindrome() {
        assertTrue(StringHelper.isPalindrome("racecar"));
    }

    @Test
    void isPalindrome_ignoresCaseAndSpaces() {
        assertTrue(StringHelper.isPalindrome("A man a plan a canal Panama"));
    }

    @Test
    void isPalindrome_returnsFalseForNonPalindrome() {
        assertFalse(StringHelper.isPalindrome("hello"));
    }

    @Test
    void isPalindrome_returnsFalseForNull() {
        assertFalse(StringHelper.isPalindrome(null));
    }
}
