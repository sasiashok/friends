package org.kp.utils

/**
 * Send hipchat notification
 * @param message message to be sent
 * @param color color coding for message
 * @param room room to which the message is to be sent to.
 * @return
 */
def notifyHipChat(message, color = 'GREEN', room = 'someRoom') {
	String token = 'sometoken'
	String sendAs = 'Jenkins'
	if (color == "pass") {
		color = "GREEN"
	}
	if (color == "fail") {
		color = "RED"
	}
	if (color == "info") {
		color = "PURPLE"
	}
	hipchatSend color: "${color}", message: "${message}", room: "${room}", sendAs: "${sendAs}", server: 'hipchat.kp.org', token: "${token}", v2enabled: true
}

/**
 * Send email
 * @param subject Subject of the email
 * @param recipients Recipients of the email
 * @param body Body of the email
 * @return
 */
def sendMail(subject, recipients, body) {
	if(recipients != null) {
		try {
			String applicationName = new ApplicationUtils().getApplicationName() + "/" + env.BRANCH_NAME
			mail body: "Jenkins job status \n\n " + applicationName.toString().trim() + " job: \n"+ body.toString().trim() + " \n\n Build URL: ${env.BUILD_URL} \n\n ~ Jenkins Build Master" ,
							from: 'Jenkins Build Master <ESBDevOp@domino.kp.org>',
							replyTo: 'ESBDevOp@domino.kp.org',
							subject: applicationName.toString().trim() + " " + subject.toString().trim(),
							to: recipients.toString().trim().replaceAll("\\s","")
		} catch (e) {
			echo "ℹ️ [INFO] Unable to send email. Error for reference: " + e.getMessage()
		}
	}
}