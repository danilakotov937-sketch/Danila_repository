package booking.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/** Повторяющаяся бронь: каждую неделю в один и тот же день недели и время. */
public class WeeklyBooking extends Booking {
    private final DayOfWeek day;
    private final LocalTime start;
    private final LocalTime end;
    private final LocalDate from;
    private final LocalDate until;
    private final Set<LocalDate> skipped = new TreeSet<>();

    /** Создаёт серию: день недели, время и период (до until включительно). */
    public WeeklyBooking(int id, Room room, Employee employee, String title,
                         DayOfWeek day, LocalTime start, LocalTime end, LocalDate from, LocalDate until) {
        super(id, room, employee, title);
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Конец брони должен быть позже начала");
        }
        if (until.isBefore(from)) {
            throw new IllegalArgumentException("Конец периода раньше начала");
        }
        this.day = day;
        this.start = start;
        this.end = end;
        this.from = from;
        this.until = until;
    }

    /** Возвращает все даты серии (вместе с пропущенными). */
    public List<LocalDate> getDates() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = from.with(TemporalAdjusters.nextOrSame(day));
        while (!date.isAfter(until)) {
            dates.add(date);
            date = date.plusWeeks(1);
        }
        return dates;
    }

    /** Возвращает интервал серии в указанную дату (без учёта пропусков). */
    public TimeInterval occurrence(LocalDate date) {
        return TimeInterval.of(date, start, end);
    }

    /** Помечает дату как пропущенную (в эту дату помещение занято другой бронью). */
    public void skip(LocalDate date) {
        skipped.add(date);
    }

    /** Возвращает пропущенные даты по возрастанию. */
    public List<LocalDate> getSkippedDates() {
        return List.copyOf(skipped);
    }

    /** Возвращает текстовый отчёт: сколько дат забронировано и какие пропущены. */
    public String report() {
        int total = getDates().size();
        return "Забронировано " + (total - skipped.size()) + " из " + total + " дат, пропущены: " + skipped;
    }

    @Override
    public Optional<TimeInterval> intervalOn(LocalDate date) {
        boolean inSeries = date.getDayOfWeek() == day && !date.isBefore(from) && !date.isAfter(until);
        if (inSeries && !skipped.contains(date)) {
            return Optional.of(occurrence(date));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "№" + getId() + " «" + getTitle() + "», " + getRoom().getId() + ", каждый " + day + " "
                + start + "-" + end + " с " + from + " по " + until + ", " + getEmployee();
    }
}
