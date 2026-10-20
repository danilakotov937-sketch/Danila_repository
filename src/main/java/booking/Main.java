package booking;

import booking.model.Employee;
import booking.model.OneTimeBooking;
import booking.model.Room;
import booking.model.RoomType;
import booking.model.TimeInterval;
import booking.model.WeeklyBooking;
import booking.service.BookingConflictException;
import booking.service.BookingSystem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Консольная демонстрация работы системы бронирования. */
public class Main {
    private static final LocalDate MON = LocalDate.of(2026, 10, 19); // понедельник

    private static TimeInterval at(LocalDate day, String from, String to) {
        return TimeInterval.of(day, LocalTime.parse(from), LocalTime.parse(to));
    }

    /**
     * Точка входа.
     *
     * @param args не используются
     * @throws BookingConflictException если сценарий демонстрации нарушен (в нём конфликты обрабатываются отдельно)
     */
    public static void main(String[] args) throws BookingConflictException {
        BookingSystem system = new BookingSystem();

        Room alpha = new Room("A-101", "Альфа", RoomType.MEETING, 6, List.of("проектор", "доска"));
        Room beta = new Room("A-102", "Бета", RoomType.MEETING, 10, List.of("проектор", "видеосвязь"));
        Room lect1 = new Room("B-201", "Аудитория 201", RoomType.LECTURE, 60, List.of("проектор"));
        Room lect2 = new Room("B-202", "Аудитория 202", RoomType.LECTURE, 120, List.of("проектор", "микрофон"));
        Room lab = new Room("C-301", "Компьютерный класс", RoomType.COMPUTER_LAB, 24, List.of("компьютеры"));
        Room hall = new Room("D-401", "Большой зал", RoomType.CONFERENCE, 200, List.of("проектор", "микрофон"));
        for (Room room : List.of(alpha, beta, lect1, lect2, lab, hall)) {
            system.addRoom(room);
        }

        Employee ivanov = new Employee(1, "Иванов");
        Employee petrova = new Employee(2, "Петрова");
        Employee sidorov = new Employee(3, "Сидоров");
        Employee kuznetsova = new Employee(4, "Кузнецова");
        Employee smirnov = new Employee(5, "Смирнов");
        Employee popova = new Employee(6, "Попова");
        List<Employee> staff = List.of(ivanov, petrova, sidorov, kuznetsova, smirnov, popova);

        System.out.println("=== 1. Помещения ===");
        for (Room room : system.getRooms()) {
            System.out.println(room + ", оснащение: " + room.getEquipment());
        }

        System.out.println("\n=== 2. Бронирование ===");
        OneTimeBooking planning = system.book(alpha, ivanov, at(MON, "10:00", "11:00"), "Планёрка");
        OneTimeBooking interview = system.book(alpha, petrova, at(MON, "13:00", "14:30"), "Собеседование");
        OneTimeBooking demoBooking = system.book(beta, sidorov, at(MON, "09:00", "10:30"), "Демо для заказчика");
        system.book(lab, smirnov, at(MON, "14:00", "16:00"), "Практикум по Java");
        system.book(hall, kuznetsova, at(MON, "09:00", "13:00"), "Конференция");
        system.book(beta, petrova, at(MON.plusDays(1), "12:00", "13:00"), "Обед с клиентом");
        system.book(lab, smirnov, at(MON.plusDays(1), "10:00", "12:00"), "Практикум по SQL");
        system.book(hall, kuznetsova, at(MON.plusDays(2), "10:00", "12:00"), "Приём делегации");
        for (int i = 0; i < 5; i++) { // лекции на всю неделю
            LocalDate day = MON.plusDays(i);
            system.book(lect1, popova, at(day, "09:00", "10:30"), "Лекция по ООП");
            system.book(lect2, popova, at(day, "11:00", "12:30"), "Лекция по базам данных");
        }
        System.out.println("Создано броней: " + system.getBookings().size());
        System.out.println(planning);

        System.out.println("\n=== 3. Конфликт броней ===");
        TimeInterval wanted = at(MON, "10:30", "11:30");
        try {
            system.book(alpha, smirnov, wanted, "Консультация");
        } catch (BookingConflictException e) {
            System.out.println("Отказ: " + e.getMessage());
            List<Room> free = system.findFreeRooms(4, wanted);
            if (!free.isEmpty()) {
                system.book(free.get(0), smirnov, wanted, "Консультация");
                System.out.println("Забронировано в другом помещении: " + free.get(0).getId());
            }
        }

        System.out.println("\n=== 4. Отмена брони ===");
        system.cancel(interview.getId());
        System.out.println("Бронь №" + interview.getId() + " отменена");
        system.book(alpha, sidorov, interview.getInterval(), "Созвон команды");
        System.out.println("Освободившееся время занято заново");
        try {
            system.cancel(999);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\n=== 5. Свободные окна " + alpha.getId() + " в понедельник ===");
        for (TimeInterval window : system.getFreeWindows(alpha, MON)) {
            System.out.println(window);
        }

        System.out.println("\n=== 6. Подбор помещения ===");
        System.out.println("На 20 человек, 14:00-15:00: " + system.findFreeRooms(20, at(MON, "14:00", "15:00")));
        System.out.println("На 500 человек: " + system.findFreeRooms(500, at(MON, "14:00", "15:00")));

        System.out.println("\n=== 7. Повторяющаяся бронь ===");
        system.book(beta, kuznetsova, at(LocalDate.of(2026, 11, 3), "14:00", "16:00"), "Тренинг");
        system.book(beta, kuznetsova, at(LocalDate.of(2026, 12, 1), "14:00", "15:00"), "Совещание");
        WeeklyBooking seminar = system.bookWeekly(beta, sidorov, "Семинар", DayOfWeek.TUESDAY,
                LocalTime.of(14, 0), LocalTime.of(15, 30), MON.plusDays(1), LocalDate.of(2026, 12, 22));
        System.out.println(seminar);
        System.out.println(seminar.report());

        int total = system.getRooms().size() + staff.size() + system.getBookings().size();
        System.out.println("\nВсего объектов в модели: " + total + " (помещений " + system.getRooms().size()
                + ", сотрудников " + staff.size() + ", броней " + system.getBookings().size() + ")");
    }
}
