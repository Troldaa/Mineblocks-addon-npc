package cz.miniomega.mobs.util.range;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StaticNumber implements NumberRange {

    private final int value;

    @Override
    public boolean test(Integer integer) {
        return integer == value;
    }

    @Override
    public int getMin() {
        return value;
    }

    @Override
    public int getMax() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
