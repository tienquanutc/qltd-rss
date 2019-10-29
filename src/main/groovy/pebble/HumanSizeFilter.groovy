package pebble

import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate

class HumanSizeFilter implements Filter {
    @Override
    Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input == null) {
            return "N/A";
        }
        return bytesToSize((long) input)
    }

    @Override
    List<String> getArgumentNames() {
        return null
    }

    String bytesToSize(long bytes, boolean si = false) {
        int unit = 1024
        if (bytes < unit) return bytes + " B"
        int exp = (int) (Math.log(bytes) / Math.log(unit))
        String pre = "KMGTPE".charAt(exp - 1)

        long bytesPerGB = 1_073_741_824
        String format = bytes >= bytesPerGB ? '%.2f %sB' : "%.0f %sB"
        return String.format(format, bytes / Math.pow(unit, exp), pre)
    }

}
