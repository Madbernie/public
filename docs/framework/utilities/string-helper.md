# StringHelper

Utility class for string operations in `swedberg.framework.utilities`.

## Methods

### `reverse(String input)`
Returns the reversed version of the input string.

| Input | Result |
|-------|--------|
| `"hello"` | `"olleh"` |
| `""` | `""` |
| `null` | `null` |

### `isPalindrome(String input)`
Returns `true` if the input is a palindrome (case-insensitive, ignoring spaces).

| Input | Result |
|-------|--------|
| `"racecar"` | `true` |
| `"A man a plan a canal Panama"` | `true` |
| `"hello"` | `false` |
| `null` | `false` |

## Build

```
.\gradlew :framework:build
```

## Test

```
.\gradlew :framework:test
```
