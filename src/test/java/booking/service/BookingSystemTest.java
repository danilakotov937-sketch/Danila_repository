package booking.service;

import booking.model.Employee;
import booking.model.OneTimeBooking;
import booking.model.Room;
import booking.model.RoomType;
import booking.model.TimeInterval;
import booking.model.WeeklyBooking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingSystemTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 19); // понедельник

    private BookingSystem system;
    private Room small;
    private Room big;
    private Employee ivanov;
    private Employee petrova;

    @BeforeEach
    void setUp() {
        system = new BookingSystem();
        small = new Room("A-1", "Малая", RoomType.MEETING, 6, List.of());
        big = new Room("B-1", "Большая", RoomType.LECTURE, 50, List.of("проектор"));
        system.addRoom(small);
        system.addRoom(big);
        ivanov = new Employee(1, "Иванов");
        petrova = new Employee(2, "Петрова");
    }

    private TimeInterval at(String from, String to) {
        return TimeInterval.of(DAY, LocalTime.parse(from), LocalTime.parse(to));
    }

    @Test
    void bookSavesBooking() throws BookingConflictException {
        OneTimeBooking b = system.book(small, ivanov, at("10:00", "11:00"), "Планёрка");
        assertEquals(1, system.getBookings().size());
        assertEquals(small, b.getRoom());
    }

    @Test
    void overlappingBookingThrowsConflict() throws BookingConflictException {
        OneTimeBooking first = system.book(small, ivanov, at("10:00", "11:00"), "Планёрка");
        BookingConflictException e = assertThrows(BookingConflictException.class,
                () -> system.book(small, petrova, at("10:30", "11:30"), "Встреча"));
        assertEquals(first, e.getConflicting());
        assertEquals(1, system.getBookings().size());
    }

    @Test
    void adjacentBookingsAndOtherRoomAreAllowed() throws BookingConflictException {
        system.book(small, ivanov, at("10:00", "11:00"), "A");
        system.book(small, petrova, at("11:00", "12:00"), "B"); // встык
        system.book(big, petrova, at("10:00", "11:00"), "C");   // другое помещение
        assertEquals(3, system.getBookings().size());
    }

    @Test
    void bookingOutsideWorkingHoursThrows() {
        assertThrows(IllegalArgumentException.class, () -> system.book(small, ivanov, at("07:00", "09:00"), "Рано"));
        assertThrows(IllegalArgumentException.class, () -> system.book(small, ivanov, at("21:00", "22:30"), "Поздно"));
    }

    @Test
    void cancelFreesTimeAndUnknownIdFails() throws BookingConflictException {
        OneTimeBooking b = system.book(small, ivanov, at("10:00", "11:00"), "Планёрка");
        system.cancel(b.getId());
        assertTrue(system.getBookings().isEmpty());
        system.book(small, petrova, at("10:00", "11:00"), "Снова"); // время снова свободно
        assertThrows(IllegalArgumentException.class, () -> system.cancel(999));
    }

    @Test
    void freeWindowsAreBetweenBookings() throws BookingConflictException {
        system.book(small, ivanov, at("10:00", "11:00"), "A");
        system.book(small, petrova, at("13:00", "14:00"), "B");
        assertEquals(List.of(at("08:00", "10:00"), at("11:00", "13:00"), at("14:00", "22:00")),
                system.getFreeWindows(small, DAY));
        assertEquals(List.of(at("08:00", "22:00")), system.getFreeWindows(big, DAY));
    }

    @Test
    void findFreeRoomsByCapacityAndTime() throws BookingConflictException {
        system.book(big, ivanov, at("10:00", "11:00"), "A");
        assertEquals(List.of(small), system.findFreeRooms(5, at("10:00", "11:00")));
        assertEquals(List.of(small, big), system.findFreeRooms(5, at("11:00", "12:00")));
        assertEquals(List.of(big), system.findFreeRooms(10, at("11:00", "12:00")));
        assertTrue(system.findFreeRooms(100, at("11:00", "12:00")).isEmpty());
    }

    @Test
    void weeklyBookingSkipsBusyDates() throws BookingConflictException {
        LocalDate busyDay = DAY.plusDays(8); // вторник 27 октября
        system.book(small, petrova, TimeInterval.of(busyDay, LocalTime.of(14, 0), LocalTime.of(15, 0)), "Занято");
        WeeklyBooking series = system.bookWeekly(small, ivanov, "Семинар", DayOfWeek.TUESDAY,
                LocalTime.of(14, 30), LocalTime.of(16, 0), DAY, DAY.plusDays(15));
        assertEquals(3, series.getDates().size()); // 20.10, 27.10, 03.11
        assertEquals(List.of(busyDay), series.getSkippedDates());
        assertTrue(series.intervalOn(busyDay).isEmpty());
        assertTrue(series.intervalOn(DAY.plusDays(1)).isPresent());
    }

    @Test
    void moveToOtherRoomAndTime() throws BookingConflictException {
        OneTimeBooking b = system.book(small, ivanov, at("10:00", "11:00"), "Планёрка");
        OneTimeBooking moved = system.move(b.getId(), big, at("12:00", "13:00"));
        assertEquals(b.getId(), moved.getId());
        assertEquals(big, moved.getRoom());
        assertEquals(1, system.getBookings().size());
        // сдвиг в той же комнате на время, пересекающееся со старым, допустим
        OneTimeBooking shifted = system.move(moved.getId(), big, at("12:30", "13:30"));
        assertEquals(at("12:30", "13:30"), shifted.getInterval());
    }

    @Test
    void moveConflictKeepsOldBooking() throws BookingConflictException {
        OneTimeBooking b = system.book(small, ivanov, at("10:00", "11:00"), "Планёрка");
        system.book(big, petrova, at("10:00", "11:00"), "Другая");
        assertThrows(BookingConflictException.class, () -> system.move(b.getId(), big, at("10:30", "11:30")));
        assertEquals(small, system.getBookings().get(0).getRoom());
    }
}
