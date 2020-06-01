package com.apptastic.fininsyn.rssfeed;

import com.apptastic.rssreader.Item;

import static com.apptastic.fininsyn.utils.TextUtil.containsAny;


public class RssFeedFilter {

    public static final String[] COMMON_KEYWORDS = { "börs", "aktie", "terminer", "rörelseresultat", "värdepapper",
            "periodresultat", "årsresultat", "kvartalsresultat", "kvartalsrapport", "rekyl", "konkurrera", "kunder",
            "årsrapport", "delårsrapport", "halvårsrapport", "rapport", "rapportflod", "riktkurs", "fintech", "ipo",
            "förvärv", "uppköp", "räntebesked", "reporänta", "styrräntan", "räntehöjning", "räntesäkning", "intäkter",
            "centralbank", "riksbank", "imf", "ebm", "ecb", "fed", "emu", "bnp", "redovisar ett resultat", "redovisade ett result",
            "omsättning", "efterhandeln", "rekommendation", "räkenskapsår", "tecknat avtal", "tecknar avtal", "stort avtal",
            "avtal kring", "bruttomarginal", "nettoomsätt", "kreditförlust", "övertecknad", "dagens vinnare",
            "vinstvarn", "omsätt", "intäkt", "lönsam", "tillväxt", "kvartalet", "emission", "licens", "beställning",
            "leverans", "bitcoin", "kryptovalut", "blanka", "blankning", "storägaren", "ramavtal", "prognos",
            "handelskrig", "handelskonflikt", "handelsoro", "handelsamtal", "handelssamtal", "tullar", "frihandel", "riskkapital",
            "budrykte", "varsla", "varsel", "händer idag", "rusa", "rusning", "investera", "forsk", "startup", "strejk",
            "köpläge", "säljläge", "lansera", "entreprenör", "analytiker", "innovation", "hajp", "milstolpe",
            "forskning", "rasar", "böter", "lägger ner", "upphandling", "patent", "förhandel", "konjunktur", "studie",
            "förvänt", "försäljning", "marknadsandelar", "genombrott", "bötfälls", "vinstlyft", "spelbolag",
            "vinstfall", "nedskrivning", "blockchain", "blockkedja", "omstrukturera", "förvaltning", "verksamhet",
            "köpeskilling", "marknadsleda", "revolutionera", "nätkund", "analys", "förlorare", "råvaror", "rekord",
            "penningtvätt", "kross", "grundaren", "slakt", "värt", "raket", "skena", "dubblar", "raset", "faller",
            "konkurren", "elpris", "köper", "avyttra", "artificiell", "konkurrent", "marknadsled", "import", "export",
            "sysselsättning", "stämning", "stämmer", "stäms", "pressmeddelande", "jobbsiffror", "konkurs", "noter",
            "uppstickare", "köprek", "säljrek", "köpråd", "säljråd", "neutralt råd", "finansering", "handelsstopp",
            "rally", "biometri", "marknadsmanipulation", "kursmanipulation", "marknadsmissbruk", "inside", "förvärv",
            "finansinspektionen", "finansmyndighet", "rekonstruktion", "ägardata", "backar", "satsning", "satsar",
            "fusion", "läkemedel", "inflation", "deflation", "recession", "rekordlåg", "permitter", "rekommendation",
            "personalneddrag", "cannabis", "tvångsinlös", "licens", "växtbaser", "straffavgift", "sanktionsavgift",
            "stödpaket", "stödåtgärder", "wall street", "nasdaq", "nyse", "omx", "värdering", "utdelning"};

    public static final String[] COMMON_IGNORE_KEYWORDS = { "politiker", "partiet", "väljarbarometer", "deklaration",
            "eu-val", "jämställdhet", "jag", "läsarna" };

    public static boolean filterContentScb(Item item) {
        String content = getContent(item);
        return !containsAny(content, "politik", "parti", "barn", "föräld", "skola", "vi söker", "allmänna val",
                "folkvald", "kommunfullmäktig", "folkmängd", "fritidshus", "kultur", "konst", "natur", "barn",
                "medellivslängd", "utbildningsnivå");
    }

    public static boolean filterContentEkobrottsmyndigheten(Item item) {
        String content = getContent(item);
        return !containsAny(content, "vi söker", "söker", "tjänsten", "högskol", "trafikskol", "välfärd",
                "friskol", "namnstatistik", "tilltalsnamn", "miljöräkenskaper");
    }

    public static boolean filterContentRiksbanken(Item item) {
        String content = getContent(item);
        return !containsAny(content, "vi söker", "söker", "tjänsten", "konferens", "seminarium", "webbinarium",
                "protokoll");
    }

    public static boolean filterContentFinanspolitiskaRadet(Item item) {
        String content = getContent(item);
        return !containsAny(content, "vi söker", "söker", "tjänsten", "konferens", "seminarium", "webbinarium",
                "pressträff", "inbjudan");
    }

    public static boolean filterContentKonjunkturinstitutet(Item item) {
        String content = getContent(item);
        return !containsAny(content, "vi söker", "söker", "tjänsten", "konferens", "seminarium", "webbinarium");
    }

    public static boolean filterContentVeckansAffarer(Item item) {
        String content = getContent(item);
        return containsAny(content, COMMON_KEYWORDS) && !containsAny(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentRealtid(Item item) {
        String content = getContent(item);
        return containsAny(content, COMMON_KEYWORDS) && !containsAny(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentPlacera(Item item) {
        String content = getContent(item);
        return !containsAny(content, "tv:") && containsAny(content, COMMON_KEYWORDS) && !containsAny(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentBreakit(Item item) {
        String content = getContent(item);
        return containsAny(content, COMMON_KEYWORDS) && !containsAny(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentAffarsvarlden(Item item) {
        String content = getContent(item);
        return !containsAny(content, "analys+", "portfölj", "köper aktier", "säljer aktier") && containsAny(content, COMMON_KEYWORDS) && !containsAny(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentInvestingCom(Item item) {
        String content = getContent(item);
        return !containsAny(content, "nettoköper", "köper", "köpt", "nettosäljer", "säljer", "sålt", "mäklar", "mäklade",
                "emission", "exklusive") && containsAny(content, COMMON_KEYWORDS) && !containsAny(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentDiDigital(Item item) {
        String content = getContent(item);
        return containsAny(content, COMMON_KEYWORDS) && !containsAny(content, COMMON_IGNORE_KEYWORDS);
    }

    public static boolean filterContentFiSanktioner(Item item) {
        return true;
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
