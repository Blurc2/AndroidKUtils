package com.raer.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.hardware.fingerprint.FingerprintManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.raer.utils.FingerPrintUtils.FingerPrintValidations
import com.raer.utils.base.BaseViewModelFactory
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun Context.showToast(msg : String){
    Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}

fun Context.showToast(@StringRes res: Int){
    Toast.makeText(this,res,Toast.LENGTH_SHORT).show()
}

fun Any.toJson() : String = Gson().toJson(this)

inline fun <reified T> String.fromJson(): T = Gson().fromJson(this,T::class.java)

inline fun <reified T : ViewModel> Fragment.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null)
        ViewModelProviders.of(this).get(T::class.java)
    else
        ViewModelProviders.of(this, BaseViewModelFactory(creator)).get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null)
        ViewModelProviders.of(this).get(T::class.java)
    else
        ViewModelProviders.of(this, BaseViewModelFactory(creator)).get(T::class.java)
}

/**
 * Converts a value to px
 *
 * @return value in px
 */
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density + 0.5).toInt()

/**
 * Converts a value to dp
 *
 * @return value in dp
 */
fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density + 0.5).toInt()

/**
 * Converts a value to sp
 *
 * @return value in sp
 */
fun Int.toSp(): Float = (Resources.getSystem().displayMetrics.scaledDensity * this).toFloat()


/**
 * Converts a value to px
 *
 * @return value in px
 */
fun Int.toPx(context: Context): Int = (this * context.resources.displayMetrics.density + 0.5).toInt()

/**
 * Converts a value to dp
 *
 * @return value in dp
 */
fun Int.toDp(context: Context): Int = (this / context.resources.displayMetrics.density + 0.5).toInt()

/**
 * Converts a value to sp
 *
 * @return value in sp
 */
fun Int.toSp(context: Context): Float = (context.resources.displayMetrics.scaledDensity * this).toFloat()
/**
 * Fades in view.
 *
 * @param time the animation time
 */
fun View.FadeInView(time: Int) {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
        alpha = 0.0f
        animate().alpha(1.0f).setDuration(time.toLong()).setListener(null)
    }
}

/**
 * Fades out view.
 *
 * @param time the animation time
 */
fun View.FadeOutView(time: Int) {
    if (visibility != View.GONE)
        animate().alpha(0.0f).setDuration(time.toLong()).setListener(object :
            AnimatorListenerAdapter() {
            override fun onAnimationEnd(param1Animator: Animator) {
                super.onAnimationEnd(param1Animator)
                visibility = View.GONE
            }
        })
}

/**
 * Slides in view.
 *
 * @param targetHeight the target height
 * @param time         the animation time
 */
fun View.SlideView(targetHeight: Int, time: Int): ResizeHeightAnimation {
    val resizeHeightAnimation = ResizeHeightAnimation(this, targetHeight)
    resizeHeightAnimation.interpolator = AccelerateDecelerateInterpolator()
    resizeHeightAnimation.duration = time.toLong()
    startAnimation(resizeHeightAnimation)
    return resizeHeightAnimation
}

fun Any?.isNull(): Boolean =  this?.let { false } ?: true

/**
 * Checks finger print validations.
 *
 * @return the finger validations
 *
 * @see FingerPrintValidations
 */
@SuppressLint("MissingPermission")
@RequiresApi(api = Build.VERSION_CODES.M)
fun FragmentActivity.validateFingerPrint(): FingerPrintValidations {
    val keyguardManager =
        getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val fingerprintManager =
        getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
    if (!fingerprintManager.isHardwareDetected)
        return FingerPrintValidations.NO_HARDWARE
    if (!keyguardManager.isKeyguardSecure) {
        customDialog {
            title = R.string.config_finger_header.stringFromResource()
            msg = R.string.config_finger_msg.stringFromResource()
            btnAcceptText = R.string.btn_config.stringFromResource()
            btnCancelText = R.string.btn_cancel.stringFromResource()
            btnAcceptAction {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                startActivity(intent)
            }
            btnCancelAction {  }
        }
        return FingerPrintValidations.NO_SECURE_LOCK
    }
    if (!fingerprintManager.hasEnrolledFingerprints()) {
        customDialog {
            title = R.string.config_finger_header.stringFromResource()
            msg = R.string.config_finger_msg.stringFromResource()
            btnAcceptText = R.string.btn_config.stringFromResource()
            btnCancelText = R.string.btn_cancel.stringFromResource()
            btnAcceptAction {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                startActivity(intent)
            }
            btnCancelAction {  }
        }
        return FingerPrintValidations.NO_ENROLLED
    }
    return FingerPrintValidations.VALID
}

fun View.setOnDebouncClickListener(time : Long = 1000L,listener : View.OnClickListener) {
    setOnClickListener(ClickListenerWrapper(listener,time))
}

fun View.setOnDebouncClickListener(time : Long = 1000L,listener : (v: View)->Unit) {
    setOnClickListener(ClickListenerWrapper(listener,time))
}

@SuppressLint("SimpleDateFormat")
fun Long.getDateFormatFromTimeStamp(format : String):String= SimpleDateFormat(format).format(Date(this))

fun Long.getDateFromTimeStamp(): Date = Date(this)

fun Context.getDrawableResUri(@DrawableRes drawableId: Int): String {

    val imageUri = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(drawableId))
        .appendPath(resources.getResourceTypeName(drawableId))
        .appendPath(resources.getResourceEntryName(drawableId))
        .build()

    return imageUri.toString()
}

fun Int.stringFromResource() : String = Resources.getSystem().getString(this)

fun Context.getCompatColor(@ColorRes color: Int): Int = ContextCompat.getColor(this, color)

fun Context.getCompatDrawable(@DrawableRes drawableId: Int): Drawable? = ContextCompat.getDrawable(this, drawableId)

fun Date.format(formatPattern: String): String = SimpleDateFormat(formatPattern, Locale.getDefault()).format(this)

fun String.parseDate(formatPattern: String): Date? = SimpleDateFormat(formatPattern, Locale.getDefault()).parse(this)

fun Any.amountFormat() = DecimalFormat("#,###.00").format(this)

fun Any.decimalFormat(pattern: String) = DecimalFormat(pattern).format(this)

/**
 * Find the closest ancestor of the given type.
 */
inline fun <reified T> View.findParentOfType(): T? {
    var p = parent
    while (p != null && p !is T) p = p.parent
    return p as T?
}

/**
 * Scroll down the minimum needed amount to show [descendant] in full. More
 * precisely, reveal its bottom.
 */
fun ViewGroup.scrollDownTo(descendant: View) {
    // Could use smoothScrollBy, but it sometimes over-scrolled a lot
    howFarDownIs(descendant)?.let { scrollBy(0, it) }
}

/**
 * Calculate how many pixels below the visible portion of this [ViewGroup] is the
 * bottom of [descendant].
 *
 * In other words, how much you need to scroll down, to make [descendant]'s bottom
 * visible.
 */
fun ViewGroup.howFarDownIs(descendant: View): Int? {
    val bottom = Rect().also {
        descendant.getDrawingRect(it)
        offsetDescendantRectToMyCoords(descendant, it)
    }.bottom
    return (bottom - height - scrollY).takeIf { it > 0 }
}

/**
 * Ciphers a string with the given key
 *
 * @param key the string which is going to be used as key
 * @return the ciphered string
 * @see AESEncrypt.encryptToString
 */
private fun String.encryptWithKey(key: String): String? = AESEncrypt.encryptToString(this, key)?.trim()

/**
 * Deciphers a string with the given key
 *
 * @param key the string which is going to be used as key
 * @return the ciphered string
 * @see AESEncrypt.encryptToString
 */
private fun String.decryptWithKey(key: String): String? = AESEncrypt.decryptToString(this, key)?.trim()

/**
 * Encodes a string into base64
 *
 * @return the encoded string
 * @throws UnsupportedEncodingException
 * @see Base64
 */
@Throws(UnsupportedEncodingException::class)
private fun String.base64Encode(): String = Base64.encodeToString(this.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)

fun FragmentActivity.customDialog(tag: String? = null, init: DialogDSL.() -> Unit) = DialogDSL().apply { init() }.show(supportFragmentManager,tag)

fun Fragment.customDialog(tag: String? = null, init: DialogDSL.() -> Unit) = DialogDSL().apply { init() }.show(childFragmentManager,tag)

fun RadioGroup.getSelectedItem() : Any?{
    for(i in 0..childCount)
    {
        val child = getChildAt(i) as? RadioButton
        child?.let {
            if(it.isChecked)
                return it.tag
        }
    }
    return null
}

/**
 * Apply settings for show the item list in horizontal
 * @param[hasFixedSize] true if adapter changes cannot affect the size of the RecyclerView.
 * @param[reverseLayout] Used to reverse item traversal and layout order.
 * @param[stackFromEnd] true to pin the view's content to the bottom edge, false to pin the
 *                      view's content to the top edge
 * @param[pIsNestedScrollingEnabled] true to enable nested scrolling dispatch from this view, false otherwise
 * @param[pIsMotionEventSplittingEnabled] true to allow MotionEvents to be split and dispatched to multiple
 *              child views. false to only allow one child view to be the target of any MotionEvent received by this ViewGroup.
 */
fun RecyclerView.initColumn(hasFixedSize: Boolean = true,
                            reverseLayout: Boolean = false,
                            stackFromEnd: Boolean = false,
                            spacingItem: Int = 0,
                            pIsNestedScrollingEnabled: Boolean = true,
                            pIsMotionEventSplittingEnabled: Boolean = true) {

    setHasFixedSize(hasFixedSize)
    addItemDecoration(SpacesItemDecoration(spacingItem, 3))
    isNestedScrollingEnabled = pIsNestedScrollingEnabled
    isMotionEventSplittingEnabled = pIsMotionEventSplittingEnabled
    layoutManager = getLinearLayoutVertical(context, reverseLayout, stackFromEnd)
}

/**
 * Apply settings for show the item list in vertical
 * @param[hasFixedSize] true if adapter changes cannot affect the size of the RecyclerView.
 * @param[reverseLayout] Used to reverse item traversal and layout order.
 * @param[stackFromEnd] true to pin the view's content to the bottom edge, false to pin the
 *                      view's content to the top edge
 * @param[pIsNestedScrollingEnabled] true to enable nested scrolling dispatch from this view, false otherwise
 * @param[pIsMotionEventSplittingEnabled] true to allow MotionEvents to be split and dispatched to multiple
 *              child views. false to only allow one child view to be the target of any MotionEvent received by this ViewGroup.
 */
fun RecyclerView.initRow(hasFixedSize: Boolean = true,
                         reverseLayout: Boolean = false,
                         stackFromEnd: Boolean = false,
                         spacingItem: Int = 0,
                         pIsNestedScrollingEnabled: Boolean = true,
                         pIsMotionEventSplittingEnabled: Boolean = true,
                         type: Int=1) {

    setHasFixedSize(hasFixedSize)
    isNestedScrollingEnabled = pIsNestedScrollingEnabled
    addItemDecoration(SpacesItemDecoration(spacingItem, type))
    isMotionEventSplittingEnabled = pIsMotionEventSplittingEnabled
    layoutManager = getLinearLayoutHorizontal(context, reverseLayout, stackFromEnd)
}

fun  RecyclerView.initStickyRow(
    mListener: StickHeaderItemDecoration.StickyHeaderInterface? = null,
    hasFixedSize: Boolean = true,
    reverseLayout: Boolean = false,
    stackFromEnd: Boolean = false,
    spacingItem: Int = 0,
    @LayoutRes layoutId: Int,
    pIsNestedScrollingEnabled: Boolean = true,
    pIsMotionEventSplittingEnabled: Boolean = true,
    type: Int=1){

    setHasFixedSize(hasFixedSize)
    isNestedScrollingEnabled = pIsNestedScrollingEnabled
    mListener?.let {
        addItemDecoration(StickHeaderItemDecoration(it,layoutId,spacingItem, type))
    }
    isMotionEventSplittingEnabled = pIsMotionEventSplittingEnabled
    layoutManager = getLinearLayoutHorizontal(context, reverseLayout, stackFromEnd)
}

fun RecyclerView.initStickyColumn(
    mListener: StickHeaderItemDecoration.StickyHeaderInterface? = null,
    hasFixedSize: Boolean = true,
    reverseLayout: Boolean = false,
    stackFromEnd: Boolean = false,
    spacingItem: Int = 0,
    @LayoutRes layoutId: Int,
    pIsNestedScrollingEnabled: Boolean = true,
    pIsMotionEventSplittingEnabled: Boolean = true,
    type: Int=3){

    setHasFixedSize(hasFixedSize)
    isNestedScrollingEnabled = pIsNestedScrollingEnabled
    isMotionEventSplittingEnabled = pIsMotionEventSplittingEnabled
    layoutManager = getLinearLayoutVertical(context, reverseLayout, stackFromEnd)
    mListener?.let {
        addItemDecoration(StickHeaderItemDecoration(it,layoutId,spacingItem, type))
    }
}

fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = snapMode
        override fun getHorizontalSnapPreference(): Int = snapMode
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

/**
 * Apply settings for show the item list in grid
 * @param[hasFixedSize] true if adapter changes cannot affect the size of the RecyclerView.
 * @param[numOfColumns] number of columns.
 * @param[spacingItem] space between items.
 * @param[pIsNestedScrollingEnabled] true to enable nested scrolling dispatch from this view, false otherwise
 */
fun RecyclerView.initGrid(hasFixedSize: Boolean = true,
                          numOfColumns: Int = 2,
                          spacingItem: Int = 0,
                          pIsNestedScrollingEnabled: Boolean = true) {

    setHasFixedSize(hasFixedSize)
    isNestedScrollingEnabled = pIsNestedScrollingEnabled
    addItemDecoration(SpacesItemDecoration(spacingItem, 2))
    layoutManager = GridLayoutManager(context, numOfColumns)
}

fun RecyclerView.initStaggeredVerticalGrid(hasFixedSize: Boolean = true,
                                           spanCount: Int = 2,
                                           spacingItem: Int = 0,
                                           pIsNestedScrollingEnabled: Boolean = true) {

    setHasFixedSize(hasFixedSize)
    isNestedScrollingEnabled = pIsNestedScrollingEnabled
    addItemDecoration(SpacesItemDecoration(spacingItem, 2))
    layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
}

/**
 * Apply settings for show the item list in grid
 * @param[hasFixedSize] true if adapter changes cannot affect the size of the RecyclerView.
 * @param spanCount The number of columns or rows in the grid
 * @param orientation Layout orientation.
 * @param[cacheSize] Number of views to cache offscreen before returning them to the general recycled view pool
 */
fun RecyclerView.initGridSpan(hasFixedSize: Boolean = true,
                              spanCount: Int = 2,
                              orientation: Int = RecyclerView.VERTICAL,
                              cacheSize: Int = 0) {

    setHasFixedSize(hasFixedSize)
    layoutManager = GridLayoutManager(context, spanCount, orientation, false)
    if (cacheSize > 0) {
        setItemViewCacheSize(cacheSize)//
        isDrawingCacheEnabled = true
        drawingCacheQuality = android.view.View.DRAWING_CACHE_QUALITY_HIGH
    }
}

fun Serializable.serialize() :String {
    val baos = ByteArrayOutputStream()
    val oos = ObjectOutputStream(baos)
    oos.writeObject(this)
    oos.close()
    return Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT)
}

fun String.deserialize() : Any {
    val data = Base64.decode(this,Base64.DEFAULT)
    val ois = ObjectInputStream(
        ByteArrayInputStream(data))
    val o = ois.readObject()
    ois.close()
    return o
}

fun <T, K, R> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    block: (T?, K?) -> R
): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) {
        result.value = block.invoke(this.value, liveData.value)
    }
    result.addSource(liveData) {
        result.value = block.invoke(this.value, liveData.value)
    }
    return result
}