package io.airlift.command;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;

public class UsagePrinter
{
    private final StringBuilder out;
    private final int maxSize;
    private final int indent;
    private final int hangingIndent;
    private final AtomicInteger currentPosition;

    public UsagePrinter(StringBuilder out)
    {
        this(out, 79);
    }

    public UsagePrinter(StringBuilder out, int maxSize)
    {
        this(out, maxSize, 0, 0, new AtomicInteger());
    }

    public UsagePrinter(StringBuilder out, int maxSize, int indent, int hangingIndent, AtomicInteger currentPosition)
    {
        this.out = out;
        this.maxSize = maxSize;
        this.indent = indent;
        this.hangingIndent = hangingIndent;
        this.currentPosition = currentPosition;
    }

    public UsagePrinter newIndentedPrinter(int size)
    {
        return new UsagePrinter(out, maxSize, indent + size, hangingIndent, currentPosition);
    }

    public UsagePrinter newPrinterWithHangingIndent(int size)
    {
        return new UsagePrinter(out, maxSize, indent, hangingIndent + size, currentPosition);
    }

    public UsagePrinter newline()
    {
        out.append("\n");
        currentPosition.set(0);
        return this;
    }

    public UsagePrinter appendTable(Iterable<? extends Iterable<String>> table)
    {
        List<Integer> columnSizes = newArrayList();
        for (Iterable<String> row : table) {
            int column = 0;
            for (String value : row) {
                while (column >= columnSizes.size()) {
                    columnSizes.add(0);
                }
                columnSizes.set(column, Math.max(value.length(), columnSizes.get(column)));
                column++;
            }
        }

        if (currentPosition.get() != 0) {
            currentPosition.set(0);
            out.append("\n");
        }

        for (Iterable<String> row : table) {
            int column = 0;
            StringBuilder line = new StringBuilder();
            for (String value : row) {
                int columnSize = columnSizes.get(column);
                line.append(value);
                line.append(spaces(columnSize - value.length()));
                line.append("   ");
                column++;
            }
            out.append(spaces(indent)).append(trimEnd(line.toString())).append("\n");
        }

        return this;
    }

    public static String trimEnd(final String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }

        int end = str.length();
        while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
            end--;
        }

        return str.substring(0, end);
    }

    public UsagePrinter append(String value) {
        return append(value, false);
    }

    public UsagePrinter appendOnOneLine(String value) {
        return append(value, true);
    }

    public UsagePrinter appendWords(Iterable<String> words) {
        return appendWords(words, false);
    }

    public UsagePrinter append(String value, boolean avoidNewlines)
    {
        if (value == null) {
            return this;
        }
        return appendWords(Splitter.onPattern("\\s+").omitEmptyStrings().trimResults().split(String.valueOf(value)), avoidNewlines);
    }

    public UsagePrinter appendWords(Iterable<String> words, boolean avoidNewlines)
    {
        int bracketCount = 0;
        for (String word : words) {
            if(null == word || "".equals(word))
            {
                continue;
            }
            if (currentPosition.get() == 0) {
                // beginning of line
                out.append(spaces(indent));
                currentPosition.getAndAdd((indent));
            }
            else if (word.length() > maxSize || currentPosition.get() + word.length() <= maxSize || bracketCount > 0 || avoidNewlines) {
                // between words
                out.append(" ");
                currentPosition.getAndIncrement();
            }
            else {
                // wrap line
                out.append("\n").append(spaces(indent)).append(spaces(hangingIndent));
                currentPosition.set(indent);
            }

            out.append(word);
            currentPosition.getAndAdd((word.length()));
            if (word.contains("{") || word.contains("[") || word.contains("<")) {
                bracketCount++;
            }
            if (word.contains("}") || word.contains("]") || word.contains(">")) {
                bracketCount--;
            }
        }
        return this;
    }

    private static String spaces(int count)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(" ");
        }
        return result.toString();
    }
}
