package extensions

import org.apache.commons.mail.SimpleEmail
import play.api.Logger
import play.api.Play.current

/**
 * Email sending
 *
 * TODO consider replacing this with one of the Typesafe-maintained libraries
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

private[extensions] case class MailBuilder(subject: String, content: String = "", from: String, to: Seq[String] = Seq.empty, bcc: Seq[String] = Seq.empty, cc: Seq[String] = Seq.empty) {

  val hostName = current.configuration.getString("mail.smtp.host").getOrElse("")
  val smtpPort = current.configuration.getInt("mail.smtp.port").getOrElse(25)
  val mailerType = current.configuration.getString("mail.smtp.type").getOrElse("mock")


  def to(to: String*): MailBuilder = this.copy(to = this.to ++ to)
  def cc(cc: String*): MailBuilder = this.copy(cc = this.cc ++ cc)
  def bcc(bcc: String*): MailBuilder = this.copy(bcc = this.bcc ++ bcc)

  def withContent(content: String) = this.copy(content = content)

  def send() {

    if(mailerType == "mock") {

      val mail = """

      ~~~~ Mock mailer ~~~~~
      Mail from: %s
      Mail to:   %s
      Mail CC:   %s
      BCC:       %s

      Subject:   %s

      %s
      ~~~~~~~~~~~~~~~~~~~~~~

      """.format(from, to.mkString(", "), cc.mkString(", "), bcc.mkString(", "), subject, content)

      Logger("Email").info(mail)

    } else {

      val email = new SimpleEmail
      email.setHostName(hostName)
      email.setSmtpPort(smtpPort)
      email.setFrom(from)
      to foreach(email.addTo(_))
      cc foreach(email.addCc(_))
      bcc foreach(email.addBcc(_))
      email.setSubject(subject)
      email.setMsg(content)

      email.send()
    }

  }

}

object Email {

  def apply(from: String, subject: String): MailBuilder = new MailBuilder(from = from, subject = subject)
}
