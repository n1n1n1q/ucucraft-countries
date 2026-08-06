package com.ucucraft.countries.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class NameArgs {

    private NameArgs() {
    }

    /** Joins args[from..) into a single whitespace-normalized name. */
    public static String join(String[] args, int from) {
        return join(args, from, args.length);
    }

    /** Joins args[from..to) into a single whitespace-normalized name; quotes are optional. */
    public static String join(String[] args, int from, int to) {
        StringBuilder builder = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return unquote(builder.toString().trim().replaceAll("\\s+", " "));
    }

    private static String unquote(String name) {
        if (name.length() >= 2 && name.startsWith("\"") && name.endsWith("\"")) {
            return name.substring(1, name.length() - 1).trim();
        }
        return name;
    }

    /**
     * Suggestions for a name spanning several arguments: the next word of every name whose
     * earlier words are already typed. Without it, two-word names cannot be tab completed.
     */
    public static List<String> completeName(Collection<String> names, String[] args, int from) {
        int typed = args.length - from;
        if (typed <= 0) {
            return List.of();
        }
        String partial = args[args.length - 1].toLowerCase();
        List<String> suggestions = new ArrayList<>();
        for (String name : names) {
            String[] words = name.split(" ");
            if (words.length < typed || !words[typed - 1].toLowerCase().startsWith(partial)) {
                continue;
            }
            boolean matches = true;
            for (int i = 0; i < typed - 1; i++) {
                if (!words[i].equalsIgnoreCase(args[from + i])) {
                    matches = false;
                    break;
                }
            }
            if (matches && !suggestions.contains(words[typed - 1])) {
                suggestions.add(words[typed - 1]);
            }
        }
        return suggestions;
    }
}
