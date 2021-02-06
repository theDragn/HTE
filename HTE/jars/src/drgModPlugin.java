import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;


public class drgModPlugin extends BaseModPlugin
{

    @Override
    public void onApplicationLoad()
    {
        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
        if (!hasLazyLib)
            throw new RuntimeException("HTE requires LazyLib.\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444");

        boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
        if (!hasMagicLib)
            throw new RuntimeException("HTE requires MagicLib.\nGet it at http://fractalsoftworks.com/forum/index.php?topic=13718");
        
        boolean a = Global.getSettings().getModManager().isModEnabled(de("bmV3X2dhbGFjdGljX29yZGVy"));
        boolean b = Global.getSettings().getModManager().isModEnabled(de("YXJpYQ=="));
        if (a || b)
           throw new RuntimeException(de("SFRFIGlzIG5vdCBjb21wYXRpYmxlIHdpdGggTkdPIG9yIEFyaWEuIFBsZWFzZSB1bmluc3RhbGwgb25lIG9mIHRoZSB0d28gYW5kIHJlc3RhcnQgeW91ciBnYW1lLg=="));
        String c = "SSdtIG5vdCBsZXR0aW5nIHRoaXMgb2JmdXNjYXRpb24gY29kZSBnbyB0byB3YXN0ZS4=";
    }

    private static String de(String s)
    {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

        // remove/ignore any characters not in the base64 characters list
        // or the pad character -- particularly newlines
        s = s.replaceAll("[^" + chars + "=]", "");

        // replace any incoming padding with a zero pad (the 'A' character is
        // zero)
        String p = (s.charAt(s.length() - 1) == '=' ? (s.charAt(s.length() - 2) == '=' ? "AA" : "A") : "");
        String r = "";
        s = s.substring(0, s.length() - p.length()) + p;

        // increment over the length of this encoded string, four characters
        // at a time
        for (int c = 0; c < s.length(); c += 4)
        {

            // each of these four characters represents a 6-bit index in the
            // base64 characters list which, when concatenated, will give the
            // 24-bit number for the original 3 characters
            int n = (chars.indexOf(s.charAt(c)) << 18) + (chars.indexOf(s.charAt(c + 1)) << 12)
                    + (chars.indexOf(s.charAt(c + 2)) << 6) + chars.indexOf(s.charAt(c + 3));

            // split the 24-bit number into the original three 8-bit (ASCII)
            // characters
            r += "" + (char) ((n >>> 16) & 0xFF) + (char) ((n >>> 8) & 0xFF) + (char) (n & 0xFF);
        }

        // remove any zero pad that was added to make this a multiple of 24 bits
        return r.substring(0, r.length() - p.length());
    }
}
