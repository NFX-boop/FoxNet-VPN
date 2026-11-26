package com.foxnet.app.ui.qrscanner;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.junit.Assert.assertEquals;
import static com.foxnet.app.ui.qrscanner.QrTokenValidator.Status.*;

public class QrTokenValidatorTest {
    private static final String SERVER = "{\"name\":\"Test\",\"host\":\"example.invalid\",\"port\":443}";
    private static String token(String prefix, String json) {
        return prefix + Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
    private static String json(String servers) {
        return "{\"username\":\"test\",\"password\":\"not-a-real-password\",\"servers\":" + servers + "}";
    }
    @Test public void acceptsPlainAndLegacy() {
        assertEquals(VALID, QrTokenValidator.validate(token("fptn:", json("[" + SERVER + "]"))));
        assertEquals(VALID, QrTokenValidator.validate(token("fptn://", json("[" + SERVER + "]"))));
    }
    @Test public void acceptsCensoredOnly() {
        assertEquals(VALID, QrTokenValidator.validate(token("fptn:", "{\"username\":\"u\",\"password\":\"p\",\"censored_zone_servers\":[" + SERVER + "]}")));
    }
    @Test public void rejectsForeignQr() {
        assertEquals(WRONG_FORMAT, QrTokenValidator.validate("https://example.invalid"));
        assertEquals(WRONG_FORMAT, QrTokenValidator.validate("hello"));
    }
    @Test public void rejectsBrokenPayload() {
        assertEquals(DAMAGED, QrTokenValidator.validate("fptn:"));
        assertEquals(DAMAGED, QrTokenValidator.validate("fptnb:12345"));
        assertEquals(DAMAGED, QrTokenValidator.validate("fptn:%%%"));
        assertEquals(DAMAGED, QrTokenValidator.validate(token("fptn:", "not json")));
    }
    @Test public void rejectsEmptyServers() {
        assertEquals(EMPTY_SERVERS, QrTokenValidator.validate(token("fptn:", json("[]"))));
    }
    @Test public void rejectsBadCredentialsAndPorts() {
        assertEquals(DAMAGED, QrTokenValidator.validate(token("fptn:", "{\"username\":\"\",\"password\":\"p\",\"servers\":[" + SERVER + "]}")));
        assertEquals(DAMAGED, QrTokenValidator.validate(token("fptn:", json("[" + SERVER.replace("443", "65536") + "]"))));
    }
    @Test public void boundsInput() {
        assertEquals(DAMAGED, QrTokenValidator.validate("fptn:" + "A".repeat(4096)));
    }
}
