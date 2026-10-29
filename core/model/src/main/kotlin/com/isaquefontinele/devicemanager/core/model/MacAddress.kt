package com.isaquefontinele.devicemanager.core.model

/**
 * The API stores MAC addresses as 12 hex digits without separators (`A0B1C2D3E4F5`),
 * while the UI shows them grouped (`A0:B1:C2:D3:E4:F5`).
 */
object MacAddress {
    private const val LENGTH = 12
    private const val GROUP_SIZE = 2
    private val separators = Regex("[:\\-.\\s]")
    private val normalizedPattern = Regex("^[0-9A-F]{$LENGTH}$")

    /** Removes the usual separators and uppercases the value. Does not validate it. */
    fun normalize(value: String): String = value.replace(separators, "").uppercase()

    fun isValid(value: String): Boolean = normalizedPattern.matches(normalize(value))

    /** Groups a valid MAC address in pairs separated by `:`; invalid values are returned unchanged. */
    fun format(value: String): String {
        val normalized = normalize(value)
        if (!normalizedPattern.matches(normalized)) return value
        return normalized.chunked(GROUP_SIZE).joinToString(":")
    }
}
