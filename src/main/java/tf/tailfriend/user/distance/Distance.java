package tf.tailfriend.user.distance;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum Distance {

    LEVEL1(5),
    LEVEL2(10),
    LEVEL3(20),
    LEVEL4(40);

    private final int value;

    Distance(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
