package cz.miniomega.mobs.util.range;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RangeNumber implements NumberRange {

    private final int min;
    private final int max;

    @Override
    public boolean test(Integer integer) {
        return integer >= min && integer <= max;
    }

    @Override
    public int getMin() {
        return min;
    }

    @Override
    public int getMax() {
        return max;
    }

    @Override
    public String toString() {
        return min + "-" + max;
    }

}
