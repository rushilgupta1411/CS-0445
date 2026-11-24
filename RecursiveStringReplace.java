
/**
 * A class that has a methond to recursively replaces characters in a String.
 * 
 * @author Charles Hoot
 * @version 4.0
 */
public class RecursiveStringReplace
{

    /**
     * replace - Replace all instances of one character by another.
     * 
     * @param  s   The string to do the replacement on.
     * @param  from   The character to be replaced.
     * @param  to   the character to change to.
     * @return     A new string with the appropriate characters replaced.
     * @throws NullPointerException if s is null.
     */
    public String replace(String s, char from, char to)
    {
        // Base case: an empty string has no characters left to replace.
        if (s.isEmpty())
        {
            return "";
        }

        char first = s.charAt(0);
        if (first == from)
        {
            first = to;
        }

        // Replace the first character and recursively process the shorter tail.
        return first + replace(s.substring(1), from, to);
    }


}
