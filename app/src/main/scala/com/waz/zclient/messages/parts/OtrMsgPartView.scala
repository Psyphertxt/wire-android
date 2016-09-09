package com.waz.zclient.messages.parts

import android.content.Context
import android.util.AttributeSet
import com.waz.model.{MessageContent, MessageData, UserId}
import com.waz.service.ZMessaging
import com.waz.threading.Threading
import com.waz.utils.events.Signal
import com.waz.zclient.messages.{MessageViewPart, MsgPart, SystemMessageView}
import com.waz.zclient.utils.ContextUtils._
import com.waz.zclient.{R, ViewHelper}

class OtrMsgPartView(context: Context, attrs: AttributeSet, style: Int) extends SystemMessageView(context, attrs, style) with MessageViewPart with ViewHelper {
  def this(context: Context, attrs: AttributeSet) = this(context, attrs, 0)
  def this(context: Context) = this(context, null, 0)

  import com.waz.api.Message.Type._

  override val tpe = MsgPart.Location

  val zMessaging = inject[Signal[ZMessaging]]
  val message = Signal[MessageData]()

  val msgType = message.map(_.msgType)

  private def userDisplayName(zms: ZMessaging, id: UserId) =
    if (zms.selfUserId == id) Signal const getString(R.string.content__system__you)
    else zms.users.userSignal(id).map(_.getDisplayName)

  val userName = for {
    zms <- zMessaging
    msg <- message
    name <- userDisplayName(zms, msg.userId)
  } yield name

  val memberNames = for {
    zms <- zMessaging
    msg <- message
    names <- Signal.sequence(msg.members.toSeq.sortBy(_.str).map { userDisplayName(zms, _) }: _*)
  } yield names.mkString(", ")

  val shieldIcon = msgType map {
    case OTR_ERROR | OTR_IDENTITY_CHANGED | HISTORY_LOST  => Some(R.drawable.red_alert)
    case OTR_VERIFIED                                     => Some(R.drawable.shield_full)
    case OTR_UNVERIFIED | OTR_DEVICE_ADDED                => Some(R.drawable.shield_full)
    case STARTED_USING_DEVICE                             => None
    case _                                                => None
  }

  val msgString = msgType flatMap {
    case HISTORY_LOST         => Signal const getString(R.string.content__otr__lost_history)
    case STARTED_USING_DEVICE => Signal const getString(R.string.content__otr__start_this_device__message)
    case OTR_VERIFIED         => Signal const getString(R.string.content__otr__all_fingerprints_verified)
    case OTR_ERROR            => userName map { getString(R.string.content__otr__message_error, _) }
    case OTR_IDENTITY_CHANGED => userName map { getString(R.string.content__otr__identity_changed_error, _) }
    case OTR_UNVERIFIED       => memberNames map { getString(R.string.content__otr__unverified_device__message, _) }
    case OTR_DEVICE_ADDED     => memberNames map { getString(R.string.content__otr__added_new_device__message, _) }
    case _                    => Signal const ""
  }

  shieldIcon.on(Threading.Ui) {
    case None       => setIcon(null)
    case Some(icon) => setIcon(icon)
  }

  msgString.on(Threading.Ui) { setText }

  override def set(pos: Int, msg: MessageData, part: Option[MessageContent], widthHint: Int): Unit = {
    message ! msg
  }
}
