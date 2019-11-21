package io.singularitynet.sdk.test;

import org.junit.*;
import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.Map;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

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

    @Test
    public void testReadToMap() {
        String json = "{\"1\":{\"events\":{},\"links\":{},\"address\":\"0xdce9c76ccb881af94f7fb4fac94e4acc584fa9a5\",\"transactionHash\":\"0x29f3271851bb6b2a0d85fa94084945859467c872a00d158ab05dbd8c131c0e24\"},\"3\":{\"events\":{},\"links\":{},\"address\":\"0x663422c6999ff94933dbcb388623952cf2407f6f\",\"transactionHash\":\"0x150f6f8d47978152c3d1ed84f49d78aa4619a0b08c381b549f9ba6dedc818968\"},\"42\":{\"events\":{},\"links\":{},\"address\":\"0x89a780619a7b0542b52bbb929bc1ea01516542ec\",\"transactionHash\":\"0x5ba7650968492c2822175e80e4ceed9e86a50942ef8c243f6cd35b5d753b0add\"}}";
        Type mapType = new TypeToken<Map<String, Map<String, Object>>>(){}.getType();

        Map<String, Map<String, Object>> map = gson.fromJson(json, mapType);

        assertEquals("Kovan address", "0x89a780619a7b0542b52bbb929bc1ea01516542ec", map.get("42").get("address"));
    }

    @Test
    public void testParseEnumField() {
        EnumField result = this.gson.fromJson("{ \"field\": \"FIRST\" }", EnumField.class);

        assertEquals(EnumField.EnumType.FIRST, result.field);
    }

    private static class EnumField {

        EnumType field;

        private static enum EnumType {
            FIRST,
            SECOND
        }
    }

}
