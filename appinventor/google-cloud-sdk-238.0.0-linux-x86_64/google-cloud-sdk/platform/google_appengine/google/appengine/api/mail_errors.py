#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#




"""Exceptions raised by the Mail API."""




class Error(Exception):
  """Base Mail error type."""


class BadRequestError(Error):
  """The email is not valid."""


class InvalidSenderError(Error):
  """The sender is not permitted to send mail for this application."""


class InvalidEmailError(Error):
  """The specified email is invalid."""


class InvalidAttachmentTypeError(Error):
  """The file type of the attachment is invalid."""


class InvalidHeaderNameError(Error):
  """The header name is invalid."""


class MissingRecipientsError(Error):
  """A recipient was not specified in the message."""


class MissingSenderError(Error):
  """A sender was not specified in the message."""


class MissingSubjectError(Error):
  """The subject was not specified in the message."""


class MissingBodyError(Error):
  """A body was not specified in the message."""


class PayloadEncodingError(Error):
  """The payload encoding is unknown."""


class UnknownEncodingError(PayloadEncodingError):
  """The encoding is unknown."""


class UnknownCharsetError(PayloadEncodingError):
  """The character set is unknown."""
