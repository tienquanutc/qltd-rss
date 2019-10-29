package pebble


import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import org.apache.commons.lang3.time.DateUtils

class DateEqualFunction implements Function {

    @Override
    List<String> getArgumentNames() {
        return null
    }


    @Override
    Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
        Date left = safeGetDate(args.getOrDefault("0", null))
        Date right = safeGetDate(args.getOrDefault("1", null))
        if (!left || !right) return false

        return DateUtils.isSameDay(left, right)
    }

    Date safeGetDate(Object input) {
        if (input == null) return null
        if (input instanceof Date) return input
        if (input instanceof Long) return new Date(input)
        return null
    }
}
