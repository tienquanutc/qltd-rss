package pebble

import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate

class HumanNumDownloadsFilter implements Filter {
    @Override
    Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (!input) return ""
        long number = (long) input
        if (number == 0) return ""
        int unit = 1000
        if (number < unit) return String.format("%d+", number)
        int exp = (int) (Math.log(number) / Math.log(unit))
        String postfix = "KMB".charAt(exp - 1).toString()
        return String.format("%.0f%s+", number / Math.pow(unit, exp), postfix)
    }

    @Override
    List<String> getArgumentNames() {
        return null
    }
}
