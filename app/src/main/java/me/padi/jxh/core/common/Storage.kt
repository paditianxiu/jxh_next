package me.padi.jxh.core.common

interface Storage {
    fun get(): String?
    fun set(value: String)
    fun clear()
}
