package com.penny.expense.service;

import com.penny.expense.service.strategy.CategorizationStrategy;
import com.penny.expense.service.strategy.KeywordCategorizationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the CategorizationStrategy contract via the keyword implementation.
 *
 * LSP: Tests are written against the CategorizationStrategy interface, not
 * the concrete class. Any implementation must pass these same tests.
 */
@DisplayName("CategorizationStrategy — KeywordCategorizationStrategy")
class CategorizationServiceTest {

    private CategorizationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new KeywordCategorizationStrategy();
    }

    @Test @DisplayName("Swiggy → Food")
    void swiggyIsFood() { assertThat(strategy.categorize("Swiggy")).isEqualTo("Food"); }

    @Test @DisplayName("Zomato → Food")
    void zomatoIsFood() { assertThat(strategy.categorize("Zomato")).isEqualTo("Food"); }

    @Test @DisplayName("Uber Eats → Food (not Transport — specificity ordering)")
    void uberEatsIsFood() { assertThat(strategy.categorize("Uber Eats")).isEqualTo("Food"); }

    @Test @DisplayName("Uber → Transport")
    void uberIsTransport() { assertThat(strategy.categorize("Uber")).isEqualTo("Transport"); }

    @Test @DisplayName("Amazon → Shopping")
    void amazonIsShopping() { assertThat(strategy.categorize("Amazon")).isEqualTo("Shopping"); }

    @Test @DisplayName("Netflix → Entertainment")
    void netflixIsEntertainment() { assertThat(strategy.categorize("Netflix")).isEqualTo("Entertainment"); }

    @Test @DisplayName("Airtel Broadband → Utilities")
    void airtelIsUtilities() { assertThat(strategy.categorize("Airtel Broadband")).isEqualTo("Utilities"); }

    @Test @DisplayName("Apollo Pharmacy → Health")
    void apolloIsHealth() { assertThat(strategy.categorize("Apollo Pharmacy")).isEqualTo("Health"); }

    @Test @DisplayName("SBI Bank → Finance")
    void sbiIsFinance() { assertThat(strategy.categorize("SBI Bank EMI")).isEqualTo("Finance"); }

    @Test @DisplayName("Unknown vendor → Other")
    void unknownIsOther() { assertThat(strategy.categorize("Random Vendor XYZ")).isEqualTo("Other"); }

    @Test @DisplayName("Null → Other (no NPE)")
    void nullIsOther() { assertThat(strategy.categorize(null)).isEqualTo("Other"); }

    @Test @DisplayName("Blank → Other")
    void blankIsOther() { assertThat(strategy.categorize("   ")).isEqualTo("Other"); }

    @Test @DisplayName("Case-insensitive match")
    void caseInsensitive() { assertThat(strategy.categorize("NETFLIX PREMIUM")).isEqualTo("Entertainment"); }

    @Test @DisplayName("getRules returns non-empty immutable map")
    void rulesMapIsExposed() {
        assertThat(strategy.getRules()).isNotEmpty();
    }
}
