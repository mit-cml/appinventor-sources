package com.google.appinventor.components.runtime;

import android.content.Intent;

import android.net.Uri;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.YailList;

/**
 * EmailSender is a non-visible component that enables sending emails from
 * your app using an email app installed on the device. The component will display a list of
 * the installed email apps that can handle the request, and will allow the user to choose
 * one to send the email with.
 * 
 * The recipient is required, while the subject, body, cc, and bcc fields are optional.
 * 
 * Be aware that the email is not sent directly from your app. The selected email app will open 
 * with the fields pre-filled, and the user will need to press send.
 * 
 * @author yashteaches@gmail.com (Yash Srivastava)
 */
@DesignerComponent(version = YaVersion.EMAIL_SENDER_COMPONENT_VERSION,
    description = "EmailSender is a non-visible component that enables sending emails from " +
    "your app using an email app installed on the device. The component will display a list of " +
    "the installed email apps that can handle the request, and will allow the user to choose " +
    "one to send the email with.<br>The recipient is required, while the " +
    "subject, body, cc, and bcc fields are optional.<br>Be aware that the email is not sent " +
    "directly from your app. The selected email app will open with the fields pre-filled, " +
    "and the user will need to press send.",
    category = ComponentCategory.SOCIAL,
    nonVisible = true, iconName = "images/emailSender.png"
)
@SimpleObject
public final class EmailSender extends AndroidNonvisibleComponent {
	public EmailSender(ComponentContainer container){
		super(container.$form());
	}

	@SimpleFunction(description = "Opens a list of available email apps installed on the phone, " +
			"allowing the user to choose one. The selected app will open with email data already " +
			"filled in. Multiple recipients, CC, and BCC addresses can be provided as a list."
	)
	public void SendEmail(
			YailList recipient, String subject, String body, YailList cc, YailList bcc) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
		emailIntent.setData(Uri.parse("mailto:"));

		emailIntent.putExtra(Intent.EXTRA_EMAIL, recipient.toStringArray());
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		emailIntent.putExtra(Intent.EXTRA_CC, cc.toStringArray());
		emailIntent.putExtra(Intent.EXTRA_BCC, bcc.toStringArray());
		
		form.startActivity(emailIntent);
	}
}
