package me.padi.jxh.data.repository

import io.ktor.http.Parameters
import me.padi.jxh.Api
import me.padi.jxh.core.common.d
import me.padi.jxh.core.network.NetworkDataSource
import me.padi.jxh.core.network.NetworkState
import org.json.JSONArray
import org.json.JSONObject

open class CourseRepository(
    private val network: NetworkDataSource
) {

    open suspend fun fetchCourse(
        year: String, semester: String
    ): NetworkState<String> {
        return runCatching {
            val result = network.postText(Api.COURSE_URL, Parameters.build {
                append("xnm", year)
                append("xqm", semester)
            }).getOrThrow()
            NetworkState.Success(result)
        }.getOrElse { throwable ->
            NetworkState.Error("获取课程表失败", throwable)
        }
    }

    open suspend fun fetchClassCourse(
        params: ClassParams
    ): NetworkState<String> {
        return runCatching {
            val formData = mapOf(
                "xnm" to params.xnm,
                "xqm" to params.xqm,
                "xnmc" to params.xnmc,
                "xqmmc" to params.xqmmc,
                "xqh_id" to params.xqhId,
                "njdm_id" to params.njdmId,
                "zyh_id" to params.zyhId,
                "bh_id" to params.bhId,
                "tjkbzdm" to params.tjkbzdm,
                "tjkbzxsdm" to params.tjkbzxsdm,
                "zymc" to params.zymc,
                "jgmc" to params.jgmc,
                "njmc" to params.njmc,
                "bj" to params.bj,
                "xkrs" to params.xkrs,
                "jsxm" to params.jsxm,
                "lxdh" to params.lxdh,
                "bh" to params.bh,
                "zs" to params.zs,
                "zxszjjs" to params.zxszjjs,
                "xsdm" to params.xsdm,
                "kclxdm" to params.kclxdm,
                "kclbdm" to params.kclbdm,
                "kbsjlyqz" to params.kbsjlyqz,
                "yf" to params.yf,
                "kzlx" to params.kzlx
            )
            val result = network.postFormData(Api.CLASS_COURSE_URL, formData).getOrThrow()
            NetworkState.Success(result)
        }.getOrElse { throwable ->
            NetworkState.Error("获取班级课程表失败", throwable)
        }
    }


    open suspend fun fetchClassList(
        year: String, semester: String
    ): NetworkState<List<ProcessedClassInfo>> {
        return runCatching {
            val nowTime = System.currentTimeMillis().toString()
            val result = network.postText(Api.CLASS_LIST_URL, Parameters.build {
                append("xnm", year)
                append("xqm", semester)
                append("xqh_id", "")
                append("njdm_id", "")
                append("jg_id", "")
                append("zyh_id", "")
                append("zyfx_id", "")
                append("bh_id", "")
                append("xsdm", "")
                append("pyccdm", "")
                append("kclxdm", "")
                append("kclbdm", "")
                append("sfzhsjk", "")
                append("kbsjlyqz", "")
                append("zs", "")
                append("yf", "")
                append("_search", "false")
                append("nd", nowTime)
                append("queryModel.showCount", "5000")
                append("queryModel.currentPage", "1")
                append("queryModel.sortName", " ")
                append("queryModel.sortOrder", "asc")
                append("time", "10")
            }).getOrThrow()
            val processedList = parseClassListJson(result)
            NetworkState.Success(processedList)
        }.getOrElse { throwable ->
            NetworkState.Error("获取班级列表失败", throwable)
        }
    }

    private fun parseClassListJson(jsonString: String): List<ProcessedClassInfo> {
        return try {
            val jsonObject = JSONObject(jsonString)
            val itemsArray = jsonObject.optJSONArray("items") ?: JSONArray()
            val processedList = mutableListOf<ProcessedClassInfo>()
            for (i in 0 until itemsArray.length()) {
                val item = itemsArray.getJSONObject(i)
                val processedItem = ProcessedClassInfo(
                    college = item.optString("jgmc", ""),
                    className = item.optString("bh", ""),
                    campus = item.optString("xqmc", ""),
                    major = item.optString("zymc", ""),
                    studentCount = item.optString("xkrs", ""),
                    params = ClassParams(
                        xnm = item.optString("xnm", ""),
                        xqm = item.optString("xqm", ""),
                        xnmc = item.optString("xnmc", ""),
                        xqmmc = item.optString("xqmmc", ""),
                        xqhId = item.optString("xqh_id", ""),
                        njdmId = item.optString("njdm_id", ""),
                        zyhId = item.optString("zyh_id", ""),
                        bhId = item.optString("bh_id", ""),
                        tjkbzdm = item.optString("tjkbzdm", ""),
                        tjkbzxsdm = item.optString("tjkbzxsdm", ""),
                        zymc = item.optString("zymc", ""),
                        jgmc = item.optString("jgmc", ""),
                        njmc = item.optString("njmc", ""),
                        bj = item.optString("bj", ""),
                        xkrs = item.optString("xkrs", ""),
                        jsxm = "",
                        lxdh = "",
                        bh = item.optString("bh", ""),
                        zs = "",
                        zxszjjs = "false",
                        xsdm = "",
                        kclxdm = "",
                        kclbdm = "",
                        kbsjlyqz = "",
                        yf = "",
                        kzlx = "ck"
                    )
                )
                processedList.add(processedItem)
            }
            processedList
        } catch (e: Exception) {
            emptyList()
        }
    }


}

data class ClassParams(
    val xnm: String = "",
    val xqm: String = "",
    val xnmc: String = "",
    val xqmmc: String = "",
    val xqhId: String = "",
    val njdmId: String = "",
    val zyhId: String = "",
    val bhId: String = "",
    val tjkbzdm: String = "",
    val tjkbzxsdm: String = "",
    val zymc: String = "",
    val jgmc: String = "",
    val njmc: String = "",
    val bj: String = "",
    val xkrs: String = "",
    val jsxm: String = "",
    val lxdh: String = "",
    val bh: String = "",
    val zs: String = "",
    val zxszjjs: String = "false",
    val xsdm: String = "",
    val kclxdm: String = "",
    val kclbdm: String = "",
    val kbsjlyqz: String = "",
    val yf: String = "",
    val kzlx: String = "ck"
)

fun ClassParams.isEmpty(): Boolean {
    return this == ClassParams()
}

data class ProcessedClassInfo(
    val college: String,
    val className: String,
    val campus: String,
    val major: String,
    val studentCount: String,
    val params: ClassParams
)