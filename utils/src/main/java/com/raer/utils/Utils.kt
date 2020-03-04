package com.raer.utils

import android.content.Context
import android.graphics.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gets color with alpha.
 *
 * @param color the color
 * @param alpha the alpha
 * @return the color with alpha
 */
fun getColorWithAlpha(color: Int,@IntRange(from = 0, to = 255) alpha: Int): Int {
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    return Color.argb(alpha, r, g, b)
}

inline fun <reified T> toArray(list: List<*>): Array<T> {
    return (list as List<T>).toTypedArray()
}

fun isOnline(): Boolean {
    val runtime = Runtime.getRuntime()
    try {
        val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
        val exitValue = ipProcess.waitFor()
        return exitValue == 0
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }

    return false
}

fun resizeAndRotate(
    path: String,
    width: Int,
    height: Int,
    waterMarck: String,
    override: Boolean
): Boolean {
    val bm = modify(path, width, height, false, waterMarck, override)
    if (bm != null) {
        bm.recycle()
        return true
    } else
        return false
}

private fun modify(
    path: String?,
    width: Int,
    height: Int,
    rotate: Boolean,
    waterMarck: String?,
    override: Boolean
): Bitmap? {

    var width = width
    var height = height

    val f = File(path)
    // val exif: ExifInterface
    try {
        //  exif = ExifInterface(f.absolutePath)
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)

        //if portrait invert values
        //to preserve aspect relation
        if (options.outHeight > options.outWidth) {
            val tmp = width
            width = height
            height = tmp
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        val bitmapLoaded = BitmapFactory.decodeFile(path, options)
        var bitmap = bitmapLoaded.copy(Bitmap.Config.ARGB_8888, true)

        // rotate and scale
        if (rotate) {
            /* val orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION)
             val orientation =
                 if (orientString != null) Integer.parseInt(orientString) else ExifInterface.ORIENTATION_NORMAL
             var rotationAngle = 0
             if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90
             if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180
             if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270
             // calculate the scale
             val scaleWidth = width.toFloat() / options.outWidth
             val scaleHeight = height.toFloat() / options.outHeight

             val matrix = Matrix()
             matrix.reset()
             matrix.postScale(scaleWidth, scaleHeight)
             matrix.postRotate(rotationAngle.toFloat())

             bitmap = Bitmap.createBitmap(bitmap, 0, 0, options.outWidth, options.outHeight, matrix, true)*/
        } else {
            //just scale
            if (options.outWidth > options.outHeight)
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        }

        //compress
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(path)
            if (waterMarck != null) {
                if (!override) {
                    val canvas = Canvas(bitmap)
                    val paint = Paint()
                    paint.color = Color.RED
                    paint.textSize = 20f
                    paint.strokeWidth = 20f
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                    val texto = SimpleDateFormat("dd-MM-yy HH:mm").format(
                        Date(System.currentTimeMillis())
                    ) + " " + waterMarck
                    if (texto.length > 45) {
                        canvas.drawText(texto.substring(0, 44), 20f, 20f, paint)
                        canvas.drawText(texto.substring(44, texto.length), 20f, 50f, paint)
                    } else
                        canvas.drawText(texto, 20f, 20f, paint)

                } else {
                    val canvas = Canvas(bitmap)
                    val paint = Paint()
                    paint.color = Color.BLUE
                    paint.textSize = 20f
                    paint.strokeWidth = 20f
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                    val texto = SimpleDateFormat("dd-MM-yy HH:mm").format(
                        Date(System.currentTimeMillis())
                    ) + " FOTO ENVIADA"
                    if (texto.length > 45) {
                        canvas.drawText(texto.substring(0, 44), 20f, 80f, paint)
                        canvas.drawText(texto.substring(44, texto.length), 20f, 110f, paint)
                    } else
                        canvas.drawText(texto, 20f, 80f, paint)
                }
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            //  exif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString() + "")
            // exif.saveAttributes()
            out.flush()
            out.close()
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                out?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (e1: IOException) {
        e1.printStackTrace()
        return null
    }

}

private fun calculateInSampleSize(
    options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight = height / 2
        val halfWidth = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

//fun hasNetwork() : Boolean{
//    var haveConnectedWifi = false
//    var haveConnectedMobile = false
//    val cm = MizziAplication.application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//    val netInfo = cm.allNetworkInfo
//    for (ni in netInfo) {
//        if (ni.typeName.equals("WIFI", ignoreCase = true))
//            if (ni.isConnected)
//                haveConnectedWifi = true
//        if (ni.typeName.equals("MOBILE", ignoreCase = true))
//            if (ni.isConnected)
//                haveConnectedMobile = true
//    }
//    return haveConnectedWifi || haveConnectedMobile
//}

/**
 * Used to log long strings in debug
 *
 * @param tag     the tag
 * @param message the message
 */
fun longLogD(tag: String, message: String) {
    longLog("d",tag,message)
}

/**
 * Used to log long strings in info
 *
 * @param tag     the tag
 * @param message the message
 */
fun longLogI(tag: String, message: String) {
    longLog("i",tag,message)
}

/**
 * Used to log long strings in error
 *
 * @param tag     the tag
 * @param message the message
 */
fun longLogE(tag: String, message: String) {
    longLog("e",tag,message)
}

private fun longLog(type: String,tag: String, message: String){
    val maxLogSize = 1000
    for (i in 0..message.length / maxLogSize) {
        val start = i * maxLogSize
        var end = (i + 1) * maxLogSize
        end = if (end > message.length) message.length else end
        when(type){
            "e"->android.util.Log.e(tag, message.substring(start, end))
            "i"->android.util.Log.i(tag, message.substring(start, end))
            "d"->android.util.Log.d(tag, message.substring(start, end))
        }

    }
}

fun executeIfNotNull(vararg objects : Any?, fnc :() -> Unit) = objects.find { it == null } ?: fnc.invoke()


open class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}

/**
 * Class used as animation object for the [Extensions.SlideView] methods.
 */
class ResizeHeightAnimation
/**
 * Instantiates a new Resize height animation.
 *
 * @param view the view
 * @param targetHeight  the targetHeight
 */
    (
    /**
     * The View.
     */
    internal var view: View,
    /**
     * The Target height.
     */
    internal var targetHeight: Int
) : Animation() {
    /**
     * The Start height.
     */
    internal var startHeight: Int = 0

    init {
        this.startHeight = view.height
    }

    public override fun applyTransformation(
        param1Float: Float,
        param1Transformation: Transformation
    ) {
        val i = (this.startHeight + (this.targetHeight - this.startHeight) * param1Float).toInt()
        this.view.layoutParams.height = i
        this.view.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}

class ClickListenerWrapper : View.OnClickListener {
    private val listener1 : View.OnClickListener?
    private val listener2 : ((v: View)->Unit)?
    private var EVENT_CONSUMPTION_WINDOW : Long = 1000L

    private var lastClickTimestamp : Long = 0

    constructor(listener : View.OnClickListener, time : Long = 1000L){
        this.listener1 = listener
        this.listener2 = null
        this.EVENT_CONSUMPTION_WINDOW = time
    }

    constructor(listener : (v: View)->Unit, time : Long = 1000L){
        this.listener1 = null
        this.listener2 = listener
        this.EVENT_CONSUMPTION_WINDOW = time
    }

    override fun onClick(v: View) {
        val timestamp = System.currentTimeMillis()

        if (canConsumeClickAt(timestamp)) {
            listener1?.onClick(v)
            listener2?.invoke(v)

            lastClickTimestamp = timestamp
        }
    }

    private fun canConsumeClickAt(timestamp: Long): Boolean = (timestamp - lastClickTimestamp) >= EVENT_CONSUMPTION_WINDOW

}

fun getLinearLayoutHorizontal(context: Context, reverseLayout: Boolean, stackFromEnd: Boolean) =
    object : LinearLayoutManager(context) {
        override fun requestChildRectangleOnScreen(parent: RecyclerView, child: View, rect: Rect, immediate: Boolean): Boolean {
            return false
        }
    }.apply {
        this.reverseLayout = reverseLayout
        this.stackFromEnd = stackFromEnd
    }

fun getLinearLayoutVertical(context: Context, reverseLayout: Boolean, stackFromEnd: Boolean) =
    object : LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    {
        override fun requestChildRectangleOnScreen(parent: RecyclerView, child: View, rect: Rect, immediate: Boolean): Boolean {
            return false
        }
    }.apply {
        this.reverseLayout = reverseLayout
        this.stackFromEnd = stackFromEnd
    }


class SpacesItemDecoration(private val mSpace: Int, private val type: Int) : RecyclerView.ItemDecoration() {


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        when (type) {
            1 -> {
                outRect?.bottom = mSpace
            }
            2 ->{
                outRect?.left = mSpace
                outRect?.right = mSpace
                outRect?.bottom=mSpace
            }
            3->{
                outRect?.left=mSpace
                outRect?.right=mSpace
            }
            4->{
                outRect?.bottom=mSpace
            }
            else ->{
                outRect?.left = mSpace
                outRect?.right = mSpace
                outRect?.bottom=mSpace
            }

        }

        // Add top margin only for the first item to avoid double space between items
        //if (parent?.getChildAdapterPosition(view) == 0)
        //  outRect?.top = mSpace
    }

}

class StickHeaderItemDecoration(@param:NonNull private val mListener: StickyHeaderInterface, @LayoutRes private val layoutId: Int, private val mSpace: Int, private val type: Int) :
    RecyclerView.ItemDecoration() {
    private var mStickyHeaderHeight: Int = 0

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val topChild = parent.getChildAt(0) ?: return

        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val headerPos = mListener.getHeaderPositionForItem(topChildPosition)
        if(headerPos == -1)
            return
        val currentHeader = getHeaderViewForItem(headerPos, parent)
        fixLayoutSize(parent, currentHeader)
        val contactPoint = currentHeader.bottom
        val childInContact = getChildInContact(parent, contactPoint, headerPos)

        if (childInContact != null && mListener.isHeader(
                parent.getChildAdapterPosition(
                    childInContact
                )
            )
        ) {
            moveHeader(c, currentHeader, childInContact)
            return
        }

        drawHeader(c, currentHeader)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        when (type) {
            1 -> {
                outRect?.bottom = mSpace
            }
            2 ->{
                outRect?.left = mSpace
                outRect?.right = mSpace
                outRect?.bottom=mSpace
            }
            3->{
                outRect?.left=mSpace
                outRect?.right=mSpace
            }
            4->{
                outRect?.bottom=mSpace
            }
            else ->{
                outRect?.left = mSpace
                outRect?.right = mSpace
                outRect?.bottom=mSpace
            }

        }

        // Add top margin only for the first item to avoid double space between items
        //if (parent?.getChildAdapterPosition(view) == 0)
        //  outRect?.top = mSpace
    }

    private fun getHeaderViewForItem(headerPosition: Int, parent: RecyclerView): View {
        val header : ViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),layoutId,parent,false)
        mListener.bindHeaderData(header, headerPosition)
        return header.root
    }

    private fun drawHeader(c: Canvas, header: View) {
        c.save()
        c.translate(0f, 0f)
        header.draw(c)
        c.restore()
    }

    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View) {
        c.save()
        c.translate(0f, (nextHeader.top - currentHeader.height).toFloat())
        currentHeader.draw(c)
        c.restore()
    }

    private fun getChildInContact(
        parent: RecyclerView,
        contactPoint: Int,
        currentHeaderPos: Int
    ): View? {
        var childInContact: View? = null
        for (i in 0 until parent.childCount) {
            var heightTolerance = 0
            val child = parent.getChildAt(i)

            //measure height tolerance with child if child is another header
            if (currentHeaderPos != i) {
                val isChildHeader = mListener.isHeader(parent.getChildAdapterPosition(child))
                if (isChildHeader) {
                    heightTolerance = mStickyHeaderHeight - child.height
                }
            }

            //add heightTolerance if child top be in display area
            val childBottomPosition: Int
            if (child.top > 0) {
                childBottomPosition = child.bottom + heightTolerance
            } else {
                childBottomPosition = child.bottom
            }

            if (childBottomPosition > contactPoint) {
                if (child.top <= contactPoint) {
                    // This child overlaps the contactPoint
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }


    /**
     * Properly measures and layouts the top sticky header.
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private fun fixLayoutSize(parent: ViewGroup, view: View) {

        // Specs for parent (RecyclerView)
        val widthSpec =
            View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED)

        // Specs for children (headers)
        val childWidthSpec = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.getPaddingLeft() + parent.getPaddingRight(),
            view.layoutParams.width
        )
        val childHeightSpec = ViewGroup.getChildMeasureSpec(
            heightSpec,
            parent.getPaddingTop() + parent.getPaddingBottom(),
            view.layoutParams.height
        )

        view.measure(childWidthSpec, childHeightSpec)

        mStickyHeaderHeight = view.measuredHeight
        view.layout(0, 0, view.measuredWidth, mStickyHeaderHeight)
    }

    interface StickyHeaderInterface{

        /**
         * This method gets called by [StickHeaderItemDecoration] to fetch the position of the header item in the adapter
         * that is used for (represents) item at specified position.
         * @param itemPosition int. Adapter's position of the item for which to do the search of the position of the header item.
         * @return int. Position of the header item in the adapter.
         */
        fun getHeaderPositionForItem(itemPosition: Int): Int

        /**
         * This method gets called by [StickHeaderItemDecoration] to setup the header View.
         * @param header View. Header to set the data on.
         * @param headerPosition int. Position of the header item in the adapter.
         */
        fun bindHeaderData(header: ViewDataBinding, headerPosition: Int)

        /**
         * This method gets called by [StickHeaderItemDecoration] to verify whether the item represents a header.
         * @param itemPosition int.
         * @return true, if item at the specified adapter's position represents a header.
         */
        fun isHeader(itemPosition: Int): Boolean
    }
}
