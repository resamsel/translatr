package com.translatr.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.translatr.model.LogEntry;

/**
 * Utilities for rendering activity log entries (ported from the Play-era ActivityUtils).
 *
 * <p>The {@code contentType} stored on {@link LogEntry} is the fully-qualified class name of the
 * DTO (e.g. {@code com.translatr.dto.UserDto}). {@link #contentTypeOf} strips the package prefix
 * and the trailing {@code Dto} suffix so the switch statements can work with simple type names
 * ({@code "User"}, {@code "Project"}, …).</p>
 */
public final class ActivityUtils {

    // Icons
    public static final String PROJECT_ICON       = "view_quilt";
    public static final String USER_ICON          = "account_circle";
    public static final String LOCALE_ICON        = "language";
    public static final String KEY_ICON           = "vpn_key";
    public static final String MESSAGE_ICON       = "message";
    public static final String ACCESS_TOKEN_ICON  = "vpn_key";
    public static final String PROJECT_USER_ICON  = "group";

    // Colors
    public static final String USER_COLOR         = "teal";
    public static final String PROJECT_COLOR      = "orange";
    public static final String LOCALE_COLOR       = "blue";
    public static final String KEY_COLOR          = "light-green";
    public static final String MESSAGE_COLOR      = "red";
    public static final String ACCESS_TOKEN_COLOR = "red";
    public static final String PROJECT_USER_COLOR = "purple";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ActivityUtils() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public static String iconOf(LogEntry activity) {
        if (activity == null) return null;
        return switch (contentTypeOf(activity)) {
            case "Project"     -> PROJECT_ICON;
            case "Locale"      -> LOCALE_ICON;
            case "Key"         -> KEY_ICON;
            case "AccessToken" -> ACCESS_TOKEN_ICON;
            case "Message"     -> MESSAGE_ICON;
            case "User"        -> USER_ICON;
            case "Member"      -> PROJECT_USER_ICON;
            default            -> "";
        };
    }

    public static String colorOf(LogEntry activity) {
        if (activity == null) return null;
        return switch (contentTypeOf(activity)) {
            case "Project"     -> PROJECT_COLOR;
            case "Locale"      -> LOCALE_COLOR;
            case "Key"         -> KEY_COLOR;
            case "Message"     -> MESSAGE_COLOR;
            case "User"        -> USER_COLOR;
            case "AccessToken" -> ACCESS_TOKEN_COLOR;
            case "Member"      -> PROJECT_USER_COLOR;
            default            -> "";
        };
    }

    public static String nameOf(LogEntry activity) {
        if (activity == null) return null;
        JsonNode node = parse(activity);
        return switch (contentTypeOf(activity)) {
            case "User", "Project", "Locale", "Key", "AccessToken" ->
                    getAsText(node, "name");
            case "Message" ->
                    String.format("%s (%s)", getAsText(node, "keyName"), getAsText(node, "localeName"));
            case "Member" ->
                    String.format("%s (%s)", getAsText(node, "projectName"), getAsText(node, "userName"));
            default -> "";
        };
    }

    public static JsonNode parse(LogEntry activity) {
        String raw = switch (activity.type) {
            case Create, Update, Login, Logout -> activity.after;
            case Delete                        -> activity.before;
        };
        if (raw != null) {
            try { return MAPPER.readTree(raw); } catch (Exception ignored) {}
        }
        return MAPPER.createObjectNode();
    }

    // -------------------------------------------------------------------------
    // Package-private helpers
    // -------------------------------------------------------------------------

    /** Extracts the simple entity name from a fully-qualified DTO class name. */
    static String contentTypeOf(LogEntry activity) {
        String ct = activity.contentType;
        if (ct == null) return "";
        // Strip package prefix
        String simple = ct.substring(ct.lastIndexOf('.') + 1);
        // Strip trailing "Dto" suffix (e.g. "UserDto" → "User")
        if (simple.endsWith("Dto")) {
            simple = simple.substring(0, simple.length() - 3);
        }
        return simple;
    }

    private static String getAsText(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) return null;
        return node.get(field).asText();
    }
}

