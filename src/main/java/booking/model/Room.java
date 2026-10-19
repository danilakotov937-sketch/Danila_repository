package booking.model;

import java.util.List;

/**
 * Помещение, которое можно бронировать.
 * Два помещения считаются одним и тем же, если совпадает код (id).
 */
public class Room {
    private final String id;
    private final String name;
    private final RoomType type;
    private final int capacity;
    private final List<String> equipment;

    /**
     * Создаёт помещение.
     *
     * @param id        уникальный код, например "A-101"
     * @param name      название
     * @param type      тип помещения
     * @param capacity  вместимость, больше нуля
     * @param equipment оснащение (список копируется)
     */
    public Room(String id, String name, RoomType type, int capacity, List<String> equipment) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Код помещения не задан");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Вместимость должна быть больше нуля");
        }
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.equipment = List.copyOf(equipment); // копия, чтобы снаружи список нельзя было изменить
    }

    /** Возвращает код помещения. */
    public String getId() {
        return id;
    }

    /** Возвращает название помещения. */
    public String getName() {
        return name;
    }

    /** Возвращает тип помещения. */
    public RoomType getType() {
        return type;
    }

    /** Возвращает вместимость. */
    public int getCapacity() {
        return capacity;
    }

    /** Возвращает оснащение помещения (список неизменяемый). */
    public List<String> getEquipment() {
        return equipment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Room other = (Room) o;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id + " «" + name + "» (" + type.getTitle() + ", мест: " + capacity + ")";
    }
}
