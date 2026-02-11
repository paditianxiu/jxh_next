package me.padi.jxh.core.common

import com.tencent.mmkv.MMKV


class MMKVCookieStorage(
    mmkvId: String = "ktor_cookie_storage"
) : Storage {

    private val mmkv = MMKV.mmkvWithID(mmkvId)

    override fun get(): String? {
        return mmkv.decodeString(KEY)
    }

    override fun set(value: String) {
        mmkv.encode(KEY, value)
    }

    override fun clear() {
        mmkv.clear()
        mmkv.removeValueForKey(KEY)
    }

    private companion object {
        const val KEY = "cookies_json"
    }
}
