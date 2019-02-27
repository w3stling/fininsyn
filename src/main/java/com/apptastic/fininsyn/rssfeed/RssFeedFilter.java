package com.apptastic.fininsyn.rssfeed;

import com.apptastic.rssreader.Item;

import java.util.Arrays;

public class RssFeedFilter {
    public static final String[] COMMON_KEYWORDS = { "börs", "aktie", "terminer", "rörelseresultat", "värdepapper",
            "periodresultat", "årsresultat", "kvartalsresultat", "kvartalsrapport", "rekyl", "marknaden", "konkurrera", "kunder",
            "årsrapport", "delårsrapport", "halvårsrapport", "rapport", "rapportflod", "riktkurs", "släppt", "fintech",
            "förvärv", "uppköp", "räntebesked", "reporänta", "styrräntan", "räntehöjning", "räntesäkning", "intäkter",
            "riksbanken", "ecb", "fed", "bnp", "redovisar ett resultat", "omsättning", "efterhandeln", "rekommendation",
            "räkenskapsår", "tecknat avtal", "tecknar avtal", "stort avtal", "avtal kring", "bruttomarginal", "nettoomsätt",
            "kreditförlust", "övertecknad", "dagens vinnare", "vinstvarn", "omsätt", "intäkt", "lönsam", "tillväxt", "kvartalet",
            "emission", "licens", "beställning", "leverans", "bitcoin", "kryptovalut", "blanka", "blankning", "storägaren",
            "ramavtal", "prognos", "handelskrig", "handelskonflikt", "handelsoro", "tullar", "frihandel", "riskkapital",
            "budrykte", "varsla", "varsel", "händer idag", "rusa", "investera", "forsk", "värdering", "startup", "strejk",
            "köpläge", "säljläge", "lansera", "entreprenör", "analytiker", "innovation", "hajp", "milstolpe", "forskning",
            "rasar", "böter", "lägger ner", "upphandling", "patent", "förhandel", "konjunktur", "studie", "förvänt",
            "försäljning", "marknadsandelar", "genombrott", "bötfälls", "vinstlyft", "spelbolag", "vinstfall", "nedskrivning",
            "blockchain", "blockkedja", "omstrukturera", "förvaltning", "verksamhet", "köpeskilling", "marknadsleda",
            "revolutionera", "nätkund", "analys", "förlorare", "råvaror", "rekord", "penningtvätt", "kross", "grundaren",
            "slakt", "värt", "lyfte", "raket", "skena", "dubblar", "raset", "faller", "konkurren", "elpriser", "köper",
            "artificiell", "brist", "konkurrent", "marknadsled", "import", "export", "sysselsättning", "stämning", "stämmer",
            "pressmeddelande", "jobbsiffror", "konkurs", "noter", "uppstickare", "köprek", "säljrek", "finansering", "handelsstopp",
            "rally", "biometri", "marknadsmanipulation", "kursmanipulation", "marknadsmissbruk", "inside", "finansinspektionen" };

    public static final String[] COMMON_IGNORE_KEYWORDS = { "politiker", "väljarbarometer" };

    public static boolean filterContentScb(Item item) {
        String content = getContent(item);
        return !contains(content, "politik", "parti", "barn", "föräld", "skola", "vi söker", "allmänna val",
                "folkvald", "kommunfullmäktig", "folkmängd");
    }

    public static boolean filterContentEkobrottsmyndigheten(Item item) {
        String content = getContent(item);
        return !contains(content, "vi söker", "söker", "tjänsten", "högskoleprovsfusk", "namnstatistik", "tilltalsnamn", "miljöräkenskaper");
    }

    public static boolean filterContentRiksbanken(Item item) {
        String content = getContent(item);
        return !contains(content, "vi söker", "söker", "tjänsten");
    }

    public static boolean filterContentFinanspolitiskaradet(Item item) {
        String content = getContent(item);
        return !contains(content, "vi söker", "söker", "tjänsten");
    }

    public static boolean filterContentKonjunkturinstitutet(Item item) {
        String content = getContent(item);
        return !contains(content, "vi söker", "söker", "tjänsten");
    }

    public static boolean filterContentVeckansAffarer(Item item) {
        String content = getContent(item);
        return contains(content, COMMON_KEYWORDS) && !contains(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentRealtid(Item item) {
        String content = getContent(item);
        return contains(content, COMMON_KEYWORDS) && !contains(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentPlacera(Item item) {
        String content = getContent(item);
        return !contains(content, "tv:") && contains(content, COMMON_KEYWORDS) && !contains(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentBreakit(Item item) {
        String content = getContent(item);
        return contains(content, COMMON_KEYWORDS) && !contains(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentAffarsvarlden(Item item) {
        String content = getContent(item);
        return !contains(content, "analys+", "portfölj") && contains(content, COMMON_KEYWORDS) && !contains(content, COMMON_IGNORE_KEYWORDS);
    }

    private static boolean contains(String text, String... words) {
        return Arrays.stream(words)
                     .parallel()
                     .anyMatch(text::contains);
    }

    private static String getContent(Item item) {
        if (item == null)
            return "";

        StringBuilder builder = new StringBuilder();
        item.getTitle().ifPresent(t -> builder.append(t.toLowerCase()));
        item.getDescription().ifPresent(d -> builder.append(" ").append(d.toLowerCase()));
        return builder.toString();
    }

}
