package booking.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoomTest {

    @Test
    void equalsAndHashCodeUseOnlyId() {
        Room a = new Room("A-1", "Первая", RoomType.MEETING, 6, List.of());
        Room b = new Room("A-1", "Другое название", RoomType.LECTURE, 50, List.of("проектор"));
        Room c = new Room("A-1", "Третья", RoomType.MEETING, 8, List.of());
        Room other = new Room("A-2", "Первая", RoomType.MEETING, 6, List.of());

        assertEquals(a, a);                           // рефлексивность
        assertEquals(a, b);
        assertEquals(b, a);                           // симметричность
        assertEquals(b, c);
        assertEquals(a, c);                           // транзитивность
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, other);
        assertNotEquals(a, null);
        assertNotEquals(a, "A-1");
    }

    @Test
    void equipmentListIsCopied() {
        List<String> source = new ArrayList<>(List.of("проектор"));
        Room room = new Room("A-1", "Первая", RoomType.MEETING, 6, source);
        source.add("доска");
        assertEquals(List.of("проектор"), room.getEquipment());
        assertThrows(UnsupportedOperationException.class, () -> room.getEquipment().add("колонки"));
    }

    @Test
    void invalidCapacityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Room("A-1", "Первая", RoomType.MEETING, 0, List.of()));
    }
}
