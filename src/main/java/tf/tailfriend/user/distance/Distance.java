package tf.tailfriend.user.distance;

import lombok.Getter;
import tf.tailfriend.user.exception.DistanceCodeException;

@Getter
public enum Distance {

    ONE("1", 1),
    TWO("2", 3),
    THREE("3", 5),
    FOUR("4", 10);

    private final String code;
    private final int distanceValue;

    Distance(String code, int distanceValue) {
        this.code = code;
        this.distanceValue = distanceValue;
    }

    public static Distance fromCode(String dbCode) {
        for (Distance d : values()) {
            if (d.code.equals(dbCode)) {
                return d;
            }
        }
        throw new DistanceCodeException();
    }
}
