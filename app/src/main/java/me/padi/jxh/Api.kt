package me.padi.jxh

object Api {
    val BASE_URL = "https://jw.jhzyedu.cn"
    val LOGIN_URL = "$BASE_URL/xtgl/login_slogin.html?time="
    val LOGIN_KEY_URL = "$BASE_URL/xtgl/login_getPublicKey.html?time="
    val CODE_URL = "$BASE_URL/kaptcha?time="
    val PRIVACY_URL = "$BASE_URL/xtgl/login_cxGxBmysxyqrxx.html"
    val STUDENT_INFO_URL =
        "${BASE_URL}/xsxxxggl/xsgrxxwh_cxXsgrxx.html?gnmkdm=N100801&layout=default"
    val SCORE_URL = "${BASE_URL}/cjcx/cjcx_cxDgXscj.html?gnmkdm=N305005&doType=query&fromXh_id="
    val COURSE_URL = "${BASE_URL}/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=N2151"
    val CLASS_LIST_URL = "${BASE_URL}/kbdy/bjkbdy_cxBjkbdyTjkbList.html?gnmkdm=N214505"
    val CLASS_COURSE_URL="${BASE_URL}/kbdy/bjkbdy_cxBjKb.html?gnmkdm=N214505"
    val LOGOUT_URL="${BASE_URL}/logout?t="
}