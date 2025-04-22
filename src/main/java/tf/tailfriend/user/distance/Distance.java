package tf.tailfriend.user.distance;

import lombok.Getter;

@Getter
public enum Distance {
    ONE("1"), TWO("2"), THREE("3"), FOUR("4");

    private final String value;

    Distance(String value) {
        this.value = value;
    }

    public static Distance fromString(String value) {
        for (Distance distance : Distance.values()) {
            if (distance.getValue().equals(value)) {
                return distance;
            }
        }
        throw new IllegalArgumentException("Invalid Distance value: " + value);
    }
}