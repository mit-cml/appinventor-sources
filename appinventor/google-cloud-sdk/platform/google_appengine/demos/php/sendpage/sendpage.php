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
require_once 'google/appengine/api/mail/Message.php';

use google\appengine\api\mail\Message;

$url = $_POST['url'];
$page_content = file_get_contents($url);

$mail_options = [
    "sender" => $_POST['email'],
    "to" => $_POST['email'],
    "subject" => "Content of $url",
    "htmlBody" => $page_content
];

$message = new Message($mail_options);
$message->send();