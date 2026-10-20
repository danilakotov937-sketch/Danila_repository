package booking.model;

import java.time.LocalDate;
import java.util.Optional;

/** Разовая бронь на один интервал времени. */
public class OneTimeBooking extends Booking {
    private final TimeInterval interval;

    /** Создаёт разовую бронь. */
    public OneTimeBooking(int id, Room room, Employee employee, String title, TimeInterval interval) {
        super(id, room, employee, title);
        this.interval = interval;
    }

    /** Возвращает время брони. */
    public TimeInterval getInterval() {
        return interval;
    }

    @Override
    public Optional<TimeInterval> intervalOn(LocalDate date) {
        if (interval.date().equals(date)) {
            return Optional.of(interval);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "№" + getId() + " «" + getTitle() + "», " + getRoom().getId() + ", " + interval
                + ", " + getEmployee();
    }
}
