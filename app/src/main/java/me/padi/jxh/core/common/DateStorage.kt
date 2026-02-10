package me.padi.jxh.core.common

import android.os.Build
import androidx.annotation.RequiresApi
import com.tencent.mmkv.MMKV
import java.time.LocalDate

object DateStorage {
    private val mmkv = MMKV.defaultMMKV()

    // Key 前缀
    private const val KEY_START_DATE = "start_class_date"
    private const val KEY_YEAR = "year_"
    private const val KEY_SEMESTER = "semester_"


    private const val KEY_IS_SAME = "is_same_config" // 新增 Key

    fun saveIsSame(isSame: Boolean) {
        mmkv.encode(KEY_IS_SAME, isSame)
    }

    fun getIsSame(): Boolean {
        return mmkv.decodeBool(KEY_IS_SAME, true)
    }

    // 定义类型：个人或班级
    enum class Type(val suffix: String) {
        PERSONAL("personal"), CLASS("class")
    }

    fun saveDate(date: LocalDate) = mmkv.encode(KEY_START_DATE, date.toString())

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDate(): LocalDate {
        val dateString = mmkv.decodeString(KEY_START_DATE, null)
        return if (dateString == null) LocalDate.now() else LocalDate.parse(dateString)
    }

    fun saveYear(type: Type, year: Int) {
        mmkv.encode(KEY_YEAR + type.suffix, year)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getYear(type: Type): Int {
        return mmkv.decodeInt(KEY_YEAR + type.suffix, LocalDate.now().year)
    }

    fun saveSemester(type: Type, semester: Int) {
        mmkv.encode(KEY_SEMESTER + type.suffix, semester)
    }

    fun getSemester(type: Type): Int {
        // 默认第一学期 3
        return mmkv.decodeInt(KEY_SEMESTER + type.suffix, 3)
    }
}