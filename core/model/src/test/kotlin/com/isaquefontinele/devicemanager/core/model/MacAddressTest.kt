package com.isaquefontinele.devicemanager.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MacAddressTest {

    @Test
    fun `normalize removes separators and uppercases`() {
        assertEquals("A0B1C2D3E4F5", MacAddress.normalize("a0:b1:c2:d3:e4:f5"))
        assertEquals("A0B1C2D3E4F5", MacAddress.normalize("A0-B1-C2-D3-E4-F5"))
        assertEquals("A0B1C2D3E4F5", MacAddress.normalize(" a0b1.c2d3.e4f5 "))
    }

    @Test
    fun `isValid accepts 12 hex digits with or without separators`() {
        assertTrue(MacAddress.isValid("1A2B3C4D5E6F"))
        assertTrue(MacAddress.isValid("1a:2b:3c:4d:5e:6f"))
    }

    @Test
    fun `isValid rejects wrong length or non hex characters`() {
        assertFalse(MacAddress.isValid(""))
        assertFalse(MacAddress.isValid("1A2B3C4D5E"))
        assertFalse(MacAddress.isValid("1A2B3C4D5E6F7A"))
        assertFalse(MacAddress.isValid("1A2B3C4D5E6G"))
    }

    @Test
    fun `format groups a valid address in pairs`() {
        assertEquals("A0:B1:C2:D3:E4:F5", MacAddress.format("a0b1c2d3e4f5"))
    }

    @Test
    fun `format returns invalid values unchanged`() {
        assertEquals("not-a-mac", MacAddress.format("not-a-mac"))
    }
}
