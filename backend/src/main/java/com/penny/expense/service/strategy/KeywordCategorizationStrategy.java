package com.penny.expense.service.strategy;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keyword-based implementation of {@link CategorizationStrategy}.
 *
 * OCP: This is one concrete strategy. A future ML-based or DB-driven
 * strategy simply implements CategorizationStrategy and replaces this
 * bean — no consumers change.
 *
 * SRP: This class has exactly one job: match a vendor string to a
 * category using an ordered keyword map. It knows nothing about
 * persistence, HTTP, or CSV parsing.
 *
 * The map uses LinkedHashMap to preserve insertion order.
 * More-specific keywords (e.g. "uber eats") must appear before
 * broader ones (e.g. "uber") to win the first-match scan.
 */
@Component
public class KeywordCategorizationStrategy implements CategorizationStrategy {

    private static final String DEFAULT_CATEGORY = "Other";

    // Insertion-ordered: specific entries first, broader entries after
    private static final Map<String, String> RULES = new LinkedHashMap<>();

    static {
        // ── Food & Dining 
        RULES.put("uber eats",       "Food");
        RULES.put("swiggy",          "Food");
        RULES.put("zomato",          "Food");
        RULES.put("doordash",        "Food");
        RULES.put("grubhub",         "Food");
        RULES.put("instacart",       "Food");
        RULES.put("mcdonald",        "Food");
        RULES.put("starbucks",       "Food");
        RULES.put("subway",          "Food");
        RULES.put("dominos",         "Food");
        RULES.put("pizza hut",       "Food");
        RULES.put("kfc",             "Food");
        RULES.put("dunkin",          "Food");
        RULES.put("chipotle",        "Food");
        RULES.put("panera",          "Food");
        RULES.put("barbeque nation", "Food");
        RULES.put("haldirams",       "Food");

        // ── Transport ────
        RULES.put("air india",       "Transport");
        RULES.put("make my trip",    "Transport");
        RULES.put("makemytrip",      "Transport");
        RULES.put("indigo",          "Transport");
        RULES.put("spicejet",        "Transport");
        RULES.put("redbus",          "Transport");
        RULES.put("irctc",           "Transport");
        RULES.put("rapido",          "Transport");
        RULES.put("uber",            "Transport");   // after "uber eats"
        RULES.put("ola",             "Transport");
        RULES.put("lyft",            "Transport");
        RULES.put("metro",           "Transport");
        RULES.put("airways",         "Transport");
        RULES.put("airline",         "Transport");

        // ── Shopping
        RULES.put("amazon",          "Shopping");
        RULES.put("flipkart",        "Shopping");
        RULES.put("myntra",          "Shopping");
        RULES.put("ajio",            "Shopping");
        RULES.put("nykaa",           "Shopping");
        RULES.put("walmart",         "Shopping");
        RULES.put("target",          "Shopping");
        RULES.put("ebay",            "Shopping");
        RULES.put("meesho",          "Shopping");

        // ── Entertainment 
        RULES.put("prime video",     "Entertainment");
        RULES.put("apple music",     "Entertainment");
        RULES.put("netflix",         "Entertainment");
        RULES.put("spotify",         "Entertainment");
        RULES.put("hotstar",         "Entertainment");
        RULES.put("youtube",         "Entertainment");
        RULES.put("zee5",            "Entertainment");
        RULES.put("sonyliv",         "Entertainment");
        RULES.put("steam",           "Entertainment");
        RULES.put("playstation",     "Entertainment");
        RULES.put("xbox",            "Entertainment");

        // ── Utilities
        RULES.put("tata power",      "Utilities");
        RULES.put("bses",            "Utilities");
        RULES.put("airtel",          "Utilities");
        RULES.put("jio",             "Utilities");
        RULES.put("vodafone",        "Utilities");
        RULES.put("bsnl",            "Utilities");
        RULES.put("electricity",     "Utilities");
        RULES.put("water bill",      "Utilities");
        RULES.put("gas bill",        "Utilities");

        // ── Health
        RULES.put("apollo",          "Health");
        RULES.put("medplus",         "Health");
        RULES.put("1mg",             "Health");
        RULES.put("netmeds",         "Health");
        RULES.put("pharmeasy",       "Health");
        RULES.put("cult fit",        "Health");
        RULES.put("gym",             "Health");
        RULES.put("hospital",        "Health");
        RULES.put("clinic",          "Health");

        // ── Finance
        RULES.put("insurance",       "Finance");
        RULES.put("lic",             "Finance");
        RULES.put("hdfc",            "Finance");
        RULES.put("icici",           "Finance");
        RULES.put("sbi",             "Finance");
        RULES.put("loan",            "Finance");
        RULES.put("emi",             "Finance");
    }

    @Override
    public String categorize(String vendorName) {
        if (vendorName == null || vendorName.isBlank()) {
            return DEFAULT_CATEGORY;
        }
        String normalised = vendorName.toLowerCase().trim();
        return RULES.entrySet().stream()
                .filter(entry -> normalised.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(DEFAULT_CATEGORY);
    }

    @Override
    public Map<String, String> getRules() {
        return Map.copyOf(RULES);
    }
}
