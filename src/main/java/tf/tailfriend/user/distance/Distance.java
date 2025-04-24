package tf.tailfriend.user.distance;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tf.tailfriend.user.exception.DistanceCodeException;

@Slf4j
@Getter
public enum Distance {

    ONE("1", 1),
    TWO("2", 3),
    THREE("3", 5),
    FOUR("4", 10);

    private final String code;
    private final int value;

    Distance(String code, int value) {
        this.code = code;
        this.value = value;
    }

    @JsonValue
    public String getCode() {
        return code;
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
