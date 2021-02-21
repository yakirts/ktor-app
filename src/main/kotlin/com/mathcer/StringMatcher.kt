package com.mathcer

import net.openhft.hashing.LongHashFunction


abstract class StringMatcher {
    abstract fun isMatch(s1: String, s2: String): Boolean

    companion object {
        fun createMatcher(className: String): StringMatcher {
            return when (className) {
                "SimpleStringMatcher" -> CaseStringMatcher()
                "ComplexStringMatcher" -> ComplexStringMatcher()
                else -> DefaultStringMatcher()
            }
        }
    }
}

class DefaultStringMatcher : StringMatcher() {
    override fun isMatch(s1: String, s2: String): Boolean {
        return s1 == s2
    }
}

class CaseStringMatcher : StringMatcher() {
    override fun isMatch(s1: String, s2: String): Boolean {
        return s1.toLowerCase() == s2.toLowerCase()
    }
}

class ComplexStringMatcher : StringMatcher() {
    private val HASH = LongHashFunction.xx(0)
    private fun String.xxHash() = HASH.hashChars(this)

    override fun isMatch(s1: String, s2: String): Boolean {
        return s1.xxHash() == s2.xxHash()
    }
}