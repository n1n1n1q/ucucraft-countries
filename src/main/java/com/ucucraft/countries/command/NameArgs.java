package com.ucucraft.countries.command;

public final class NameArgs {

    private NameArgs() {
    }

    /** Joins args[from..) into a single whitespace-normalized name. */
    public static String join(String[] args, int from) {
        return join(args, from, args.length);
    }

    /** Joins args[from..to) into a single whitespace-normalized name. */
    public static String join(String[] args, int from, int to) {
        StringBuilder builder = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString().trim().replaceAll("\\s+", " ");
    }
}
