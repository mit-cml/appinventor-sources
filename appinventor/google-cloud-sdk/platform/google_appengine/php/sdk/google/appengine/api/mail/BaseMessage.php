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
 */

namespace google\appengine\api\mail;

use google\appengine\MailAttachment;
use google\appengine\MailHeader;
use google\appengine\MailMessage;
use google\appengine\MailServiceError\ErrorCode;

/**
 * Abstract base class for sending mail using the App Engine mail APIs.
 */
abstract class BaseMessage {
  // Force AdminMessage and Message to implement send.
  abstract public function send();

  /*
   * Force AdminMessage and Message to implment getFunctionArray, required
   * for parsing the options array.
   */
  abstract protected function getFunctionArray();

  // Container for the MailMessage protobuf.
  protected $message = null;

  // Whitelisted headers.
  protected static $allowed_headers = array(
      'auto-submitted', 'in-reply-to', 'list-id', 'list-unsubscribe',
      'on-behalf-of', 'references', 'resent-date', 'resent-from', 'resent-to');

  // Blacklisted extension types.
  protected static $extension_blacklist = array(
      'ade', 'adp', 'bat', 'chm', 'cmd', 'com', 'cpl', 'exe', 'hta', 'ins',
      'isp', 'jse', 'lib', 'mde', 'msc', 'msp', 'mst', 'pif', 'scr', 'sct',
      'shb', 'sys', 'vb', 'vbe', 'vbs', 'vxd', 'wsc', 'wsf', 'wsh');

  // Setter functions for constructor.
  protected static $set_functions = array('sender' => 'setSender',
                                        'replyto' => 'setReplyTo',
                                        'to' => 'addTo',
                                        'cc' => 'addCc',
                                        'bcc' => 'addBcc',
                                        'subject' => 'setSubject',
                                        'textBody' => 'setTextBody',
                                        'htmlBody' => 'setHtmlBody',
                                        'ampHtmlBody' => 'setAmpHtmlBody',
                                        'header' => 'addHeaderArray',
                                        'attachment' => 'addAttachmentArray',
                                        'attachments' => 'addAttachmentsArray');

  /**
   * Construct an instance of Message.
   *
   * @param array $options Options for message content, key as per set_functions
   * shown above, value to be set.
   * @throws \InvalidArgumentException If the options variable passed was not an
   * array, if an invalid option was set in the options array, or if a value
   * to be set by the options array was invalid.
   */
  public function __construct($options = null) {
    $this->message = new MailMessage();

    if (isset($options)) {
      if (is_array($options)) {
        foreach($options as $key => $value) {
          // If this is a valid option to set.
          $allowed_functions = $this->getFunctionArray();
          if (array_key_exists($key, $allowed_functions)) {
            // Call the corresponding setter function with the input argument.
            $func_name = $allowed_functions[$key];
            call_user_func(array($this, $func_name), $value);
          } else {
            $error = sprintf("Message received an invalid option: %s",
                             htmlspecialchars($key));
            throw new \InvalidArgumentException($error);
          }
        }
      } else {
        $error = sprintf("Message expects an array, not %s", gettype($options));
        throw new \InvalidArgumentException($error);
      }
    }
  }

  /**
   * Adds an attachment to the Message object.
   *
   * @param string $filename Filename of the attachment.
   * @param mixed $data File data of the attachment.
   * @param string $content_id Optional Content-ID header value of the
   * attachment. Must be enclosed by angle brackets (<>).
   * @throws \InvalidArgumentException If the input is not an array or if the
   * attachment type is invalid (i.e. the filename is not a string, or the
   * file extension is blacklisted).
   */
  public function addAttachment($filename, $data, $content_id = null) {
    $this->addAttachmentsArray([["name" => $filename,
                                 "data" => $data,
                                 "content_id" => $content_id]]);
  }

  /**
   * Adds an array of attachments to the Message object.
   *
   * @param array Attachments as filename => data pairs.
   *    Example: array("filename.txt" => "This is the file contents.");
   * @throws \InvalidArgumentException If the input is not an array or if the
   * attachment type is invalid (i.e. the filename is not a string, or the
   * file extension is blacklisted).
   * @deprecated
   */
  public function addAttachmentArray($attach_array) {
    syslog(LOG_WARNING, "Both \$options['attachment'] and " .
        "addAttachmentArray() are deprecated. Use \$options['attachments'] " .
        "and addAttachmentsArray() instead.");
    if (!is_array($attach_array)) {
      $error = sprintf("Input is not an array (Actual type: %s).",
                       gettype($attach_array));
      throw new \InvalidArgumentException($error);
    }

    $error = "";
    foreach($attach_array as $filename => $data) {
      if (!$this->checkValidAttachment($filename, $error)) {
        throw new \InvalidArgumentException($error);
      }
    }

    foreach($attach_array as $filename => $data) {
      $new_attachment = $this->message->addAttachment();
      $new_attachment->setFilename($filename);
      $new_attachment->setData($data);
    }
  }

  /**
   * Adds an array of attachments to the Message object.
   *
   * @param array Array of arrays that represent attachments. Each attachment
   * array supports name, data, and (optionally) content_id keys.  Example:
   * [['name' => 'foo.jpg', 'data' => 'data', 'content_id' => '<foo>']]
   * @throws \InvalidArgumentException If the input is not an array or if the
   * attachment type is invalid (i.e. the filename is not a string, or the
   * file extension is blacklisted).
   */
  public function addAttachmentsArray($attach_array) {
    if (!is_array($attach_array)) {
      throw new \InvalidArgumentException(sprintf(
        "Input is not an array (Actual type: %s).", gettype($attach_array)));
    }

    $error = "";
    foreach($attach_array as $attachment) {
      if (!$this->checkValidAttachment($attachment["name"], $error)) {
        throw new \InvalidArgumentException($error);
      }
    }

    foreach($attach_array as $attachment) {
      $new_attachment = $this->message->addAttachment();
      $new_attachment->setFilename($attachment["name"]);
      $new_attachment->setData($attachment["data"]);
      if (isset($attachment["content_id"])) {
        if (!preg_match("/^\<.*\>$/", $attachment["content_id"])) {
          throw new \InvalidArgumentException(
            "Content-id must begin and end with angle brackets.");
        }
        $new_attachment->setContentId($attachment["content_id"]);
      }
    }
  }

  /**
   * Adds a header pair to the mail object.
   *
   * @param string $key Header name (from the whitelist) to be added.
   * @param string $value Header value to be added.
   * @throws \InvalidArgumentException If the header is not on the whitelist, or
   * if the header is invalid (i.e. not a string).
   */
  public function addHeader($key, $value) {
    if (!is_string($key)) {
      $error = sprintf("Header key is not a string (Actual type: %s).",
                       gettype($key));
      throw new \InvalidArgumentException($error);
    }
    $this->addHeaderArray(array($key => $value));
  }

  /**
   * Adds an array of headers to the mail object.
   *
   * @param array An array of headers.
   * @throws \InvalidArgumentException If the input is not an array, or if
   * headers are not on the whitelist, or if a header is invalid
   * (i.e. not a string).
   */
  public function addHeaderArray($header_array) {
    if (!is_array($header_array)) {
      $error = sprintf("Input is not an array (Actual type: %s).",
                       gettype($header_array));
      throw new \InvalidArgumentException($error);
    }

    $error = "";
    foreach($header_array as $key => $value) {
      if (!$this->checkValidHeader($key, $value, $error)) {
        throw new \InvalidArgumentException($error);
      }
    }

    foreach($header_array as $key => $value) {
      $new_header = $this->message->addHeader();
      $new_header->setName($key);
      $new_header->setValue($value);
    }
  }

  /**
   * Checks that an attachment is valid.
   *
   * @param string $filename Filename of the attachment.
   * @return bool True if successful, false otherwise.
   * @param string &$error Error message to be set if the header is invalid.
   */
  protected function checkValidAttachment($filename, &$error) {
    if (!is_string($filename)) {
      $error = sprintf("Filename must be a string but was type %s",
                       gettype($filename));
      return false;
    }

    $path_parts = pathinfo($filename);
    if (isset($path_parts['extension'])) {
      if (in_array($path_parts['extension'], self::$extension_blacklist)) {
        $error = sprintf("'%s' is a blacklisted file extension.",
                         $path_parts['extension']);
        return false;
      }
    }

    return true;
  }

  /**
   * Checks that an email is valid using the mailparse extension if available.
   *
   * @param string $email The email to be validated.
   * @return bool True if valid, false otherwise.
   */
  protected function checkValidEmail($email) {
    if (function_exists('mailparse_rfc822_parse_addresses')) {
      $parsed = mailparse_rfc822_parse_addresses($email);
      if (count($parsed) > 0 && array_key_exists('address', $parsed[0])) {
        $email = $parsed[0]['address'];
      } else {
        return false;
      }
    }
    return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
  }

  /**
   * Check validity of a header pair.
   *
   * @param string $key Header key.
   * @param string $value Header value.
   * @param string &$error Error message to be set if the header is invalid.
   * @return bool True if successful, false otherwise.
   */
  protected function checkValidHeader($key, $value, &$error) {
    if (!is_string($key)) {
      $error = sprintf("Header key is not a string (Actual type: %s).",
                       gettype($key));
      return false;
    } else if (!in_array(strtolower($key), self::$allowed_headers)) {
      // Array keys don't have consistent case.
      $error = sprintf("Input header '%s: %s' is not whitelisted for use with" .
                       " the Google App Engine Mail Service.",
                       htmlspecialchars($key),
                       htmlspecialchars($value));
      return false;
    }
    return true;
  }

  /**
   * Clear all attachments from the mail object.
   */
  public function clearAttachments() {
    $this->message->clearAttachment();
  }

  /**
   * Clear all headers from the mail object.
   */
  public function clearHeaders() {
    $this->message->clearHeader();
  }

  /**
   * Handles application errors generated by the RPC call.
   *
   * @param ApplicationError An exception caught during the RPC call.
   * @throws \RuntimeException If there was an internal error or bad request.
   * @throws \InvalidArgumentException If there was an unauthorized sender,
   * an invalid attachment type, or an invalid header name.
   * @throws ApplicationError If the error is not one of the above.
   */
  protected function handleApplicationError($e) {
    switch($e->getApplicationError()) {
      case ErrorCode::INTERNAL_ERROR:
      case ErrorCode::BAD_REQUEST:
        throw new \RuntimeException(
            "Mail Service Error: " . $e->getMessage());
      case ErrorCode::UNAUTHORIZED_SENDER:
        $error = sprintf("Mail Service Error: Sender (%s) is not an " .
                         "authorized email address.",
                         htmlspecialchars($this->message->getSender()));
        throw new \InvalidArgumentException($error);
      case ErrorCode::INVALID_ATTACHMENT_TYPE:
        throw new \InvalidArgumentException(
            "Mail Service Error: Invalid attachment type.");
      case ErrorCode::INVALID_HEADER_NAME:
        throw new \InvalidArgumentException(
            "Mail Service Error: Invalid header name.");
      default:
        throw $e;
    }
  }

  /**
   * Sets HTML content for the email body.
   *
   * @param string $text HTML to add.
   * @throws \InvalidArgumentException If text is not a string.
   */
  public function setHtmlBody($text) {
    if (!is_string($text)) {
      $error = sprintf("HTML text given is not a string (Actual type: %s).",
                       gettype($text));
      throw new \InvalidArgumentException($error);
    }
    $this->message->setHtmlbody($text);
  }

  /**
   * Sets AMP HTML content for the email body. This field is optional. Setting
   * AMP HTML body makes the email an AMP Email. Plain text or HTML may become
   * fallback content depending on the email client used.
   *
   * @param string $text AMP HTML to add.
   * @throws \InvalidArgumentException If text is not a string.
   */
  public function setAmpHtmlBody($text) {
    if (!is_string($text)) {
      $error = sprintf('AMP HTML text given is not a string (Actual type: %s).',
                       gettype($text));
      throw new \InvalidArgumentException($error);
    }
    $this->message->setAmpHtmlbody($text);
  }

  /**
   * Sets a reply-to address for the mail object.
   *
   * @param string $email Reply-to address.
   * @throws \InvalidArgumentException If the input reply-to address is an
   * invalid email address.
   */
  public function setReplyTo($email) {
    if (!$this->checkValidEmail($email)) {
      throw new \InvalidArgumentException(
          "Invalid reply-to: ". htmlspecialchars($email));
    }
    $this->message->setReplyto($email);
  }

  /**
   * Sets the sender for the mail object.
   *
   * @param string $email Email of the sender.
   * @throws \InvalidArgumentException If the input sender is an invalid email
   * address.
   */
  public function setSender($email) {
    if (!$this->checkValidEmail($email)) {
      throw new \InvalidArgumentException(
          "Invalid sender: ". htmlspecialchars($email));
    }
    $this->message->setSender($email);
  }

  /**
   * Sets the subject for the mail object.
   *
   * @param string $subject Subject line.
   * @throws \InvalidArgumentException If subject line is not a string.
   */
  public function setSubject($subject) {
    if (!is_string($subject)) {
      $error = sprintf("Subject given is not a string (Actual type: %s).",
               gettype($subject));
      throw new \InvalidArgumentException($error);
    }
    $this->message->setSubject($subject);
  }

  /**
   * Sets plain text for the email body.
   *
   * @param string $text Plain text to add.
   * @return bool True if successful, false otherwise.
   * @throws \InvalidArgumentException If text is not a string.
   */
  public function setTextBody($text) {
    if (!is_string($text)) {
      $error = sprintf("Plain text given is not a string (Actual type: %s).",
                       gettype($text));
      throw new \InvalidArgumentException($error);
    }
    $this->message->setTextbody($text);
  }
}
