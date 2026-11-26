package com.foxnet.app.ui.qrscanner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxnet.app.utils.token.Token;
import com.foxnet.app.utils.token.ServerFromToken;
import org.brotli.dec.BrotliInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

/** QR-only, read-only preflight. The original parser and persistence path remain unchanged. */
public final class QrTokenValidator {
    public enum Status { VALID, WRONG_FORMAT, DAMAGED, EMPTY_SERVERS }

    private static final int MAX_ENCODED = 4096;
    private static final int MAX_DECODED = 256 * 1024;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);

    private QrTokenValidator() {}

    public static String normalize(String raw) {
        return raw.trim().replaceAll("\\s+", "");
    }

    public static Status validate(String raw) {
        if (raw == null) return Status.WRONG_FORMAT;
        if (raw.length() > MAX_ENCODED) return Status.DAMAGED;
        String token = raw.trim();
        String prefix;
        if (token.startsWith("fptnb:")) prefix = "fptnb:";
        else if (token.startsWith("fptn://")) prefix = "fptn://";
        else if (token.startsWith("fptn:")) prefix = "fptn:";
        else return Status.WRONG_FORMAT;
        String payload = token.substring(prefix.length()).replaceAll("\\s+", "");
        if (payload.isEmpty()) return Status.DAMAGED;

        try {
            byte[] bytes = Base64.getDecoder().decode(payload);
            if (prefix.equals("fptnb:")) {
                try (InputStream in = new BrotliInputStream(new ByteArrayInputStream(bytes));
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        if (out.size() + count > MAX_DECODED) return Status.DAMAGED;
                        out.write(buffer, 0, count);
                    }
                    bytes = out.toByteArray();
                }
            }
            if (bytes.length > MAX_DECODED) return Status.DAMAGED;
            Token parsed = MAPPER.readValue(bytes, Token.class);
            if (parsed == null || blank(parsed.getUsername()) || blank(parsed.getPassword())) return Status.DAMAGED;
            List<ServerFromToken> normal = parsed.getServers();
            List<ServerFromToken> censored = parsed.getCensoredServers();
            if (!validServers(normal) || !validServers(censored)) return Status.DAMAGED;
            if ((normal == null || normal.isEmpty()) && (censored == null || censored.isEmpty())) {
                return Status.EMPTY_SERVERS;
            }
            return Status.VALID;
        } catch (Exception ignored) {
            // Never put a token, a decoded JSON document or parser exceptions into QR logs.
            return Status.DAMAGED;
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean validServers(List<ServerFromToken> servers) {
        if (servers == null) return true;
        for (ServerFromToken server : servers) {
            if (server == null || blank(server.getName()) || server.getName().length() > 100
                    || blank(server.getHost()) || server.getPort() == null
                    || server.getPort() < 1 || server.getPort() > 65535) return false;
        }
        return true;
    }
}
