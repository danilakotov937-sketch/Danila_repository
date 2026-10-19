package booking.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeIntervalTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 19);

    private TimeInterval at(String from, String to) {
        return TimeInterval.of(DAY, LocalTime.parse(from), LocalTime.parse(to));
    }

    @Test
    void endBeforeStartThrows() {
        assertThrows(IllegalArgumentException.class, () -> at("12:00", "11:00"));
    }

    @Test
    void zeroLengthIntervalThrows() {
        assertThrows(IllegalArgumentException.class, () -> at("12:00", "12:00"));
    }

    @Test
    void overlapsAndAdjacentIntervals() {
        assertTrue(at("10:00", "12:00").overlaps(at("11:00", "13:00")));
        assertTrue(at("11:00", "13:00").overlaps(at("10:00", "12:00")));
        assertTrue(at("10:00", "14:00").overlaps(at("11:00", "12:00")));
        assertFalse(at("10:00", "11:00").overlaps(at("11:00", "12:00"))); // встык - не пересекаются
    }
}
