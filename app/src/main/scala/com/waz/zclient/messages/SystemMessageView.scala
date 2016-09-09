package com.waz.zclient.messages

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.{Canvas, Paint}
import android.util.AttributeSet
import android.view.View.MeasureSpec
import android.view.{View, ViewGroup}
import com.waz.ZLog.ImplicitTag._
import com.waz.utils.returning
import com.waz.zclient.ui.text.{GlyphTextView, TypefaceTextView}
import com.waz.zclient.ui.utils.TextViewUtils
import com.waz.zclient.utils.ContextUtils._
import com.waz.zclient.{R, ViewHelper}

/**
  * View implementing system message layout: row containing icon, text and expandable line.
  * By hard-coding layout logic in this class we can avoid using complicated view hierarchies.
  */
class SystemMessageView(context: Context, attrs: AttributeSet, style: Int) extends ViewGroup(context, attrs, style) with ViewHelper {
  def this(context: Context, attrs: AttributeSet) = this(context, attrs, 0)
  def this(context: Context) = this(context, null, 0)

  inflate(R.layout.system_message_content)

  val end = getDimenPx(R.dimen.wire__margin__huge)
  val start = getDimenPx(R.dimen.content__separator__avatar_container__width)
  val locale = getLocale

  val iconMargin = getDimenPx(R.dimen.error__image__margin)
  val textMargin = getDimenPx(R.dimen.wire__padding__12)
  val paddingTop = getDimenPx(R.dimen.wire__padding__small)
  val stroke = getDimenPx(R.dimen.wire__divider__height__thin)
  val color = {
    val a = context.obtainStyledAttributes(Array(R.attr.wireDividerColor))
    returning(a.getColor(0, getColor(R.color.separator_dark))) { _ => a.recycle() }
  }
  val paint = returning(new Paint()) { p =>
    p.setColor(color)
    p.setStrokeWidth(stroke)
  }

  val iconView: GlyphTextView = findById(R.id.gtv__system_message__icon)
  val textView: TypefaceTextView = findById(R.id.ttv__system_message__text)

  setWillNotDraw(false)

  def setText(text: String) = {
    textView.setText(text.toUpperCase(locale))
    TextViewUtils.boldText(textView)
  }

  def setIcon(drawable: Drawable) = {
    iconView.setText("")
    iconView.setBackground(drawable)
  }

  def setIcon(resId: Int) = {
    iconView.setText("")
    iconView.setBackgroundResource(resId)
  }

  def setIconGlyph(glyph: String) = {
    iconView.setBackground(null)
    iconView.setText(glyph)
  }

  def setIconGlyph(resId: Int) = {
    iconView.setBackground(null)
    iconView.setText(resId)
  }

  override def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): Unit = {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    val width = View.getDefaultSize(getSuggestedMinimumWidth, widthMeasureSpec)

    iconView.measure(MeasureSpec.makeMeasureSpec(start, MeasureSpec.AT_MOST), heightMeasureSpec)
    textView.measure(MeasureSpec.makeMeasureSpec(width - start - end - textMargin, MeasureSpec.AT_MOST), heightMeasureSpec)

    setMeasuredDimension(width, math.max(iconView.getMeasuredHeight, textView.getMeasuredHeight) + getPaddingTop + getPaddingBottom)
  }

  override def onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int): Unit = {

    var iconLeft = start - iconView.getMeasuredWidth - iconMargin
    var textLeft = start

    if (getLayoutDirection == View.LAYOUT_DIRECTION_RTL) {
      val w = getWidth
      iconLeft = w - start + iconMargin
      textLeft = w - start - textView.getMeasuredWidth
    }

    iconView.layout(iconLeft, 0, iconLeft + iconView.getMeasuredWidth, iconView.getMeasuredHeight)
    textView.layout(textLeft, 0, textLeft + textView.getMeasuredWidth, textView.getMeasuredHeight)
  }

  override def onDraw(canvas: Canvas): Unit = {
    super.onDraw(canvas)

    if (textView.getText.length() > 0) {
      val y = paddingTop + stroke / 2f
      val w = getWidth - start - textView.getWidth - textMargin
      val l = if (getLayoutDirection == View.LAYOUT_DIRECTION_RTL) 0 else getWidth - w
      canvas.drawLine(l, y, l + w, y, paint)
    }
  }
}
