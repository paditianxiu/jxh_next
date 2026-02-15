package me.padi.jxh.core.utils

class Constants {
    companion object {
        const val PINDUODUO_URL =
            "pinduoduo://com.xunmeng.pinduoduo/mdkd/package?tab=ID_CODE&entry_source=11&refer_page_name=login&refer_page_id=10169_1751901995470_3gdprcfjhr&refer_page_sn=10169"
        const val TAOBAO_URL =
            "https://pages-fast.m.taobao.com/wow/z/uniapp/1011717/last-mile-fe/end-collect-platform/identity-code?x-ssr=true"

        const val SCHOOL_NEWS_URL = "https://jhzyedu.cn/index/xxyw"
        const val NEWS_FLASH_URL = "https://jhzyedu.cn/index/xwkx"
        const val COLLEGE_NEWS_URL = "https://jhzyedu.cn/index/xydt"
        const val JW_NOTIFY_URL = "https://www.jhzyedu.cn/jxky/jwtz"
        const val RESOURCE_DOWNLOAD_URL = "https://www.jhzyedu.cn/jxky/zyxz"
        const val DATA_SEARCH_URL = "https://www.jhzyedu.cn/jxky/zlcx"
        const val RESEARCH_MANAGEMENT_URL = "https://www.jhzyedu.cn/jxky/kygl"
        const val JOB_URL = "https://www.jhzyedu.cn/zsjy/jygz"
        const val JOB_POLICY_URL = "https://www.jhzyedu.cn/zsjy/jyzc"
        const val JOB_DYNAMIC_URL = "https://www.jhzyedu.cn/zsjy/zpdt"

        val NEW_LIST = mapOf<String, String>(
            "学校要闻" to SCHOOL_NEWS_URL,
            "新闻快讯" to NEWS_FLASH_URL,
            "学院动态" to COLLEGE_NEWS_URL,
            "教务通知" to JW_NOTIFY_URL,
            "资源下载" to RESOURCE_DOWNLOAD_URL,
            "资料查询" to DATA_SEARCH_URL,
            "科研管理" to RESEARCH_MANAGEMENT_URL,
            "就业工作" to JOB_URL,
            "就业政策" to JOB_POLICY_URL,
            "招聘动态" to JOB_DYNAMIC_URL,
        )

    }
}