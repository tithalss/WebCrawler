package org.example

import javax.mail.*
import javax.mail.internet.*

class EmailSender {

    static def sendEmail(String to, String subject, String body, List<File> attachments) {
        def props = new Properties()
        props.put("mail.smtp.host", "smtp.example.com") // Configure o SMTP
        props.put("mail.smtp.port", "587")
        props.put("mail.smtp.auth", "true")
        props.put("mail.smtp.starttls.enable", "true")

        def session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("seu-email@example.com", "sua-senha")
            }
        })

        try {
            def message = new MimeMessage(session)
            message.setFrom(new InternetAddress("seu-email@example.com"))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            message.setSubject(subject)

            // Corpo do e-mail
            def mimeBodyPart = new MimeBodyPart()
            mimeBodyPart.setText(body)

            // Anexos
            def multipart = new MimeMultipart()
            attachments.each { file ->
                def attachmentPart = new MimeBodyPart()
                attachmentPart.attachFile(file)
                multipart.addBodyPart(attachmentPart)
            }

            multipart.addBodyPart(mimeBodyPart)
            message.setContent(multipart)

            // Envio
            Transport.send(message)

            println "Email enviado com sucesso para $to"
        } catch (MessagingException e) {
            throw new RuntimeException(e)
        }
    }
}
