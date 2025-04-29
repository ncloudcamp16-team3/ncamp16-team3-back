package tf.tailfriend.user.distance;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum Distance {

    LEVEL1(1),
    LEVEL2(3),
    LEVEL3(5),
    LEVEL4(10);

    private final int value;

    Distance(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
