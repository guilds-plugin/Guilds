package me.glaremasters.guilds.utils;

/**
 * Created by GlareMasters
 * Date: 9/19/2018
 * Time: 1:11 PM
 */
public class AnnouncementUtil {

    public final static String unescape_perl_string(String oldstr) {


        oldstr = oldstr.replaceAll("&", "§");

        /*
         * In contrast to fixing Java's broken regex charclasses,
         * this one need be no bigger, as unescaping shrinks the string
         * here, where in the other one, it grows it.
         */

        StringBuffer newstr = new StringBuffer(oldstr.length());

        boolean saw_backslash = false;

        for (int i = 0; i < oldstr.length(); i++) {
            int cp = oldstr.codePointAt(i);
            if (oldstr.codePointAt(i) > Character.MAX_VALUE) {
                i++; /****WE HATES UTF-16! WE HATES IT FOREVERSES!!!****/
            }

            if (!saw_backslash) {
                if (cp == '\\') {
                    saw_backslash = true;
                } else {
                    newstr.append(Character.toChars(cp));
                }
                continue; /* switch */
            }

            if (cp == '\\') {
                saw_backslash = false;
                newstr.append('\\');
                newstr.append('\\');
                continue; /* switch */
            }

            switch (cp) {

                case 'r':
                    newstr.append('\r');
                    break; /* switch */

                case 'n':
                    newstr.append('\n');
                    break; /* switch */

                case 'f':
                    newstr.append('\f');
                    break; /* switch */

                /* PASS a \b THROUGH!! */
                case 'b':
                    newstr.append("\\b");
                    break; /* switch */

                case 't':
                    newstr.append('\t');
                    break; /* switch */

                case 'a':
                    newstr.append('\007');
                    break; /* switch */

                case 'e':
                    newstr.append('\033');
                    break; /* switch */

                /*
                 * A "control" character is what you get when you xor its
                 * codepoint with '@'==64.  This only makes sense for ASCII,
                 * and may not yield a "control" character after all.
                 *
                 * Strange but true: "\c{" is ";", "\c}" is "=", etc.
                 */
                case 'c': {
                    if (++i == oldstr.length()) {
                        return "trailing \\c";
                    }
                    cp = oldstr.codePointAt(i);
                    /*
                     * don't need to grok surrogates, as next line blows them up
                     */
                    if (cp > 0x7f) {
                        return "expected ASCII after \\c";
                    }
                    newstr.append(Character.toChars(cp ^ 64));
                    break; /* switch */
                }

                case '8':
                case '9':
                    return "illegal octal digit";
                /* NOTREACHED */

                /*
                 * may be 0 to 2 octal digits following this one
                 * so back up one for fallthrough to next case;
                 * unread this digit and fall through to next case.
                 */
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    --i;
                    /* FALLTHROUGH */

                    /*
                     * Can have 0, 1, or 2 octal digits following a 0
                     * this permits larger values than octal 377, up to
                     * octal 777.
                     */
                case '0': {
                    if (i + 1 == oldstr.length()) {
                        /* found \0 at end of string */
                        newstr.append(Character.toChars(0));
                        break; /* switch */
                    }
                    i++;
                    int digits = 0;
                    int j;
                    for (j = 0; j <= 2; j++) {
                        if (i + j == oldstr.length()) {
                            break; /* for */
                        }
                        /* safe because will unread surrogate */
                        int ch = oldstr.charAt(i + j);
                        if (ch < '0' || ch > '7') {
                            break; /* for */
                        }
                        digits++;
                    }
                    if (digits == 0) {
                        --i;
                        newstr.append('\0');
                        break; /* switch */
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(
                                oldstr.substring(i, i + digits), 8);
                    } catch (NumberFormatException nfe) {
                        return "invalid octal value for \\0 escape";
                    }
                    newstr.append(Character.toChars(value));
                    i += digits - 1;
                    break; /* switch */
                } /* end case '0' */

                case 'x': {
                    if (i + 2 > oldstr.length()) {
                        return "string too short for \\x escape";
                    }
                    i++;
                    boolean saw_brace = false;
                    if (oldstr.charAt(i) == '{') {
                        /* ^^^^^^ ok to ignore surrogates here */
                        i++;
                        saw_brace = true;
                    }
                    int j;
                    for (j = 0; j < 8; j++) {

                        if (!saw_brace && j == 2) {
                            break;  /* for */
                        }

                        /*
                         * ASCII test also catches surrogates
                         */
                        int ch = oldstr.charAt(i + j);
                        if (ch > 127) {
                            return "illegal non-ASCII hex digit in \\x escape";
                        }

                        if (saw_brace && ch == '}') {
                            break; /* for */
                        }

                        if (!((ch >= '0' && ch <= '9')
                                ||
                                (ch >= 'a' && ch <= 'f')
                                ||
                                (ch >= 'A' && ch <= 'F')
                        )
                        ) {
                            return(String.format(
                                    "illegal hex digit #%d '%c' in \\x", ch, ch));
                        }

                    }
                    if (j == 0) {
                        return "empty braces in \\x{} escape";
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i + j), 16);
                    } catch (NumberFormatException nfe) {
                        return "invalid hex value for \\x escape";
                    }
                    newstr.append(Character.toChars(value));
                    if (saw_brace) {
                        j++;
                    }
                    i += j - 1;
                    break; /* switch */
                }

                case 'u': {
                    if (i + 4 > oldstr.length()) {
                        return "string too short for \\u escape";
                    }
                    i++;
                    int j;
                    for (j = 0; j < 4; j++) {
                        /* this also handles the surrogate issue */
                        if (oldstr.charAt(i + j) > 127) {
                            return "illegal non-ASCII hex digit in \\u escape";
                        }
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i + j), 16);
                    } catch (NumberFormatException nfe) {
                        return "invalid hex value for \\u escape";
                    }
                    newstr.append(Character.toChars(value));
                    i += j - 1;
                    break; /* switch */
                }

                case 'U': {
                    if (i + 8 > oldstr.length()) {
                        return "string too short for \\U escape";
                    }
                    i++;
                    int j;
                    for (j = 0; j < 8; j++) {
                        /* this also handles the surrogate issue */
                        if (oldstr.charAt(i + j) > 127) {
                            return"illegal non-ASCII hex digit in \\U escape";
                        }
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i + j), 16);
                    } catch (NumberFormatException nfe) {
                        return "invalid hex value for \\U escape";
                    }
                    newstr.append(Character.toChars(value));
                    i += j - 1;
                    break; /* switch */
                }

                default:
                    newstr.append('\\');
                    newstr.append(Character.toChars(cp));
                    /*
                     * say(String.format(
                     *       "DEFAULT unrecognized escape %c passed through",
                     *       cp));
                     */
                    break; /* switch */

            }
            saw_backslash = false;
        }

        /* weird to leave one at the end */
        if (saw_backslash) {
            newstr.append('\\');
        }

        return newstr.toString();
    }

}