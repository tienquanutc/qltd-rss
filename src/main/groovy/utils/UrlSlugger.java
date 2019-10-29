package utils;


import java.util.regex.Pattern;

public class UrlSlugger {
    public static void main(String[] args) {
        System.out.println(UrlSlugger.slug("zzdhcz.com/video/2019-03-20/173726888/index.m3u8"));
        System.out.println("zzdhcz.com/video/2019-03-20/173726888/index.m3u8" .replace("https://", "")
                .replace(".m3u8", "_m3u8")
                .replaceAll("\\/", "_"));
    }

    private final static Pattern PATTERN_NORMALIZE_HYPHEN_SEPARATOR = Pattern.compile("[[\\p{Punct}]\\s+]+");
    private final static Pattern PATTERN_NORMALIZE_TRIM_DASH = Pattern.compile("^-|-$");

    private final static String EMPTY = "";
    private static final String HYPHEN = "-";

    public static String slug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "-";
        }
        input = PATTERN_NORMALIZE_HYPHEN_SEPARATOR.matcher(input).replaceAll(HYPHEN);
        input = PATTERN_NORMALIZE_TRIM_DASH.matcher(input).replaceAll(EMPTY);
        return input.toLowerCase();
    }

}
