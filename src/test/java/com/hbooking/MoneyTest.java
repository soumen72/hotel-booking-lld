package com.hbooking;

import com.hbooking.model.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void testArithmetic() {
        Money m1 = Money.of(100.0, "USD");
        Money m2 = Money.of(50.50, "USD");

        assertEquals(Money.of(150.50, "USD"), m1.add(m2));
        assertEquals(Money.of(49.50, "USD"), m1.subtract(m2));
        assertEquals(Money.of(200.0, "USD"), m1.multiply(2.0));
    }

    @Test
    void testCurrencyMismatchThrows() {
        Money usd = Money.of(100.0, "USD");
        Money inr = Money.of(100.0, "INR");

        assertThrows(IllegalArgumentException.class, () -> usd.add(inr));
        assertThrows(IllegalArgumentException.class, () -> usd.subtract(inr));
        assertThrows(IllegalArgumentException.class, () -> usd.compareTo(inr));
    }

    @Test
    void testNegativeAmountThrows() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(-10.0, "USD"));
    }

    @Test
    void testComparisons() {
        Money m1 = Money.of(100.0, "USD");
        Money m2 = Money.of(200.0, "USD");

        assertTrue(m1.isLessThan(m2));
        assertTrue(m2.isGreaterThan(m1));
        assertEquals(0, m1.compareTo(Money.of(100.0, "USD")));
    }
}
