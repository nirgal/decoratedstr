import java.util.Hashtable;
import java.util.Enumeration;
public class DecoratedStr {
    protected static Hashtable<Character,String> char_to_alternatives_lower = new Hashtable<Character,String>();
    protected static Hashtable<Character,String> char_to_alternatives = new Hashtable<Character,String>();
    protected static Hashtable<Character,Character> alternative_to_char = new Hashtable<Character,Character>();
    protected static Hashtable<Character,String> ligatures_expansions_lower = new Hashtable<Character,String>();
    protected static Hashtable<Character,String> ligatures_expansions = new Hashtable<Character,String>();
    protected static Hashtable<String,Character> ligatures_contractions = new Hashtable<String,Character>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized)
            return;
        char_to_alternatives_lower.put('a', "àáâãäåāăą");
        char_to_alternatives_lower.put('c', "çćĉċč");
        char_to_alternatives_lower.put('d', "ďđ");
        char_to_alternatives_lower.put('e', "èéêëēĕėęě");
        char_to_alternatives_lower.put('g', "ĝğġģ");
        char_to_alternatives_lower.put('h', "ĥħ");
        char_to_alternatives_lower.put('i', "ìíîïĩīĭįı");
        char_to_alternatives_lower.put('j', "ĵ");
        char_to_alternatives_lower.put('k', "ķ");
        char_to_alternatives_lower.put('l', "ĺļľŀł");
        char_to_alternatives_lower.put('n', "ñńņňŉŋ");
        char_to_alternatives_lower.put('o', "òóôöøōŏő");
        char_to_alternatives_lower.put('r', "ŕŗř");
        char_to_alternatives_lower.put('s', "śŝşš");
        char_to_alternatives_lower.put('t', "ţťŧ");
        char_to_alternatives_lower.put('u', "ùúûüũūŭůűų");
        char_to_alternatives_lower.put('w', "ŵ");
        char_to_alternatives_lower.put('y', "ýÿŷ");
        char_to_alternatives_lower.put('z', "źżž");

        Enumeration<Character> e = char_to_alternatives_lower.keys();
        while (e.hasMoreElements()) {
            Character k = e.nextElement();
            String v = char_to_alternatives_lower.get(k);
            char_to_alternatives.put(k, v);
            char_to_alternatives.put(Character.toUpperCase(k), v.toUpperCase());
            //System.out.println(Character.toUpperCase(k));
            //System.out.println(v.toUpperCase());
        }
        char_to_alternatives.put('I', "İ");

        e = char_to_alternatives.keys();
        while (e.hasMoreElements()) {
            Character k = e.nextElement();
            String v = char_to_alternatives.get(k);
            for (int i=v.length()-1; i>=0; --i) {
                char a = v.charAt(i);
                //System.out.println(k);
                //System.out.println(a);
                alternative_to_char.put(a, k);
            }
        }
        
        ligatures_expansions_lower.put('æ', "ae");
        //ligatures_expansions_lower.put('ĳ', "ij"); buggy: see http://en.wikipedia.org/wiki/Typographic_ligature
        ligatures_expansions_lower.put('œ', "oe");

        e = ligatures_expansions_lower.keys();
        while (e.hasMoreElements()) {
            Character k = e.nextElement();
            String v = ligatures_expansions_lower.get(k);
            ligatures_expansions.put(k, v);
            ligatures_contractions.put(v, k);
            String uv = Character.toUpperCase(v.charAt(0)) + v.substring(1);
            ligatures_expansions.put(Character.toUpperCase(k), uv);
            ligatures_contractions.put(uv, Character.toUpperCase(k));
        }

        initialized = true;
    }

    public static String remove_decoration(String txt) {
        init();
        String result = "";
        int len = txt.length();
        char l;
        Character al;
        String le;
        for (int i=0; i<len; ++i) {
            l = txt.charAt(i);
            al = alternative_to_char.get(l);
            if (al != null)
                l = al;
            le = ligatures_expansions.get(l);
            if (le != null)
                result += le;
            else
                result += l;
        }
        return result;
    }

    public static String decorated_match_single_char(char c, boolean case_sensitive) {
        init();
        if (!case_sensitive)
            c = Character.toLowerCase(c);
        String result = "" + c;
        String sa = char_to_alternatives.get(c);
        if (sa != null)
            result += sa;
        if (!case_sensitive) {
            String ur = result.toUpperCase();
            if (result.compareTo(ur) != 0)
                result += ur;
        }
        if (result.length() > 1)
            return "[" + result + "]";
        else
            return result;

    }

    public static String decorated_match(String txt, boolean case_sensitive) {
        init();
        String result = "";
        txt = remove_decoration(txt);
        if (!case_sensitive)
            txt = txt.toLowerCase();
        int len = txt.length();
        for (int i=0; i<len; ++i) {
            char c1 = txt.charAt(i);
            String c12 = "" + c1;
            if (i < len-1)
                c12 += txt.charAt(i+1);
            Character lc = ligatures_contractions.get(c12);
            if (lc != null) {
                result += "(" + lc;
                if (!case_sensitive)
                    result += "|" + Character.toUpperCase(lc);
                result += '|'
                       + decorated_match_single_char(c12.charAt(0), case_sensitive)
                       + decorated_match_single_char(c12.charAt(1), case_sensitive)
                       + ')';
                i += 1;

            }
            else
                result += decorated_match_single_char(c1, case_sensitive);
        }
        return result;
    }

	public static void main(String argv[]) {
        String in = "Œuf";
		System.out.println(in);
		System.out.println(remove_decoration(in));
		//System.out.println(decorated_match_single_char('m', true));
		//System.out.println(decorated_match_single_char('m', false));
		//System.out.println(decorated_match_single_char('h', true));
		//System.out.println(decorated_match_single_char('h', false));
		System.out.println(decorated_match(in, true));
		System.out.println(decorated_match(in, false));
	}
}
