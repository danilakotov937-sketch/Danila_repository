package booking.model;

/**
 * Сотрудник, который бронирует помещения.
 *
 * @param id   табельный номер
 * @param name фамилия сотрудника
 */
public record Employee(int id, String name) {

    public Employee {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя сотрудника не задано");
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
