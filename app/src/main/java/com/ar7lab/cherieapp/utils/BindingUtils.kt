package com.ar7lab.cherieapp.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.epoxy.EpoxyRecyclerView
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.databinding.InfoDialogWithOkayButtonBinding
import com.ar7lab.cherieapp.enums.NftArtistArtsTypeEnum
import com.ar7lab.cherieapp.enums.StatusEnum
import com.ar7lab.cherieapp.enums.TopArtistsTypeEnum
import com.ar7lab.cherieapp.ui.profile.ProfileCardLoaderListener
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.slider.ImageSliderAdapter
import com.ar7lab.cherieapp.widget.DotView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.button.MaterialButton
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*
import android.text.Spannable
import android.text.style.RelativeSizeSpan
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import kotlin.collections.ArrayList
import kotlin.random.Random
import com.ar7lab.cherieapp.base.ResetPasswordSuccessDialogListener
import com.ar7lab.cherieapp.databinding.ResetPasswordSuccessDialogBinding


// Databinding for the Material Button have loading state
@BindingAdapter(value = ["showProgress", "iconSource", "textSource"], requireAll = false)
fun MaterialButton.setShowProgress(
    showProgress: Boolean?,
    iconSource: Drawable?,
    textSource: String?,
) {
    isEnabled = showProgress != true
    icon = if (showProgress == true) {
        CircularProgressDrawable(context!!).apply {
            setStyle(CircularProgressDrawable.LARGE)
            setColorSchemeColors(ContextCompat.getColor(context!!, R.color.purple_200))
            start()
        }
    } else iconSource
    text = if (showProgress == true) "" else textSource
    if (icon != null) { // callback to redraw button icon
        icon.callback = object : Drawable.Callback {
            override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            }

            override fun invalidateDrawable(who: Drawable) {
                this@setShowProgress.invalidate()
            }

            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            }
        }
    }
}

// Databinding for ImageView to load image url
@BindingAdapter("imgUrl")
fun ImageView.loadImgUrl(imgUrl: String?) {
    Glide.with(this)
        .load(imgUrl)
        .centerCrop()
        .placeholder(R.drawable.ic_avatar_placeholder)
        .into(this)
}


// Databinding for ImageView to load image url
@BindingAdapter("loadImage")
fun ImageView.loadImgUrl(imgUrl: Any?) {
    Glide.with(this)
        .load(imgUrl)
        .centerCrop()
        .placeholder(R.drawable.ic_avatar_placeholder)
        .into(this)
}

// Databinding for ImageView to load image url
@BindingAdapter("loadImgUri")
fun ImageView.loadImgUrlPlaceHolder(imgUrl: Any?) {
    Glide.with(this)
        .load(imgUrl)
        .centerCrop()
        .into(this)
}

// Databinding for ImageView to load image url
@BindingAdapter("imgUrlWithLogo")
fun ImageView.loadImageWithDefaultLogo(imgUrl: String?) {
    Glide.with(this)
        .load(imgUrl)
        .centerCrop()
        .placeholder(R.mipmap.ic_launcher)
        .into(this)
}

// Databinding for ImageView to load image url
@BindingAdapter("imgUrlWithLoader")
fun ImageView.imgUrlWithLoader(imgUrl: String?) {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    Glide.with(this)
        .load(imgUrl)
        .centerCrop()
        .placeholder(circularProgressDrawable)
        .into(this)
}

// Databinding for ImageView to load image url
@BindingAdapter("imgUrlWithLoaderFitXY")
fun ImageView.imgUrlWithLoaderFitXY(imgUrl: String?) {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    Glide.with(this)
        .load(imgUrl)
        .centerInside()
        .placeholder(circularProgressDrawable)
        .into(this)
}

// Databinding for ImageView to load art image url
@BindingAdapter("artImgUrl")
fun ImageView.loadArtImgUrl(imgUrl: String?) {
    Glide.with(this)
        .load(imgUrl)
        .fitCenter()
        .placeholder(R.drawable.ic_placeholder_v3)
        .into(this)
}

// Databinding for ImageView to load art image url
@BindingAdapter("imgFromDrawable")
fun ImageView.loadImgFromDrawable(imgSource: Int?) {
    Glide.with(this)
        .load(imgSource)
        .fitCenter()
        .into(this)
}


// Databinding for set background resource
@BindingAdapter("bgResource")
fun View.backgroundResource(res: Int) {
    setBackgroundResource(res)
}

@BindingAdapter("app:restore")
fun ViewPager2.restoreHeight(restore: Boolean) {
    ImageSliderAdapter.iaFirstTime = true
    if (ImageSliderAdapter.globalDefaultHeight != 0 && restore) {
        (parent as ViewGroup).layoutParams.height = ImageSliderAdapter.globalDefaultHeight
        (parent as ViewGroup).requestLayout()
    }
}
//chart data & setting from using this method
@BindingAdapter("chartData")
fun LineChart.chartData(data: String) {
    //entry object
    val entries = ArrayList<Entry>()
    //static data
    (1..50).forEachIndexed { index, art ->
        entries.add(Entry(art.toFloat(), Random.nextInt(4000, 9500).toFloat()))
    }

    val vl = LineDataSet(entries, "My Type")
    val datasets = ArrayList<ILineDataSet>()
    datasets.add(vl)
    val data = LineData(datasets)
    //line width
    vl.lineWidth = 2f
    //line color
    vl.color = resources.getColor(R.color.green_munsell)
    vl.cubicIntensity = 5f
    vl.setDrawCircles(false)
    vl.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    vl.valueTextColor = Color.WHITE
    vl.valueTextSize = 0f
    //trasferency color
    vl.fillAlpha = 255
    //only left side needed
    vl.axisDependency = YAxis.AxisDependency.LEFT
    //chart fill color
    vl.fillColor = resources.getColor(R.color.gains_boro)
    vl.setDrawFilled(true)
    //legend hide
    legend.isEnabled = false
    val xAxisObject = xAxis
    xAxisObject.position = XAxis.XAxisPosition.BOTTOM
    axisLeft.isEnabled = false
    xAxisObject.valueFormatter = DateValueFormatter()
    xAxisObject.labelRotationAngle = 0f
    xAxisObject.textColor=resources.getColor(R.color.dove_gray)
    axisLeft.textColor=resources.getColor(R.color.dove_gray)
    axisRight.textColor=resources.getColor(R.color.dove_gray)
    this.data = data
    this.description.isEnabled = false
    invalidate()

}

@BindingAdapter("app:vpAdapter")
fun ViewPager2.setViewPagerAdapter(adapter: ImageSliderAdapter) {
    offscreenPageLimit = 1


    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
    })

    if (ImageSliderAdapter.globalDefaultHeight != 0 && ImageSliderAdapter.iaFirstTime) {
        ImageSliderAdapter.iaFirstTime = false
        (parent as ViewGroup).layoutParams.height = ImageSliderAdapter.globalDefaultHeight
        (parent as ViewGroup).requestLayout()
    }

    adapter.onLeftClick={
        if(currentItem!=0)
            this.currentItem = --currentItem
    }
    adapter.onRightClick={
        if(currentItem<adapter.currentList.size-1)
        {
            this.currentItem = ++currentItem
        }
    }

    setAdapter(adapter)


    // Remove item Decoration when already set
    if (itemDecorationCount > 0)
        removeItemDecorationAt(0)

    // The ItemDecoration gives the current (centered) item horizontal margin so that
    // it doesn't occupy the whole screen width. Without it the items overlap
}


@BindingAdapter("bgColor")
fun MaterialButton.backgroundColor(res: Int) {
    backgroundTintList = ContextCompat.getColorStateList(this.context, res)
}


// Databinding for time ago
@BindingAdapter("timeAgo")
fun TextView.timeAgo(timestamp: String) {
    if (timestamp.isNullOrEmpty()) return
    val prettyTime = PrettyTime(Locale.getDefault())
    val formatter = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val ago: String = prettyTime.format(formatter.parse(timestamp))
    text = ago
}

@BindingAdapter("textStyleBoldNormal")
fun TextView.textStyleBoldNormal(isBold: Boolean) {
    val typeface = ResourcesCompat.getFont(context, R.font.sf_pro_text_semi_bold)
    val medium = ResourcesCompat.getFont(context, R.font.sf_pro_text_medium)
    if (isBold)
        this.setTypeface(typeface, Typeface.BOLD)
    else
        this.setTypeface(medium, Typeface.NORMAL)
}

// Databinding for date
@BindingAdapter("showDate")
fun TextView.showDate(timestamp: String?) {
    if (timestamp.isNullOrBlank()) return
    val formatter = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val showFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    text = showFormatter.format(formatter.parse(timestamp) ?: "")
}


// Databinding for date
@BindingAdapter("showSlashDate")
fun TextView.showSlashDate(timestamp: String?) {
    if (timestamp.isNullOrBlank()) return
    val formatter = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val showFormatter = SimpleDateFormat(NEW_DATE_FORMAT, Locale.getDefault())
    text = showFormatter.format(formatter.parse(timestamp) ?: "")
}


// Databinding for date time
@BindingAdapter("showDateTime")
fun TextView.showDateTime(timestamp: String?) {
    if (timestamp.isNullOrBlank()) return
    val formatter = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val showFormatter = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
    text = showFormatter.format(formatter.parse(timestamp) ?: "")
}

@BindingAdapter("showCurrentDate")
fun TextView.showCurrentDate(timestamp: String?) {
    val showFormatter = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
    text = showFormatter.format(Date())
}
@BindingAdapter("showCurrentDateWithDayName")
fun TextView.showCurrentDateWithDayName(timestamp: String?)
{
    val showFormatter = SimpleDateFormat(DATE_TIME_FORMAT_WITH_DAY, Locale.getDefault())
    text = showFormatter.format(Date())
}

@BindingAdapter("app:setAdapter")
fun ViewPager.setAdapter(adapter: PagerAdapter) {
    this.adapter = adapter
}

@BindingAdapter("app:notify")
fun ViewPager.notify(notify: Boolean) {
    this.adapter?.notifyDataSetChanged()
}

@BindingAdapter("app:scrollViewPager")
fun ViewPager.scrollViewPager(position: Int) {
    this.setCurrentItem(position, true)
}

@BindingAdapter("app:dotCounts")
fun DotView.setDotCount(count: Int) {
    addDot(count)
}

@BindingAdapter("app:selectDot")
fun DotView.selectDot(position: Int) {
    selectDot(position)
}

@BindingAdapter("showDateWithMonthName")
fun TextView.showDateWithMonthName(timestamp: String?) {
    if (timestamp.isNullOrBlank()) return
    val formatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val showFormatter = SimpleDateFormat(DATE_FORMAT_WITH_MONTH, Locale.getDefault())
    text = showFormatter.format(formatter.parse(timestamp) ?: "")
}


//This will add the text background color as per status
@BindingAdapter("app:setStatusBackground")
fun TextView.setStatusBackground(status: String) {
    when (status) {
        StatusEnum.ON_SELL.type -> {
            this.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green)
        }
        StatusEnum.SOLD_OUT.type -> {
            this.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red_marketplace)
        }
        StatusEnum.START_SOON.type -> {
            this.backgroundTintList = ContextCompat.getColorStateList(context, R.color.dark_blue)
        }
        else -> {
            this.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green)
        }
    }
}

//This will add the text background color as per status
@BindingAdapter("app:setStatusBG")
fun TextView.setStatusBG(status: String) {
    when (status) {
        StatusEnum.SOLD_OUT.type -> {
            this.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_sold_out))
        }
        StatusEnum.START_SOON.type -> {
            this.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_start_soon))
        }
        else -> {
            this.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_on_sell))
        }
    }
}

//This will add the text color as per status
@SuppressLint("ResourceType")
@BindingAdapter("app:setStatusTextColor")
fun TextView.setStatusTextColor(status: String) {
    when (status) {
        StatusEnum.SOLD_OUT.type -> {
            this.setTextColor(ContextCompat.getColor(context, R.color.sold_out))
        }
        StatusEnum.START_SOON.type -> {
            this.setTextColor(ContextCompat.getColor(context, R.color.artist_color))
        }
        else -> {
            this.setTextColor(ContextCompat.getColor(context, R.color.green))
        }
    }
}

//This will check date is empty or not and then return the whole format
@SuppressLint("SetTextI18n", "SimpleDateFormat")
@BindingAdapter(value = ["app:artistName", "app:birthDate", "app:deathDate"], requireAll = false)
fun TextView.checkDateEmpty(artistName: String, birthDate: String, deathDate: String) {
    val bDate: List<String> = birthDate.split("-").map { it.trim() }
    val dDate: List<String> = deathDate.split("-").map { it.trim() }

    if (birthDate.isEmpty() && deathDate.isEmpty()) {
        //this.text = context.getString(R.string.artist_)+" $artistName"
        this.text = artistName
    } else if (deathDate.isEmpty()) {
        //this.text = context.getString(R.string.artist_)+ " $artistName - ${bDate[0]}"
        this.text = "$artistName - ${bDate[0]}"
    } else if (birthDate.isEmpty()) {
        //this.text = context.getString(R.string.artist_)+" $artistName - ${dDate[0]}"
        this.text = "$artistName - ${dDate[0]}"
    } else {
        //this.text = context.getString(R.string.artist_)+" $artistName - ${bDate[0]} ~ ${dDate[0]}"
        this.text = "$artistName - ${bDate[0]} ~ ${dDate[0]}"
    }
}

//This will check date is empty or not and then return the whole format
@SuppressLint("SetTextI18n", "SimpleDateFormat")
@BindingAdapter(value = ["app:artistNameNoLabel", "app:birthDateNoLabel", "app:deathDateNoLabel"], requireAll = false)
fun TextView.checkDateEmptyWithoutLabel(artistName: String, birthDate: String, deathDate: String) {
    val bDate: List<String> = birthDate.split("-").map { it.trim() }
    val dDate: List<String> = deathDate.split("-").map { it.trim() }

    if (birthDate.isEmpty() && deathDate.isEmpty()) {
        this.text = context.getString(R.string.artist_)+" $artistName"
    } else if (deathDate.isEmpty()) {
        this.text = context.getString(R.string.artist_)+ " $artistName - ${bDate[0]}"
    } else if (birthDate.isEmpty()) {
        this.text = context.getString(R.string.artist_)+" $artistName - ${dDate[0]}"
    } else {
        this.text = context.getString(R.string.artist_)+" $artistName - ${bDate[0]} ~ ${dDate[0]}"
    }
}

//This is for add the size of the art
@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["app:widthIn", "app:heightIn"], requireAll = false)
fun TextView.sizeAdd(width: Double, height: Double) {
    this.text = width.toString() + "x" + height.toString() + " in"
}


//This is for add @ for the user name in profile screen
@SuppressLint("SetTextI18n")
@BindingAdapter("app:addUserName")
fun TextView.addUserName(userName: String?) {
    if (userName != null)
        this.text = "@$userName"
}


//This is for get focus of the edit text to show and hide button
@BindingAdapter("app:onFocusChange")
fun onFocusChange(text: EditText, listener: OnFocusChangeListener?) {
    text.onFocusChangeListener = listener
}


//show session expired dialog
var alertDialog: AlertDialog? = null
fun Context.openInfoDialog(
    layoutInflater: LayoutInflater,
    infoDialog: InfoDialogOkayButtonListener,
) {
    val message = this.getString(R.string.session_expired)
    if (alertDialog != null && alertDialog?.isShowing!!) return
    val bind: InfoDialogWithOkayButtonBinding =
        InfoDialogWithOkayButtonBinding.inflate(layoutInflater)
    val builder = AlertDialog.Builder(this)
    builder.setView(bind.root)
    bind.message = message
    alertDialog = builder.create()
    alertDialog?.show()
    bind.btnOkay.setOnClickListener {
        infoDialog.onOkayButtonClicked()
        alertDialog?.dismiss()
    }

}

//show resetPassword Success dialog
var alertDialogReset: AlertDialog? = null
fun Context.openSuccessDialog(
    layoutInflater: LayoutInflater,
    infoDialog: ResetPasswordSuccessDialogListener,
) {
    val message = this.getString(R.string.session_expired)
    if (alertDialogReset != null && alertDialogReset?.isShowing!!) return
    val bind: ResetPasswordSuccessDialogBinding = ResetPasswordSuccessDialogBinding.inflate(layoutInflater)
    val builder = AlertDialog.Builder(this)
    builder.setView(bind.root)
    bind.message = message
    alertDialogReset = builder.create()
    alertDialogReset!!.setCancelable(false)
    alertDialogReset!!.show()

    bind.btnOkay.setOnClickListener {
        infoDialog.onCloseButtonClicked()
        alertDialogReset?.dismiss()
    }

}


//This is for refresh the card list
@BindingAdapter("app:load", "app:setRefresh")
fun EpoxyRecyclerView.setRefresh(callback: ProfileCardLoaderListener, type: Boolean) {
    if (type)
        callback.onLoad(this)
}


//This will check data is present or not, based on set visibility of the view
@BindingAdapter(value = ["app:onSale", "app:onOwned", "app:tabSelected", "app:typeSelected"], requireAll = false)
fun TextView.needToShowNoMore(onSale: Int, onOwned: Int, tabSelected: String, typeSelected: String) {
    if (typeSelected == TopArtistsTypeEnum.NFT.name) {
        if (tabSelected == NftArtistArtsTypeEnum.ON_SALE.name && onSale == 0) {
            this.visibility = View.VISIBLE
        } else if (tabSelected == NftArtistArtsTypeEnum.OWNED.name && onOwned == 0) {
            this.visibility = View.VISIBLE
        } else {
            this.visibility = View.GONE
        }
    } else {
        this.visibility = View.GONE
    }
}


//This is get 1 piece of price
@BindingAdapter(value = ["app:amount", "app:totalShare", "app:currency"], requireAll = false)
fun TextView.onePiece(amount: Double, totalShare: Double, currency: String) {
    val a: Double
    if (totalShare != 0.0) {
        a = amount / totalShare
        this.text = context.getString(R.string.one_piece, a.toString(), currency)
    } else {
        a = amount
        this.text = context.getString(R.string.one_piece, a.toString(), currency)
    }
}


//This is get 1 piece of price
@BindingAdapter(value = ["app:price", "app:currencySymbol"], requireAll = false)
fun TextView.priceWithSymbol(price: Double, currencySymbol: String) {
    if (currencySymbol == "$") {
        this.text = context.getString(R.string.art_full_price, "USD", currencySymbol, price.toString())
    } else {
        this.text = context.getString(R.string.art_full_price, "Japanese yen", currencySymbol, price.toString())
    }
}


@BindingAdapter("textSelection")
fun TextView.textSelection(isBold: Boolean) {
    val typeface = ResourcesCompat.getFont(context, R.font.sf_bold)
    val medium = ResourcesCompat.getFont(context, R.font.sf_medium)
    if (isBold)
        this.setTypeface(typeface, Typeface.BOLD)
    else
        this.setTypeface(medium, Typeface.NORMAL)
}

@BindingAdapter("textRegularSemibold")
fun TextView.textRegularSemiBold(isSemiBold: Boolean) {
    val typeface = ResourcesCompat.getFont(context, R.font.sf_pro_text_semi_bold)
    val medium = ResourcesCompat.getFont(context, R.font.sf_pro_text_regular)
    if (isSemiBold)
        this.setTypeface(typeface, Typeface.BOLD)
    else
        this.setTypeface(medium, Typeface.NORMAL)
}


//This will check year is empty or not and then return the whole format
@BindingAdapter(value = ["app:artistFullName", "app:releaseYear"], requireAll = false)
fun TextView.yearOfReleaseEmpty(artistFullName: String?, releaseYear: String?) {
    this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15F)
    if (releaseYear != null) {
        if (releaseYear.isEmpty()) {
            val span: Spannable = SpannableString(artistFullName)
            this.text = span
        } else {
            val artRelease = "$artistFullName $releaseYear"

            val span: Spannable = SpannableString(artRelease)
            if (artistFullName != null) {
                span.setSpan(RelativeSizeSpan(0.75f), artistFullName.length, artRelease.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.dove_gray)), artistFullName.length, artRelease.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                this.text = span
            }
        }
    }
}

/*//Here the checkout screen total price text is styled
@BindingAdapter(value = ["app:totalPriceTxt", "app:totalPrice"], requireAll = false)
fun TextView.totalPrice(totalPriceTxt: String, price: String?) {
    this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15F)
    if (price != null) {
        if (price.isEmpty()) {
            val span: Spannable = SpannableString(totalPriceTxt)
            this.text = span
        } else {
            val total = "$totalPriceTxt $price"

            val span: Spannable = SpannableString(total)
            if (totalPriceTxt != null) {
                val boldSpan = StyleSpan(Typeface.BOLD)
                span.setSpan(RelativeSizeSpan(1.10f), totalPriceTxt.length, total.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(boldSpan, totalPriceTxt.length, total.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)), totalPriceTxt.length, total.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                this.text = span
            }
        }
    }
}*/

//This will check date is empty or not and then return the whole format of the profile name with birth year.
@BindingAdapter(value = ["app:profileName", "app:profileBirth"], requireAll = false)
fun TextView.checkProfileDate(profileName: String?, profileBirth: String) {
    val bDate: List<String> = profileBirth.split("-").map { it.trim() }
    if (profileName!!.isNotEmpty())
        this.text = context.getString(R.string.full_profile_name, profileName, bDate[0])
}