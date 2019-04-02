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
 * Allow users to send mail using the App Engine mail APIs.
 *
 */

namespace google\appengine\runtime;

use google\appengine\api\app_identity\AppIdentityService;
use google\appengine\api\mail\Message;
use google\appengine\util\ArrayUtil;
use google\appengine\util\StringUtil;

final class Mail {

  // The format string for the default sender address.
  const DEFAULT_SENDER_ADDRESS_FORMAT = 'mailer@%s.appspotmail.com';

  /**
   * Send an email.
   *
   * This is a re-implementation of PHP's mail() function using App Engine
   * mail API. The function relies on mailparse extension to parse emails.
   *
   * @param string $to Receiver, or receivers of the mail.
   * @param string $subject Subject of the email to be sent.
   * @param string $message Message to be sent.
   * @param string $additional_headers optional
   *   String to be inserted at the end of the email header.
   * @param string $additional_parameters optional
   *   Additional flags to be passed to the mail program. This arugment is
   *   added only to match the signature of PHP's mail() function. The value is
   *   always ignored.
   * @return bool
   *   TRUE if the message is sent successfully, otherwise return FALSE.
   *
   * @see http://php.net/mail
   */
  public static function sendMail($to,
                                  $subject,
                                  $message,
                                  $additional_headers = null,
                                  $additional_parameters = null) {
    $raw_mail = "To: $to\r\nSubject: $subject\r\n";
    if ($additional_headers != null) {
      $raw_mail .= trim($additional_headers);
    }
    $raw_mail .= "\r\n\r\n$message";

    $mime = mailparse_msg_create();
    mailparse_msg_parse($mime, $raw_mail);
    $root_part = mailparse_msg_get_part_data($mime);

    // Set sender address based on the following order
    // 1. "From" header in $additional_headers
    // 2. "sendmail_from" ini setting
    // 3. Default address "mailer@<app-id>.appspotmail.com
    $from = ini_get('sendmail_from');
    if (isset($root_part['headers']['from'])) {
      $from = $root_part['headers']['from'];
    }
    if ($from === false || $from == "") {
      $from = sprintf(self::DEFAULT_SENDER_ADDRESS_FORMAT,
                      AppIdentityService::getApplicationId());
      syslog(LOG_WARNING,
             "mail(): Unable to determine sender's email address from the " .
             "'sendmail_from' directive in php.ini or from the 'From' " .
             "header. Falling back to the default $from.");
    }

    $email = new Message();
    try {
      $email->setSender($from);
      $email->addTo($root_part['headers']['to']);
      if (isset($root_part['headers']['cc'])) {
        $email->AddCc($root_part['headers']['cc']);
      }
      if (isset($root_part['headers']['bcc'])) {
        $email->AddBcc($root_part['headers']['bcc']);
      }
      if (isset($root_part['headers']['reply-to'])) {
        $email->setReplyTo($root_part['headers']['reply-to']);
      }
      $email->setSubject($root_part['headers']['subject']);
      $parts = mailparse_msg_get_structure($mime);
      if (count($parts) > 1) {
        foreach ($parts as $part_id) {
          $part = mailparse_msg_get_part($mime, $part_id);
          self::parseMimePart($part, $raw_mail, $email);
        }
      } else if ($root_part['content-type'] == 'text/plain') {
        $email->setTextBody($message);
      }  else if ($root_part['content-type'] == 'text/html') {
        $email->setHtmlBody($message);
      }
      $extra_headers = array_diff_key($root_part['headers'], array_flip([
          'from', 'to', 'cc', 'bcc', 'reply-to', 'subject', 'content-type']));
      foreach ($extra_headers as $key => $value) {
        try {
          $email->addHeader($key, $value);
        } catch (\InvalidArgumentException $e) {
          syslog(LOG_WARNING, "mail:() Dropping disallowed email header $key");
        }
      }
      $email->send();
    } catch (\Exception $e) {
      trigger_error('mail(): ' . $e->getMessage(), E_USER_WARNING);
      return false;
    }

    return true;
  }

  /**
   * Parse a MIME part and set the Message object accordingly.
   *
   * @param resource $part A MIME part, returned from mailparse_msg_get_part,
   *    to be parse.
   * @param string $raw_mail The string holding the raw content of the email
   *    $part is extracted from.
   * @param Message& $email The Message object to be set.
   */
  private static function parseMimePart($part, $raw_mail, &$email) {
    $data = mailparse_msg_get_part_data($part);
    $type = ArrayUtil::findByKeyOrDefault($data, 'content-type', 'text/plain');

    $start = $data['starting-pos-body'];
    $end = $data['ending-pos-body'];
    $encoding = ArrayUtil::findByKeyOrDefault($data, 'transfer-encoding', '');
    $content = self::decodeContent(substr($raw_mail, $start, $end - $start),
                                   $encoding);

    if (isset($data['content-disposition'])) {
      $filename = ArrayUtil::findByKeyOrDefault(
          $data, 'disposition-filename', uniqid());
      $content_id = ArrayUtil::findByKeyOrNull($data, 'content-id');
      if ($content_id != null) {
        $content_id = "<$content_id>";
      }
      $email->addAttachment($filename, $content, $content_id);
    } else if ($type == 'text/html') {
      $email->setHtmlBody($content);
    } else if ($type == 'text/plain') {
      $email->setTextBody($content);
    } else if (!StringUtil::startsWith($type, 'multipart/')) {
      trigger_error("Ignore MIME part with unknown Content-Type $type. " .
                    "Did you forget to specifcy Content-Disposition header?",
                    E_USER_WARNING);
    }
  }

  /**
   * Decoded content based on the encoding scheme.
   *
   * @param string $content The content to be decoded.
   * @param string $scheme The encoding shceme used. Currently only supports
   *    'base64' and 'quoted-printable'.
   * @return string The deocded content if the encoding scheme is supported,
   *    otherwise returns the original content.
   */
  private static function decodeContent($content, $encoding) {
    switch (strtolower($encoding)) {
      case 'base64':
        return base64_decode($content);
      case 'quoted-printable':
        return quoted_printable_decode($content);
      default:
        return $content;
    }
  }
}
