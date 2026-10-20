package booking.service;

import booking.model.Booking;
import booking.model.Employee;
import booking.model.OneTimeBooking;
import booking.model.Room;
import booking.model.TimeInterval;
import booking.model.WeeklyBooking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Система бронирования: хранит помещения и брони и следит,
 * чтобы брони одного помещения не пересекались.
 */
public class BookingSystem {
    /** Начало рабочего дня. */
    public static final LocalTime DAY_START = LocalTime.of(8, 0);
    /** Конец рабочего дня. */
    public static final LocalTime DAY_END = LocalTime.of(22, 0);

    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private int nextId = 1;

    /** Добавляет помещение в систему. */
    public void addRoom(Room room) {
        if (rooms.contains(room)) {
            throw new IllegalArgumentException("Помещение " + room.getId() + " уже есть");
        }
        rooms.add(room);
    }

    /** Возвращает копию списка помещений. */
    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

    /** Возвращает копию списка броней. */
    public List<Booking> getBookings() {
        return List.copyOf(bookings);
    }

    /**
     * Бронирует помещение на интервал.
     *
     * @throws BookingConflictException если помещение занято в это время
     * @throws IllegalArgumentException если интервал выходит за рабочее время
     */
    public OneTimeBooking book(Room room, Employee employee, TimeInterval interval, String title)
            throws BookingConflictException {
        checkWorkingHours(interval);
        Booking conflict = findConflict(room, interval, null);
        if (conflict != null) {
            throw new BookingConflictException(room, interval, conflict);
        }
        OneTimeBooking booking = new OneTimeBooking(nextId++, room, employee, title, interval);
        bookings.add(booking);
        return booking;
    }

    /**
     * Отменяет бронь по номеру.
     *
     * @throws IllegalArgumentException если брони с таким номером нет
     */
    public void cancel(int id) {
        bookings.remove(findById(id));
    }

    /** Возвращает свободные окна помещения в указанный день (в рабочее время). */
    public List<TimeInterval> getFreeWindows(Room room, LocalDate date) {
        List<TimeInterval> busy = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getRoom().equals(room)) {
                b.intervalOn(date).ifPresent(busy::add);
            }
        }
        busy.sort(Comparator.comparing(TimeInterval::start));

        List<TimeInterval> free = new ArrayList<>();
        LocalDateTime cursor = date.atTime(DAY_START);
        for (TimeInterval t : busy) {
            if (t.start().isAfter(cursor)) {
                free.add(new TimeInterval(cursor, t.start()));
            }
            cursor = t.end();
        }
        LocalDateTime close = date.atTime(DAY_END);
        if (cursor.isBefore(close)) {
            free.add(new TimeInterval(cursor, close));
        }
        return free;
    }

    /** Подбирает свободные на это время помещения с вместимостью не меньше заданной (сначала самые маленькие). */
    public List<Room> findFreeRooms(int capacity, TimeInterval interval) {
        checkWorkingHours(interval);
        List<Room> result = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getCapacity() >= capacity && findConflict(room, interval, null) == null) {
                result.add(room);
            }
        }
        result.sort(Comparator.comparingInt(Room::getCapacity));
        return result;
    }

    /**
     * Бронирует помещение каждую неделю в один и тот же день и время.
     * Даты, на которые помещение уже занято, пропускаются; список пропусков есть в отчёте серии.
     *
     * @param from  с какой даты искать нужный день недели
     * @param until последняя дата серии (включительно)
     */
    public WeeklyBooking bookWeekly(Room room, Employee employee, String title, DayOfWeek day,
                                    LocalTime start, LocalTime end, LocalDate from, LocalDate until) {
        WeeklyBooking series = new WeeklyBooking(nextId++, room, employee, title, day, start, end, from, until);
        if (series.getDates().isEmpty()) {
            throw new IllegalArgumentException("В этом периоде нет подходящих дней");
        }
        for (LocalDate date : series.getDates()) {
            TimeInterval interval = series.occurrence(date);
            checkWorkingHours(interval);
            if (findConflict(room, interval, null) != null) {
                series.skip(date);
            }
        }
        bookings.add(series);
        return series;
    }

    /**
     * Переносит разовую бронь в другое помещение и/или на другое время.
     * Если в новом месте занято, бронь остаётся на прежнем месте.
     *
     * @throws BookingConflictException если в новом месте/времени занято
     * @throws IllegalArgumentException если брони нет или это повторяющаяся серия
     */
    public OneTimeBooking move(int id, Room newRoom, TimeInterval newInterval) throws BookingConflictException {
        Booking old = findById(id);
        if (!(old instanceof OneTimeBooking)) {
            throw new IllegalArgumentException("Переносить можно только разовые брони");
        }
        checkWorkingHours(newInterval);
        Booking conflict = findConflict(newRoom, newInterval, old); // саму переносимую бронь не считаем
        if (conflict != null) {
            throw new BookingConflictException(newRoom, newInterval, conflict);
        }
        OneTimeBooking moved = new OneTimeBooking(id, newRoom, old.getEmployee(), old.getTitle(), newInterval);
        bookings.set(bookings.indexOf(old), moved);
        return moved;
    }

    private Booking findById(int id) {
        for (Booking b : bookings) {
            if (b.getId() == id) {
                return b;
            }
        }
        throw new IllegalArgumentException("Бронь №" + id + " не найдена");
    }

    /** Ищет бронь этого помещения, пересекающуюся с интервалом (ignored не учитывается). */
    private Booking findConflict(Room room, TimeInterval interval, Booking ignored) {
        for (Booking b : bookings) {
            if (b == ignored || !b.getRoom().equals(room)) {
                continue;
            }
            Optional<TimeInterval> busy = b.intervalOn(interval.date());
            if (busy.isPresent() && busy.get().overlaps(interval)) {
                return b;
            }
        }
        return null;
    }

    private void checkWorkingHours(TimeInterval interval) {
        boolean oneDay = interval.start().toLocalDate().equals(interval.end().toLocalDate());
        if (!oneDay || interval.start().toLocalTime().isBefore(DAY_START)
                || interval.end().toLocalTime().isAfter(DAY_END)) {
            throw new IllegalArgumentException("Бронь должна быть в пределах одного дня с "
                    + DAY_START + " до " + DAY_END);
        }
    }
}
