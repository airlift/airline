package org.iq80.cli;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;

public class UsagePrinter
{
    private final StringBuilder out;
    private final int maxSize;
    private final int indent;
    private final AtomicInteger currentPosition;

    public UsagePrinter(StringBuilder out)
    {
        this(out, 79);
    }

    public UsagePrinter(StringBuilder out, int maxSize)
    {
        this(out, maxSize, 0, new AtomicInteger());
    }

    UsagePrinter(StringBuilder out, int maxSize, int indent, AtomicInteger currentPosition)
    {
        this.out = out;
        this.maxSize = maxSize;
        this.indent = indent;
        this.currentPosition = currentPosition;
    }

    public UsagePrinter newIndentedPrinter(int size)
    {
        return new UsagePrinter(out, maxSize, indent + size, currentPosition);
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
            out.append(spaces(indent)).append(line.toString().trim()).append("\n");
        }

        return this;
    }

    public UsagePrinter append(String value)
    {
        return appendWords(Splitter.onPattern("\\s+").omitEmptyStrings().trimResults().split(String.valueOf(value)));
    }

    public UsagePrinter appendWords(Iterable<String> words)
    {
        for (String word : words) {
            if (currentPosition.get() == 0) {
                // beginning of line
                out.append(spaces(indent));
                currentPosition.getAndAdd((indent));
            }
            else if (word.length() > maxSize || currentPosition.get() + word.length() <= maxSize) {
                // between words
                out.append(" ");
                currentPosition.getAndIncrement();
            }
            else {
                // wrap line
                out.append("\n").append(spaces(indent));
                currentPosition.set(indent);
            }

            out.append(word);
            currentPosition.getAndAdd((word.length()));
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
