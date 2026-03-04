package cz.miniomega.mobs.util.range;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class MultiNumberRange implements NumberRange {

    private final List<NumberRange> ranges;

    @Override
    public boolean test(Integer integer) {
        return ranges.stream().anyMatch(r -> r.test(integer));
    }

    @Override
    public int getMin() {
        return ranges.stream().mapToInt(NumberRange::getMin).min().orElse(0);
    }

    @Override
    public int getMax() {
        return ranges.stream().mapToInt(NumberRange::getMax).max().orElse(0);
    }

    @Override
    public String toString() {
        return ranges.stream().map(Object::toString).collect(Collectors.joining(","));
    }

}
