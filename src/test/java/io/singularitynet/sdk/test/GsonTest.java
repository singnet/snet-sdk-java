package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;
import com.google.gson.*;

public class GsonTest {

    Gson gson;

    @Before
    public void setUp() {
        this.gson = new Gson();
    }

    @Test
    public void testParseStringField() {
        StringField result = this.gson.fromJson("{ \"field\": \"value\" }", StringField.class);
        assertEquals("value", result.field);
    }

    private static class StringField {
        String field;
    }

    @Test
    public void testParseCamelCaseField() {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        CamelCaseField result = gson.fromJson("{ \"camel_case_field\": \"value\" }", CamelCaseField.class);
        assertEquals("value", result.camelCaseField);
    }

    private static class CamelCaseField {
        String camelCaseField;
    }

    @Test
    public void testParseFinalField() {
        FinalField result = this.gson.fromJson("{ \"field\": \"value\" }", FinalField.class);
        assertEquals("value", result.field);
    }

    private static class FinalField {
        final String field;
        private FinalField() {
            this.field = "";
        }
    }
}
