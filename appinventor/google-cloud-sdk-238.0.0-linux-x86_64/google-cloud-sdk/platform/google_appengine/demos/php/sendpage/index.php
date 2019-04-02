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
require_once 'google/appengine/api/taskqueue/PushTask.php';
require_once 'google/appengine/api/users/User.php';
require_once 'google/appengine/api/users/UserService.php';

use \google\appengine\api\taskqueue\PushTask;
use \google\appengine\api\users\User;
use \google\appengine\api\users\UserService;

$user = UserService::getCurrentUser();
$email = $user->getEmail();
?>
<html>
  <head><title>Send Site</title></head>
  <body>
    <p>Enter the a URL.
    <form method="POST">
      URL: <input type="text" size="50" name="url">
      <input
        type="submit"
        value="Send to <?php echo htmlentities($email, ENT_QUOTES) ?>">
    </form>
    <?php
      if (isset($_POST['url'])) {
        $task = new PushTask(
            '/sendpage',
            ['url' => $_POST['url'], 'email' => $email]);
        $task->add();
        echo "<b>Sending url!</b>";
      }
    ?>
  </body>
</html>
