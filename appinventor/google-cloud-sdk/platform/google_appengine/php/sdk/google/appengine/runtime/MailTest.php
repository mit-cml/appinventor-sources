<?php
/**
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Tests for Mail API on App Engine.
 *
 */

use google\appengine\base\VoidProto;
use google\appengine\MailMessage;
use google\appengine\runtime\Mail;
use google\appengine\testing\ApiProxyTestBase;

/**
 * @requires extension mailparse
 */
class MailTest extends ApiProxyTestBase {

  public function setUp() {
    parent::setUp();
    ini_set('sendmail_from', '');
    putenv('APPLICATION_ID=');
  }

  public function testSetSenderUsingIniSetting() {
    ini_set('sendmail_from', 'foo@foo.com');

    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('text');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $this->assertTrue(Mail::sendMail('bar@bar.com', 'subject', 'text'));
    $this->apiProxyMock->verify();
  }

  public function testSetSenderUsingDefaultAddress() {
    putenv('APPLICATION_ID=appid');

    $message_proto = new MailMessage();
    $message_proto->setSender('mailer@appid.appspotmail.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('text');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $this->assertTrue(Mail::sendMail('bar@bar.com', 'subject', 'text'));
    $this->apiProxyMock->verify();
  }

  /**
   * @dataProvider invalidAddressProvider
   */
  public function testInvalidAddress($headers) {
    $this->setExpectedException('PHPUnit_Framework_Error_Warning',
                                'mail(): Invalid');

    $ret = Mail::sendMail('foo@foo.com', 'subject', 'body', $headers);
    $this->assertFalse($ret);
    $this->apiProxyMock->verify();
  }

  public function invalidAddressProvider() {
    return [["From: invalid_address\r\n"],
            ["From: valid@example.com\r\n" .
             "Cc: invalid@\r\n"],
            ["From: valid@example.com\r\n" .
             "Bcc: another invalid address\r\n"]];
  }

  public function testSendSimpleMail() {
    $headers = "From: foo@foo.com\r\n";
    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('text');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $ret = Mail::sendMail('bar@bar.com', 'subject', 'text', $headers);
    $this->assertTrue($ret);
    $this->apiProxyMock->verify();
  }

  public function testSendMailUsingHeadersWithoutTrailingLinebreak() {
    $headers = "From: foo@foo.com";
    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('text');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $ret = Mail::sendMail('bar@bar.com', 'subject', 'text', $headers);
    $this->assertTrue($ret);
    $this->apiProxyMock->verify();
  }

  public function testSendMailToMultipleRecipients() {
    $to = "user@example.com, Another User <anotheruser@example.com>";
    $cc = "user2@example.com, user3@example.com";
    $bcc = "User 4 <user4@example.com>";
    $reply_to = "User5 <user5@example.com";
    $headers = "From: foo@foo.com\r\n" .
               "Cc: $cc\r\n" .
               "Bcc: $bcc\r\n" .
               "Reply-To: $reply_to\r\n";
    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo($to);
    $message_proto->addCc($cc);
    $message_proto->addBcc($bcc);
    $message_proto->setReplyTo($reply_to);
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('text');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $ret = Mail::sendMail($to, 'subject', 'text', $headers);
    $this->assertTrue($ret);
    $this->apiProxyMock->verify();
  }

  public function testSendMailWithExtraHeaders() {
    $html = "<b>html</b>";
    $headers = "From: foo@foo.com\r\n" .
               "List-Id: 12345\r\n" .
               "On-Behalf-Of: bar2@bar.com\r\n" .
               "X-Mailer: foo";  // Expected to be dropped.
    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('text');
    $header = $message_proto->addHeader();
    $header->setName('list-id');
    $header->setValue('12345');
    $header = $message_proto->addHeader();
    $header->setName('on-behalf-of');
    $header->setValue('bar2@bar.com');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $ret = Mail::sendMail('bar@bar.com', 'subject', 'text', $headers);
    $this->assertTrue($ret);
    $this->apiProxyMock->verify();
  }

  public function testSendHtmlMail() {
    $html = "<b>html</b>";
    $headers = "From: foo@foo.com\r\n" .
               "Content-Type: text/html\r\n";
    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setHtmlBody($html);
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $ret = Mail::sendMail('bar@bar.com', 'subject', $html, $headers);
    $this->assertTrue($ret);
    $this->apiProxyMock->verify();
  }

  public function testSendMultipartMail() {
    $headers = "From: foo@foo.com\r\n" .
               "Content-Type: multipart/mixed; boundary=\"a-boundary\"\r\n";
    $message = "--a-boundary\r\n" .
               "Content-Type: text/plain\r\n" .
               "\r\n" .
               "multipart mail\r\n" .
               "--a-boundary\r\n" .
               "Content-Type: text/plain\r\n" .
               "Content-Id: <first_id>\r\n" .
               "Content-Disposition: attachment; filename=\"first.txt\"\r\n" .
               "\r\n" .
               "first part in plain text\r\n" .
               "--a-boundary\r\n" .
               "Content-Type: text/plain\r\n" .
               "Content-Disposition: attachment; filename=\"second.txt\"\r\n" .
               "\r\n" .
               "second part in plain text\r\n";
               "--a-boundary--\r\n";

    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('multipart mail');
    $attachment1 = $message_proto->addAttachment();
    $attachment1->setFilename('first.txt');
    $attachment1->setData('first part in plain text');
    $attachment1->setContentId('<first_id>');
    $attachment2 = $message_proto->addAttachment();
    $attachment2->setFilename('second.txt');
    $attachment2->setData('second part in plain text');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $ret = Mail::sendMail('bar@bar.com', 'subject', $message, $headers);
    $this->assertTrue($ret);
    $this->apiProxyMock->verify();
  }

  public function testSendNestedMultipartMail() {
    $headers = "From: foo@foo.com\r\n" .
               "Content-Type: multipart/mixed; boundary=\"boundary-1\"\r\n";
    $message =
        "--boundary-1\r\n" .
        "Content-Type: multipart/alternative; boundary=\"boundary-2\"\r\n" .
        "\r\n" .
        "--boundary-2\r\n" .
        "Content-Type: text/plain\r\n" .
        "\r\n" .
        "body in text\r\n" .
        "--boundary-2\r\n" .
        "Content-Type: text/html\r\n" .
        "\r\n" .
        "<div>body in html</div>\r\n" .
        "--boundary-2--\r\n" .
        "--boundary-1\r\n" .
        "Content-Type: text/plain\r\n" .
        "Content-Disposition: attachment; filename=\"test.txt\"\r\n" .
        "\r\n" .
        "attachment in plain text\r\n";
        "--boundary-1--\r\n";

    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('body in text');
    $message_proto->setHtmlBody('<div>body in html</div>');
    $attachment1 = $message_proto->addAttachment();
    $attachment1->setFilename('test.txt');
    $attachment1->setData('attachment in plain text');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $ret = Mail::sendMail('bar@bar.com', 'subject', $message, $headers);
    $this->assertTrue($ret);
    $this->apiProxyMock->verify();
  }

  public function testSendQuotedPrintableMultipartMail() {
    $headers = "From: foo@foo.com\r\n" .
               "Content-Type: multipart/mixed; boundary=\"a-boundary\"\r\n";
    $message = "--a-boundary\r\n" .
               "Content-Type: text/plain\r\n" .
               "\r\n" .
               "multipart mail\r\n" .
               "--a-boundary\r\n" .
               "Content-Type: text/plain\r\n" .
               "Content-Transfer-Encoding: quoted-printable\r\n" .
               "Content-Disposition: attachment; filename=\"first.txt\"\r\n" .
               "\r\n" .
               "1+1=3D2, =\r\n" .
               "2+2=3D4\r\n" .
               "--a-boundary--\r\n";

    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('multipart mail');
    $attachment1 = $message_proto->addAttachment();
    $attachment1->setFilename('first.txt');
    $attachment1->setData('1+1=2, 2+2=4');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $ret = Mail::sendMail('bar@bar.com', 'subject', $message, $headers);
    $this->assertTrue($ret);
    $this->apiProxyMock->verify();
  }

  public function testSendBase64MultipartMail() {
    $headers = "From: foo@foo.com\r\n" .
               "Content-Type: multipart/mixed; boundary=\"a-boundary\"\r\n";
    $message = "--a-boundary\r\n" .
               "Content-Type: text/plain\r\n" .
               "\r\n" .
               "multipart mail\r\n" .
               "--a-boundary\r\n" .
               "Content-Type: text/plain\r\n" .
               "Content-Transfer-Encoding: base64\r\n" .
               "Content-Disposition: attachment; filename=\"first.txt\"\r\n" .
               "\r\n" .
               base64_encode('base64-encoded message') . "\r\n" .
               "--a-boundary--\r\n";


    $message_proto = new MailMessage();
    $message_proto->setSender('foo@foo.com');
    $message_proto->addTo('bar@bar.com');
    $message_proto->setSubject('subject');
    $message_proto->setTextBody('multipart mail');
    $attachment1 = $message_proto->addAttachment();
    $attachment1->setFilename('first.txt');
    $attachment1->setData('base64-encoded message');
    $response = new VoidProto();
    $this->apiProxyMock->expectCall('mail', 'Send', $message_proto, $response);

    $ret = Mail::sendMail('bar@bar.com', 'subject', $message, $headers);
    $this->assertTrue($ret);
    $this->apiProxyMock->verify();
  }

}
