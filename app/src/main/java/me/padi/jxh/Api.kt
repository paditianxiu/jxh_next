package me.padi.jxh

object Api {
    const val BASE_SCHOOL_URL = "https://www.jhzyedu.cn"
    const val BASE_URL = "https://jw.jhzyedu.cn"
    const val LOGIN_URL = "$BASE_URL/xtgl/login_slogin.html?time="
    const val LOGIN_KEY_URL = "$BASE_URL/xtgl/login_getPublicKey.html?time="
    const val CODE_URL = "$BASE_URL/kaptcha?time="
    const val PRIVACY_URL = "$BASE_URL/xtgl/login_cxGxBmysxyqrxx.html"
    const val STUDENT_INFO_URL =
        "${BASE_URL}/xsxxxggl/xsgrxxwh_cxXsgrxx.html?gnmkdm=N100801&layout=default"
    const val SCORE_URL =
        "${BASE_URL}/cjcx/cjcx_cxDgXscj.html?gnmkdm=N305005&doType=query&fromXh_id="
    const val COURSE_URL = "${BASE_URL}/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=N2151"
    const val CLASS_LIST_URL = "${BASE_URL}/kbdy/bjkbdy_cxBjkbdyTjkbList.html?gnmkdm=N214505"
    const val CLASS_COURSE_URL = "${BASE_URL}/kbdy/bjkbdy_cxBjKb.html?gnmkdm=N214505"
    const val LOGOUT_URL = "${BASE_URL}/logout?t="
}