package booking.service;

import booking.model.Booking;
import booking.model.Room;
import booking.model.TimeInterval;

/** Помещение уже занято на запрошенное время.Исключение проверяемое. */
public class BookingConflictException extends Exception {
    private final Booking conflicting;

    /** Создаёт исключение с понятным сообщением. */
    public BookingConflictException(Room room, TimeInterval requested, Booking conflicting) {
        super("Помещение " + room.getId() + " занято: запрошено " + requested
                + ", пересекается с бронью " + conflicting);
        this.conflicting = conflicting;
    }

    /** Возвращает бронь, с которой произошло пересечение. */
    public Booking getConflicting() {
        return conflicting;
    }
}
