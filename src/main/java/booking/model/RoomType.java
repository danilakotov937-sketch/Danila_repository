package booking.model;

/** Тип помещения. */
public enum RoomType {
    MEETING("Переговорная"),
    LECTURE("Лекционная аудитория"),
    COMPUTER_LAB("Компьютерный класс"),
    CONFERENCE("Конференц-зал");

    private final String title;

    RoomType(String title) {
        this.title = title;
    }

    /** Возвращает название типа для вывода на экран. */
    public String getTitle() {
        return title;
    }
}
