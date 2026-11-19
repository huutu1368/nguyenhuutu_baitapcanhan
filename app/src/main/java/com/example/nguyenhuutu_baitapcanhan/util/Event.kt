package com.example.nguyenhuutu_baitapcanhan.util

class Event<out T>(private val value: T) {
    private var handled = false
    fun getContentIfNotHandled(): T? {
        if (handled) return null
        handled = true
        return value
    }
}
