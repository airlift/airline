package io.airlift.airline;

import com.google.common.collect.ImmutableList;

import java.util.LinkedList;
import java.util.List;

public class ParseResult {
    private List<ParseException> errors = new LinkedList<ParseException>();

    public void addError(ParseException error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return errors.size() != 0;
    }

    public List<ParseException> getErrors() {
        return ImmutableList.copyOf(errors);
    }

    public String getErrorMessage() {
        if (errors.size() >= 1) {
            StringBuffer sb = new StringBuffer();
            sb.append("ERROR: Encountered problems parsing arguments:\n");
            for (ParseException error : errors) {
                sb.append("       - ").append(error.getMessage()).append("\n");
            }
            sb.append("\n");
            return sb.toString();
        } else {
            throw new IllegalStateException("There are no errors to build a message for.");
        }
    }
}
