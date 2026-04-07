package com.translatr.dto;

import java.time.LocalDate;

/**
 * One "bar" in the activity graph: the count of log_entry rows for a given date.
 * Matches the TypeScript {@code Aggregate} interface consumed by the Angular UI.
 */
public class AggregateDto {

    /** Calendar date of the aggregated bucket (yyyy-MM-dd). */
    public LocalDate date;

    /** Epoch milliseconds corresponding to {@link #date} — used by the UI chart. */
    public long millis;

    /** Optional key / label (currently unused for the daily aggregation). */
    public String key;

    /** Number of log entries on that day. */
    public int value;
}

