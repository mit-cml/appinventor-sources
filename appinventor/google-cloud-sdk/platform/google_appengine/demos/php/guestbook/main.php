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
require_once 'google/appengine/api/users/User.php';
  require_once 'google/appengine/api/users/UserService.php';

  use google\appengine\api\users\User;
  use google\appengine\api\users\UserService;

  $greeting_schema = <<<SCHEMA
CREATE TABLE IF NOT EXISTS greeting (
    id INT NOT NULL AUTO_INCREMENT,
    author VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    PRIMARY KEY (id)
)
SCHEMA;

  if (strpos(getenv('SERVER_SOFTWARE'), 'Development') === false) {
    $conn = mysqli_connect(null,
                           getenv('PRODUCTION_DB_USERNAME'),
                           getenv('PRODUCTION_DB_PASSWORD'),
                           null,
                           null,
                           getenv('PRODUCTION_CLOUD_SQL_INSTANCE'));
    $db = getenv('PRODUCTION_DB_NAME');
  } else {
    $conn = mysqli_connect(getenv('DEVELOPMENT_DB_HOST'), 
                           getenv('DEVELOPMENT_DB_USERNAME'),
                           getenv('DEVELOPMENT_DB_PASSWORD'));
    $db = getenv('DEVELOPMENT_DB_NAME');
  }

  if ($conn->connect_error) {
    die("Could not connect to database: $conn->connect_error " .
        "[$conn->connect_errno]");
  }

  if ($conn->query("CREATE DATABASE IF NOT EXISTS $db") === FALSE) {
    die("Could not create database: $conn->error [$conn->errno]");
  }

  if ($conn->select_db($db) === FALSE) {
    die("Could not select database: $conn->error [$conn->errno]");
  }

  if ($conn->query($greeting_schema) === FALSE) {
    die("Could not create tables: $conn->error [$conn->errno]");
  }

  $user = UserService::getCurrentUser();
  if($user) {
    $url = UserService::createLogoutURL('', "google.com");
    $url_linktext = "Logout";
    $author = $user->getNickname();
  }
  else {
    $url = UserService::createLoginURL('');
    $url_linktext = "Login";
    $author = "";
  }

  if(isset($_POST['content'])) {
    $stmt = $conn->prepare(
        "INSERT INTO greeting (author, content) VALUES (?, ?)");
    if ($stmt->bind_param('ss', $author, $_POST['content']) === FALSE) {
      die("Could not bind prepared statement");
    }

    if ($stmt->execute() === FALSE) {
      die("Could not execute prepared statement");
    }
   $stmt->close();
   header("Location: /");
   exit();
  }
?>
<html>
  <body>
    <?php
      $stmt = $conn->prepare(
          "SELECT author, content FROM greeting ORDER BY id DESC LIMIT 10");
      if ($stmt->execute() === FALSE) {
        die("Could not execute prepared statement");
      }
      $stmt->bind_result($author, $content);
      while ($stmt->fetch()) {
          if($author) {
            echo '<b>' . htmlentities($author) . '</b> wrote:';
          } else {
            echo 'An anonymous person wrote:';
          }
          echo '<blockquote>' . htmlentities($content) . '</blockquote>';
      }
      $stmt->close();
    ?>

    <form method="post" name="guestbook_form">
      <div><textarea name="content" rows="3" cols="60"></textarea></div>
      <div><input type="submit" value="Sign Guestbook"></div>
    </form>

    <a href="<?php echo $url; ?>"><?php echo $url_linktext; ?></a>
  </body>
</html>