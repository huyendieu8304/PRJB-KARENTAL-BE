package com.mp.karental.payment.constant;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class VNPayParamsTest {

    @Test
    void testAllFieldsArePublicStaticFinal() {
        Field[] fields = VNPayParams.class.getDeclaredFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            assertTrue(Modifier.isPublic(modifiers), 
                "Field " + field.getName() + " should be public");
            assertTrue(Modifier.isStatic(modifiers), 
                "Field " + field.getName() + " should be static");
            assertTrue(Modifier.isFinal(modifiers), 
                "Field " + field.getName() + " should be final");
        }
    }

    @Test
    void testAllFieldsAreStrings() {
        Field[] fields = VNPayParams.class.getDeclaredFields();
        for (Field field : fields) {
            assertEquals(String.class, field.getType(),
                "Field " + field.getName() + " should be of type String");
        }
    }

    @Test
    void testConstantValues() throws IllegalAccessException {
        // Test each constant has the expected value
        assertEquals("vnp_SecureHash", VNPayParams.SECURE_HASH);
        assertEquals("vnp_SecureHashType", VNPayParams.SECURE_HASH_TYPE);
        assertEquals("vnp_Amount", VNPayParams.AMOUNT);
        assertEquals("vnp_OrderInfo", VNPayParams.ORDER_INFO);
        assertEquals("vnp_OrderType", VNPayParams.ORDER_TYPE);
        assertEquals("vnp_TxnRef", VNPayParams.TXN_REF);
        assertEquals("vnp_Version", VNPayParams.VERSION);
        assertEquals("vnp_Command", VNPayParams.COMMAND);
        assertEquals("vnp_TmnCode", VNPayParams.TMN_CODE);
        assertEquals("vnp_CurrCode", VNPayParams.CURRENCY);
        assertEquals("vnp_ReturnUrl", VNPayParams.RETURN_URL);
        assertEquals("vnp_CreateDate", VNPayParams.CREATED_DATE);
        assertEquals("vnp_ExpireDate", VNPayParams.EXPIRE_DATE);
        assertEquals("vnp_IpAddr", VNPayParams.IP_ADDRESS);
        assertEquals("vnp_Locale", VNPayParams.LOCALE);
    }

    @Test
    void testAllConstantsStartWithVnp() {
        Field[] fields = VNPayParams.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                String value = (String) field.get(null);
                assertTrue(value.startsWith("vnp_"),
                    "Constant " + field.getName() + " value should start with 'vnp_'");
            } catch (IllegalAccessException e) {
                fail("Could not access field " + field.getName());
            }
        }
    }

    @Test
    void testNoNullOrEmptyConstants() {
        Field[] fields = VNPayParams.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                String value = (String) field.get(null);
                assertNotNull(value, "Constant " + field.getName() + " should not be null");
                assertFalse(value.isEmpty(), "Constant " + field.getName() + " should not be empty");
            } catch (IllegalAccessException e) {
                fail("Could not access field " + field.getName());
            }
        }
    }

    @Test
    void testConstantsAreUnique() throws IllegalAccessException {
        Field[] fields = VNPayParams.class.getDeclaredFields();
        java.util.Set<String> values = new java.util.HashSet<>();
        for (Field field : fields) {
            String value = (String) field.get(null);
            assertTrue(values.add(value),
                "Constant value '" + value + "' is duplicated");
        }
    }
} 