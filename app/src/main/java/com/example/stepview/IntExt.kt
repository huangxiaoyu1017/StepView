package cn.weeget.youxuanapp.common.util.ext

import android.content.res.Resources
import kotlin.math.roundToInt

/**
 * sp to px
 */
val Int.sp: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()
/**
 * dp to px
 */
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

/**
 * px to dp
 */
val Int.px2dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).roundToInt()