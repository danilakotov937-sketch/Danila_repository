package booking.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Интервал времени [start, end). Конец в интервал не входит,
 * поэтому интервалы 10:00-11:00 и 11:00-12:00 не пересекаются.
 *
 * @param start начало интервала
 * @param end   конец интервала, строго позже начала
 */
public record TimeInterval(LocalDateTime start, LocalDateTime end) {

    public TimeInterval {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Время начала и конца должно быть задано");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Конец интервала должен быть позже начала");
        }
    }

    /** Создаёт интервал внутри одного дня. */
    public static TimeInterval of(LocalDate date, LocalTime from, LocalTime to) {
        return new TimeInterval(date.atTime(from), date.atTime(to));
    }

    /** Возвращает дату начала интервала. */
    public LocalDate date() {
        return start.toLocalDate();
    }

    /** Проверяет, пересекается ли этот интервал с другим. */
    public boolean overlaps(TimeInterval other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    @Override
    public String toString() {
        return start.toLocalDate() + " " + start.toLocalTime() + "-" + end.toLocalTime();
    }
}
