package booking.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Бронь помещения. Базовый класс: разные виды броней (разовая, повторяющаяся)
 * по-своему отвечают, какой интервал они занимают в конкретный день.
 */
public abstract class Booking {
    private final int id;
    private final Room room;
    private final Employee employee;
    private final String title;

    /** Создаёт бронь; вызывается из конструкторов наследников. */
    protected Booking(int id, Room room, Employee employee, String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Не указано назначение брони");
        }
        this.id = id;
        this.room = Objects.requireNonNull(room);
        this.employee = Objects.requireNonNull(employee);
        this.title = title;
    }

    /** Возвращает номер брони. */
    public int getId() {
        return id;
    }

    /** Возвращает помещение. */
    public Room getRoom() {
        return room;
    }

    /** Возвращает сотрудника, оформившего бронь. */
    public Employee getEmployee() {
        return employee;
    }

    /** Возвращает назначение брони. */
    public String getTitle() {
        return title;
    }

    /**
     * Возвращает интервал, который бронь занимает в указанный день.
     *
     * @param date день
     * @return интервал или пустой Optional, если в этот день бронь не действует
     */
    public abstract Optional<TimeInterval> intervalOn(LocalDate date);
}
