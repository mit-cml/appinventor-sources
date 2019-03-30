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




"""Image manipulation API.

App Engine provides the ability to manipulate image data using a dedicated
Images service. The Images service can resize, rotate, flip, and crop images; it
can composite multiple images into a single image; and it can convert image data
between several formats. It can also enhance photographs using a predefined
algorithm. The API can also provide information about an image, such as its
format, width, height, and a histogram of color values.
"""








import struct

try:
  import json
except:
  import simplejson as json

from google.appengine.api import apiproxy_stub_map
from google.appengine.api import blobstore
from google.appengine.api import datastore_types
from google.appengine.api.images import images_service_pb
from google.appengine.runtime import apiproxy_errors


BlobKey = datastore_types.BlobKey


JPEG = images_service_pb.OutputSettings.JPEG
PNG = images_service_pb.OutputSettings.PNG
WEBP = images_service_pb.OutputSettings.WEBP
BMP = -1
GIF = -2
ICO = -3
TIFF = -4

OUTPUT_ENCODING_TYPES = frozenset([JPEG, PNG, WEBP])

UNCHANGED_ORIENTATION = images_service_pb.InputSettings.UNCHANGED_ORIENTATION
CORRECT_ORIENTATION = images_service_pb.InputSettings.CORRECT_ORIENTATION

ORIENTATION_CORRECTION_TYPE = frozenset([UNCHANGED_ORIENTATION,
                                         CORRECT_ORIENTATION])

TOP_LEFT = images_service_pb.CompositeImageOptions.TOP_LEFT
TOP_CENTER = images_service_pb.CompositeImageOptions.TOP
TOP_RIGHT = images_service_pb.CompositeImageOptions.TOP_RIGHT
CENTER_LEFT = images_service_pb.CompositeImageOptions.LEFT
CENTER_CENTER = images_service_pb.CompositeImageOptions.CENTER
CENTER_RIGHT = images_service_pb.CompositeImageOptions.RIGHT
BOTTOM_LEFT = images_service_pb.CompositeImageOptions.BOTTOM_LEFT
BOTTOM_CENTER = images_service_pb.CompositeImageOptions.BOTTOM
BOTTOM_RIGHT = images_service_pb.CompositeImageOptions.BOTTOM_RIGHT

ANCHOR_TYPES = frozenset([TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT,
                          CENTER_CENTER, CENTER_RIGHT, BOTTOM_LEFT,
                          BOTTOM_CENTER, BOTTOM_RIGHT])



MAX_TRANSFORMS_PER_REQUEST = 10




MAX_COMPOSITES_PER_REQUEST = 16


class Error(Exception):
  """Base error class for this module."""


class TransformationError(Error):
  """An error occurred while attempting to transform the image."""


class BadRequestError(Error):
  """The parameters specified were in some way invalid."""


class NotImageError(Error):
  """The image data given is not recognizable as an image."""


class BadImageError(Error):
  """The image data is corrupt."""


class LargeImageError(Error):
  """The image data is too large to process."""


class InvalidBlobKeyError(Error):
  """The provided blob key was invalid."""

  def __init__(self, blob_key=None):
    """Constructor.

    Args:
      blob_key: The `blob_key` that is believed to be invalid. Set this argument
          to `None` if the blob key is unknown.
    """
    self._blob_key = blob_key

  def __str__(self):
    """Returns a string representation of this Error."""
    if self._blob_key:
      return 'InvalidBlobKeyError: %s' % repr(self._blob_key)
    else:
      return 'InvalidBlobKeyError'


class BlobKeyRequiredError(Error):
  """A blob key is required for this operation."""


class UnsupportedSizeError(Error):
  """The size that was specified is not supported by requested operation."""


class AccessDeniedError(Error):
  """The application does not have permission to access the image."""


class ObjectNotFoundError(Error):
  """The blob key referred to an object that does not exist."""


def _ToImagesError(error, blob_key=None):
  """Translates an application error to an Images error, if possible.

  Args:
    error: An `ApplicationError` to translate.
    blob_key: The `blob_key` that is used in the function that caused the
        error. Set this argument to `None` if the blob key is unknown.

  Returns:
    An Images API error if one is found; all other errors are returned as-is.
  """
  error_map = {
      images_service_pb.ImagesServiceError.NOT_IMAGE:
      NotImageError,
      images_service_pb.ImagesServiceError.BAD_IMAGE_DATA:
      BadImageError,
      images_service_pb.ImagesServiceError.IMAGE_TOO_LARGE:
      LargeImageError,
      images_service_pb.ImagesServiceError.INVALID_BLOB_KEY:
      InvalidBlobKeyError,
      images_service_pb.ImagesServiceError.ACCESS_DENIED:
      AccessDeniedError,
      images_service_pb.ImagesServiceError.OBJECT_NOT_FOUND:
      ObjectNotFoundError,
      images_service_pb.ImagesServiceError.UNSPECIFIED_ERROR:
      TransformationError,
      images_service_pb.ImagesServiceError.BAD_TRANSFORM_DATA:
      BadRequestError,
      }

  error_code = error.application_error

  if error_code == images_service_pb.ImagesServiceError.INVALID_BLOB_KEY:
    return InvalidBlobKeyError(blob_key)

  desired_exc = error_map.get(error_code, Error)
  return desired_exc(error.error_detail)


class Image(object):
  """Image object to manipulate."""

  def __init__(self, image_data=None, blob_key=None, filename=None):
    """Constructor.

    You can only specify one of the following arguments.

    Args:
      image_data: String of the image data.
      blob_key: A `BlobKey`, `BlobInfo`, string, or Unicode representation of
          the blob key of the blob that contains the image data.
      filename: String of the file name of a Google Storage file that
          contains the image data. Must be in the format
          `/gs/bucket_name/object_name`.

    Raises:
      NotImageError: If the given data is empty.
    """

    if not image_data and not blob_key and not filename:
      raise NotImageError("Empty image data.")
    if image_data and (blob_key or filename):
      raise NotImageError("Can only take one of image, blob key or filename.")
    if blob_key and filename:
      raise NotImageError("Can only take one of image, blob key or filename.")

    self._image_data = image_data
    if filename:
      self._blob_key = blobstore.create_gs_key(filename)
    else:
      self._blob_key = _extract_blob_key(blob_key)
    self._transforms = []
    self._width = None
    self._height = None
    self._format = None
    self._correct_orientation = UNCHANGED_ORIENTATION
    self._original_metadata = None

  def _check_transform_limits(self):
    """Ensures some simple limits on the number of transforms allowed.

    Raises:
      BadRequestError: If `MAX_TRANSFORMS_PER_REQUEST` transforms have already
          been requested for this image.
    """
    if len(self._transforms) >= MAX_TRANSFORMS_PER_REQUEST:
      raise BadRequestError("%d transforms have already been requested on this "
                            "image." % MAX_TRANSFORMS_PER_REQUEST)

  def _update_dimensions(self):
    """Updates the `width` and `height` fields of the image.

    Raises:
      NotImageError: If the image data is not an image.
      BadImageError: If the image data is corrupt.
    """
    if not self._image_data:
      raise NotImageError("Dimensions unavailable for blob key input")
    size = len(self._image_data)
    if size >= 6 and self._image_data.startswith("GIF"):
      self._update_gif_dimensions()
      self._format = GIF;
    elif size >= 8 and self._image_data.startswith("\x89PNG\x0D\x0A\x1A\x0A"):
      self._update_png_dimensions()
      self._format = PNG
    elif size >= 2 and self._image_data.startswith("\xff\xD8"):
      self._update_jpeg_dimensions()
      self._format = JPEG
    elif (size >= 8 and (self._image_data.startswith("II\x2a\x00") or
                         self._image_data.startswith("MM\x00\x2a"))):
      self._update_tiff_dimensions()
      self._format = TIFF
    elif size >= 2 and self._image_data.startswith("BM"):
      self._update_bmp_dimensions()
      self._format = BMP
    elif size >= 4 and self._image_data.startswith("\x00\x00\x01\x00"):
      self._update_ico_dimensions()
      self._format = ICO
    elif (size >= 16 and (self._image_data.startswith("RIFF", 0, 4) and
                          self._image_data.startswith("WEBP", 8, 12) and
                          self._image_data.startswith("VP8 ", 12, 16))):
      self._update_webp_dimensions()
      self._format = WEBP

    elif (size >= 16 and (self._image_data.startswith("RIFF", 0, 4) and
                          self._image_data.startswith("WEBP", 8, 12) and
                          self._image_data.startswith("VP8X", 12, 16))):
      self._update_webp_vp8x_dimensions()
      self._format = WEBP
    else:
      raise NotImageError("Unrecognized image format")

  def _update_gif_dimensions(self):
    """Updates the `width` and `height` fields of the GIF image.

    Raises:
      BadImageError: If the image string is not a valid GIF image.
    """

    size = len(self._image_data)
    if size >= 10:
      self._width, self._height = struct.unpack("<HH", self._image_data[6:10])
    else:
      raise BadImageError("Corrupt GIF format")

  def _update_png_dimensions(self):
    """Updates the `width` and `height` fields of the PNG image.

    Raises:
      BadImageError: If the image string is not a valid PNG image.
    """


    size = len(self._image_data)
    if size >= 24 and self._image_data[12:16] == "IHDR":
      self._width, self._height = struct.unpack(">II", self._image_data[16:24])
    else:
      raise BadImageError("Corrupt PNG format")

  def _update_jpeg_dimensions(self):
    """Updates the `width` and `height` fields of the JPEG image.

    Raises:
      BadImageError: If the image string is not a valid JPEG image.
    """

    size = len(self._image_data)
    offset = 2
    while offset < size:
      while offset < size and ord(self._image_data[offset]) != 0xFF:
        offset += 1
      while offset < size and ord(self._image_data[offset]) == 0xFF:
        offset += 1
      if (offset < size and ord(self._image_data[offset]) & 0xF0 == 0xC0 and
          ord(self._image_data[offset]) != 0xC4):
        offset += 4
        if offset + 4 <= size:
          self._height, self._width = struct.unpack(
              ">HH",
              self._image_data[offset:offset + 4])
          break
        else:
          raise BadImageError("Corrupt JPEG format")
      elif offset + 3 <= size:
        offset += 1
        offset += struct.unpack(">H", self._image_data[offset:offset + 2])[0]
      else:
        raise BadImageError("Corrupt JPEG format")
    if self._height is None or self._width is None:
      raise BadImageError("Corrupt JPEG format")

  def _update_tiff_dimensions(self):
    """Updates the `width` and `height` fields of the TIFF image.

    Raises:
      BadImageError: If the image string is not a valid TIFF image.
    """


    size = len(self._image_data)
    if self._image_data.startswith("II"):
      endianness = "<"
    else:
      endianness = ">"
    ifd_offset = struct.unpack(endianness + "I", self._image_data[4:8])[0]
    if ifd_offset + 14 <= size:
      ifd_size = struct.unpack(
          endianness + "H",
          self._image_data[ifd_offset:ifd_offset + 2])[0]
      ifd_offset += 2
      for unused_i in range(0, ifd_size):
        if ifd_offset + 12 <= size:
          tag = struct.unpack(
              endianness + "H",
              self._image_data[ifd_offset:ifd_offset + 2])[0]
          if tag == 0x100 or tag == 0x101:
            value_type = struct.unpack(
                endianness + "H",
                self._image_data[ifd_offset + 2:ifd_offset + 4])[0]
            if value_type == 3:
              format = endianness + "H"
              end_offset = ifd_offset + 10
            elif value_type == 4:
              format = endianness + "I"
              end_offset = ifd_offset + 12
            else:
              format = endianness + "B"
              end_offset = ifd_offset + 9
            if tag == 0x100:
              self._width = struct.unpack(
                  format,
                  self._image_data[ifd_offset + 8:end_offset])[0]
              if self._height is not None:
                break
            else:
              self._height = struct.unpack(
                  format,
                  self._image_data[ifd_offset + 8:end_offset])[0]
              if self._width is not None:
                break
          ifd_offset += 12
        else:
          raise BadImageError("Corrupt TIFF format")
    if self._width is None or self._height is None:
      raise BadImageError("Corrupt TIFF format")

  def _update_bmp_dimensions(self):
    """Updates the `width` and `height` fields of the BMP image.

    Raises:
      BadImageError: If the image string is not a valid BMP image.
    """




    size = len(self._image_data)
    if size >= 18:
      header_length = struct.unpack("<I", self._image_data[14:18])[0]
      if ((header_length == 40 or header_length == 108 or
           header_length == 124 or header_length == 64) and size >= 26):

        self._width, self._height = struct.unpack("<II",
                                                  self._image_data[18:26])
      elif header_length == 12 and size >= 22:
        self._width, self._height = struct.unpack("<HH",
                                                  self._image_data[18:22])
      else:
        raise BadImageError("Corrupt BMP format")
    else:
      raise BadImageError("Corrupt BMP format")

  def _update_ico_dimensions(self):
    """Updates the `width` and `height` fields of the ICO image.

    Raises:
      BadImageError: If the image string is not a valid ICO image.
    """
    size = len(self._image_data)
    if size >= 8:
      self._width, self._height = struct.unpack("<BB", self._image_data[6:8])

      if not self._width:
        self._width = 256
      if not self._height:
        self._height = 256
    else:
      raise BadImageError("Corrupt ICO format")

  def set_correct_orientation(self, correct_orientation):
    """Sets a flag to correct the image orientation based on image metadata.

    EXIF metadata within the image may contain a parameter that indicates its
    proper orientation. This value can equal 1 through 8, inclusive. Setting the
    orientation to 1 means that the image is in its "normal" orientation, i.e.,
    it should be viewed as it is stored. Normally, this orientation value has no
    effect on the behavior of the transformations. However, if you call this
    function with the `correct_orientation` argument, any orientation specified
    in the EXIF metadata will be corrected during the first transformation.

    Note:
        If `correct_orientation` is specified, but the image is already in
        portrait orientation, i.e., "taller" than it is "wide," no corrections
        will be made, since the camera has likely already corrected it.

    Whether the correction was requested or not, the orientation value in the
    transformed image is always cleared to indicate that no additional
    corrections of the returned image's orientation is necessary.

    Args:
      correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE`.

    Raises:
      BadRequestError: If the `correct_orientation` value is invalid.
    """
    if correct_orientation not in ORIENTATION_CORRECTION_TYPE:
      raise BadRequestError("Orientation correction must be in %s" %
                            ORIENTATION_CORRECTION_TYPE)
    self._correct_orientation = correct_orientation


  def _update_webp_dimensions(self):
    """Updates the width and height fields of the webp image."""

    size = len(self._image_data)



    if size < 30:
      raise BadImageError("Corrupt WEBP format")

    bits = (ord(self._image_data[20]) | (ord(self._image_data[21])<<8) |
            (ord(self._image_data[22]) << 16))

    key_frame = ((bits & 1) == 0)

    if not key_frame:
      raise BadImageError("Corrupt WEBP format")

    profile = (bits >> 1) & 7
    show_frame = (bits >> 4) & 1

    if profile > 3:
      raise BadImageError("Corrupt WEBP format")

    if show_frame == 0:
      raise BadImageError("Corrupt WEBP format")

    self._width, self._height = struct.unpack("<HH", self._image_data[26:30])

    if self._height is None or self._width is None:
      raise BadImageError("Corrupt WEBP format")

  def _update_webp_vp8x_dimensions(self):
    """Updates the width and height fields of a WEBP image with a vp8x chunk."""
    size = len(self._image_data)

    if size < 30:
      raise BadImageError("Corrupt WEBP format")

    self._width, self._height = struct.unpack("<II", self._image_data[24:32])

    if self._height is None or self._width is None:
      raise BadImageError("Corrupt WEBP format")

  def resize(self, width=0, height=0, crop_to_fit=False,
             crop_offset_x=0.5, crop_offset_y=0.5, allow_stretch=False):
    """Resizes the image, maintaining the aspect ratio.

    If both `width` and `height` are specified, the more restricting of the two
    values will be used when the image is resized. The maximum dimension allowed
    for both `width` and `height` is 4000 pixels.

    If both `width` and `height` are specified, and `crop_to_fit` is `True`, the
    less restricting of the two values will be used when the image is resized,
    and the image will be cropped to fit the specified size. In this case, the
    center of cropping can be adjusted by `crop_offset_x` and `crop_offset_y`.

    Args:
      width: Integer of the width in pixels to change the image width to.
      height: Integer of the height in pixels to change the image height to.
      crop_to_fit: If `True`, and both `width` and `height` are specified, the
          image is cropped after it is resized to fit the specified
          dimensions.
      crop_offset_x: Float value between 0.0 and 1.0. 0 is left, and 1 is right.
          The default value is 0.5, or the center of the image.
      crop_offset_y: Float value between 0.0 and 1.0. 0 is top, and 1 is bottom.
          The default value is 0.5, or the center of the image.
      allow_stretch: If `True`, and both `width` and `height` are specified, the
          image is stretched to fit the resize dimensions without maintaining
          the aspect ratio.

    Raises:
      TypeError: When `width` or `height` is not an integer or long type.
      BadRequestError: When the height or width is invalid, or if
          `MAX_TRANSFORMS_PER_REQUEST` transforms have already been
          requested on this image.
    """
    if (not isinstance(width, (int, long)) or
        not isinstance(height, (int, long))):
      raise TypeError("Width and height must be integers.")
    if width < 0 or height < 0:
      raise BadRequestError("Width and height must be >= 0.")

    if not width and not height:
      raise BadRequestError("At least one of width or height must be > 0.")

    if width > 4000 or height > 4000:
      raise BadRequestError("Both width and height must be <= 4000.")

    if not isinstance(crop_to_fit, bool):
      raise TypeError("crop_to_fit must be boolean.")

    if crop_to_fit and not (width and height):
      raise BadRequestError("Both width and height must be > 0 when "
                            "crop_to_fit is specified.")

    if not isinstance(allow_stretch, bool):
      raise TypeError("allow_stretch must be boolean.")

    if allow_stretch and not (width and height):
      raise BadRequestError("Both width and height must be > 0 when "
                            "allow_stretch is specified.")

    self._validate_crop_arg(crop_offset_x, "crop_offset_x")
    self._validate_crop_arg(crop_offset_y, "crop_offset_y")

    self._check_transform_limits()

    transform = images_service_pb.Transform()
    transform.set_width(width)
    transform.set_height(height)
    transform.set_crop_to_fit(crop_to_fit)
    transform.set_crop_offset_x(crop_offset_x)
    transform.set_crop_offset_y(crop_offset_y)
    transform.set_allow_stretch(allow_stretch)

    self._transforms.append(transform)

  def rotate(self, degrees):
    """Rotates an image a given number of degrees clockwise.

    Args:
      degrees: Integer value of the number of degrees that an image must be
          rotated. This value must be a multiple of 90.

    Raises:
      TypeError: When `degrees` is not an integer or long type.
      BadRequestError: When the `degrees` value is invalid or if
          `MAX_TRANSFORMS_PER_REQUEST` transforms have already been requested.
    """
    if not isinstance(degrees, (int, long)):
      raise TypeError("Degrees must be integers.")

    if degrees % 90 != 0:
      raise BadRequestError("degrees argument must be multiple of 90.")


    degrees = degrees % 360




    self._check_transform_limits()

    transform = images_service_pb.Transform()
    transform.set_rotate(degrees)

    self._transforms.append(transform)

  def horizontal_flip(self):
    """Flips the image horizontally.

    Raises:
      BadRequestError: If `MAX_TRANSFORMS_PER_REQUEST` transforms have already
          been requested on the image.
    """
    self._check_transform_limits()

    transform = images_service_pb.Transform()
    transform.set_horizontal_flip(True)

    self._transforms.append(transform)

  def vertical_flip(self):
    """Flips the image vertically.

    Raises:
      BadRequestError: If `MAX_TRANSFORMS_PER_REQUEST` transforms have already
          been requested on the image.
    """
    self._check_transform_limits()
    transform = images_service_pb.Transform()
    transform.set_vertical_flip(True)

    self._transforms.append(transform)

  def _validate_crop_arg(self, val, val_name):
    """Validates the given value of a `Crop()` method argument.

    Args:
      val: Float value of the argument.
      val_name: String of the name of the argument.

    Raises:
      TypeError: If the arguments are not of type float.
      BadRequestError: When the bounding box is invalid.
    """
    if type(val) != float:
      raise TypeError("arg '%s' must be of type 'float'." % val_name)

    if not (0 <= val <= 1.0):
      raise BadRequestError("arg '%s' must be between 0.0 and 1.0 "
                            "(inclusive)" % val_name)

  def crop(self, left_x, top_y, right_x, bottom_y):
    """Crops the image.

    The four arguments are the scaling numbers that describe the bounding box
    that will crop the image.  The upper left point of the bounding box will
    be at (`left_x*image_width`, `top_y*image_height`), and the lower right
    point will be at (`right_x*image_width`, `bottom_y*image_height`).

    Args:
      left_x: Float value between 0.0 and 1.0, inclusive.
      top_y: Float value between 0.0 and 1.0, inclusive.
      right_x: Float value between 0.0 and 1.0, inclusive.
      bottom_y: Float value between 0.0 and 1.0, inclusive.

    Raises:
      TypeError: If the arguments are not of type float.
      BadRequestError: When the bounding box values are invalid or if
          `MAX_TRANSFORMS_PER_REQUEST` transforms have already been requested
          for this image.
    """
    self._validate_crop_arg(left_x, "left_x")
    self._validate_crop_arg(top_y, "top_y")
    self._validate_crop_arg(right_x, "right_x")
    self._validate_crop_arg(bottom_y, "bottom_y")

    if left_x >= right_x:
      raise BadRequestError("left_x must be less than right_x")
    if top_y >= bottom_y:
      raise BadRequestError("top_y must be less than bottom_y")

    self._check_transform_limits()

    transform = images_service_pb.Transform()
    transform.set_crop_left_x(left_x)
    transform.set_crop_top_y(top_y)
    transform.set_crop_right_x(right_x)
    transform.set_crop_bottom_y(bottom_y)

    self._transforms.append(transform)

  def im_feeling_lucky(self):
    """Automatically adjusts the image's contrast and color levels.

    This is similar to the "I'm Feeling Lucky" button in Picasa.

    Raises:
      BadRequestError: If `MAX_TRANSFORMS_PER_REQUEST` transforms have already
          been requested for this image.
    """
    self._check_transform_limits()
    transform = images_service_pb.Transform()
    transform.set_autolevels(True)

    self._transforms.append(transform)

  def get_original_metadata(self):
    """Lists the metadata of the original image.

    Returns a dictionary of metadata extracted from the original image during
    `execute_transform`.

    Note:
        Some of the EXIF fields are processed; for example, fields with multiple
        values are returned as lists, rational types are returned as floats, GPS
        coordinates are already parsed to signed floats, etc.

    `ImageWidth` and `ImageLength` fields are corrected if they did not
    correspond to the actual dimensions of the original image.

    Returns:
      Dictionary of metadata with string keys. If `execute_transform` was
      called with `parse_metadata` set to `True`, this dictionary contains
      information about the various properties of the original image, such as
      dimensions, color profile, and properties from EXIF.

      Even if `parse_metadata` was `False` or the images did not have any
      metadata, the dictionary will contain a limited set of metadata, with
      least `ImageWidth` and `ImageLength` dimensions of the original image
      being returned.

      This call will return `None` if it is called before a successful
      `execute_transfrom` call.
    """
    return self._original_metadata

  def _set_imagedata(self, imagedata):
    """Populates an `ImageData` protocol buffer from this Image instance.

    Args:
        imagedata: An `ImageData` protocol buffer instance.
    """
    if self._blob_key:
      imagedata.set_content("")
      imagedata.set_blob_key(self._blob_key)
    else:
      imagedata.set_content(self._image_data)

  def execute_transforms(self, output_encoding=PNG, quality=None,
                         parse_source_metadata=False,
                         transparent_substitution_rgb=None,
                         rpc=None):
    """Performs transformations on a given image.

    You can only execute one transformation per image per `execute_transforms()`
    call.

    Args:
      output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
      quality: A value between 1 and 100 to specify the quality of the
          encoding.  This value is only used for JPEG and WEBP quality control.
      parse_source_metadata: When `True`, the metadata (EXIF) of the source
          image is parsed before any transformations. The results can be
          retrieved via `Image.get_original_metadata`.
      transparent_substitution_rgb: When transparent pixels are not supported in
          the destination image format, transparent pixels will be substituted
          for the specified color, which must be 32-bit RGB format.
      rpc: A UserRPC object.

    Returns:
      A string of the image data after the transformations have been
      performed on it.

    Raises:
      BadRequestError: When the request specifications are invalid.
      NotImageError: When the image data given is not an image.
      BadImageError: When the image data given is corrupt.
      LargeImageError: When the image data given is too large to process.
      InvalidBlobKeyError: When the blob key provided is invalid.
      TransformationError: When an error occurs during image manipulation.
      AccessDeniedError: When the blob key refers to a Google Storage object,
          and the application does not have permission to access the object.
      ObjectNotFoundError: When the blob key refers to an object that no
          longer exists.
      Error: All other error conditions.
    """
    rpc = self.execute_transforms_async(output_encoding=output_encoding,
        quality=quality,
        parse_source_metadata=parse_source_metadata,
        transparent_substitution_rgb=transparent_substitution_rgb,
        rpc=rpc)
    return rpc.get_result()

  def execute_transforms_async(self, output_encoding=PNG, quality=None,
                               parse_source_metadata=False,
                               transparent_substitution_rgb=None,
                               rpc=None):
    """Asynchronously performs transformations on a given image.

    Args:
      output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
      quality: A value between 1 and 100 to specify the quality of the
          encoding. This value is only used for JPEG & WEBP quality control.
      parse_source_metadata: When `True`, the metadata (EXIF) of the source
          image is parsed before any transformations. The results can be
          retrieved via `Image.get_original_metadata`.
      transparent_substitution_rgb: When transparent pixels are not supported in
          the destination image format, transparent pixels will be substituted
          for the specified color, which must be in 32-bit RGB format.
      rpc: A UserRPC object.

    Returns:
      A UserRPC object.

    Raises:
      BadRequestError: When the request specifications are invalid.
      NotImageError: When the image data given is not an image.
      BadImageError: When the image data given is corrupt.
      LargeImageError: When the image data given is too large to process.
      InvalidBlobKeyError: When the blob key provided is invalid.
      TransformationError: When an error occurs during image manipulation.
      AccessDeniedError: When the blob key refers to a Google Storage object,
          and the application does not have permission to access the object.
      ValueError: When `transparent_substitution_rgb` is not an integer.
      Error: All other error conditions.
    """
    if output_encoding not in OUTPUT_ENCODING_TYPES:
      raise BadRequestError("Output encoding type not in recognized set "
                            "%s" % OUTPUT_ENCODING_TYPES)

    if not self._transforms:
      raise BadRequestError("Must specify at least one transformation.")

    if transparent_substitution_rgb:
      if not isinstance(transparent_substitution_rgb, int):
        raise ValueError(
            "transparent_substitution_rgb must be a 32 bit integer")

    self.CheckValidIntParameter(quality, 1, 100, "quality")

    request = images_service_pb.ImagesTransformRequest()
    response = images_service_pb.ImagesTransformResponse()

    input_settings = request.mutable_input()
    input_settings.set_correct_exif_orientation(
        self._correct_orientation)

    if parse_source_metadata:
      input_settings.set_parse_metadata(True)

    self._set_imagedata(request.mutable_image())

    for transform in self._transforms:
      request.add_transform().CopyFrom(transform)

    request.mutable_output().set_mime_type(output_encoding)

    if ((output_encoding == JPEG or output_encoding == WEBP) and
        (quality is not None)):
      request.mutable_output().set_quality(quality)

    if transparent_substitution_rgb:
      input_settings.set_transparent_substitution_rgb(
          transparent_substitution_rgb)



    def execute_transforms_hook(rpc):
      """Checks success, handles exceptions, returns the converted RPC result.

      Args:
        rpc: A UserRPC object.

      Raises:
        See `execute_transforms_async` for more details.
      """
      try:
        rpc.check_success()
      except apiproxy_errors.ApplicationError, e:
        raise _ToImagesError(e, self._blob_key)
      self._image_data = rpc.response.image().content()
      self._blob_key = None
      self._transforms = []
      if response.image().has_width():
        self._width = rpc.response.image().width()
      else:
        self._width = None
      if response.image().has_height():
        self._height = rpc.response.image().height()
      else:
        self._height = None
      self._format = None
      if response.source_metadata():
        self._original_metadata = json.loads(response.source_metadata())
      return self._image_data

    return _make_async_call(rpc,
                            "Transform",
                            request,
                            response,
                            execute_transforms_hook,
                            None)

  @property
  def width(self):
    """Retrieves the width of the image."""
    if self._width is None:
      self._update_dimensions()
    return self._width

  @property
  def height(self):
    """Retrieves the height of the image."""
    if self._height is None:
      self._update_dimensions()
    return self._height

  @property
  def format(self):
    """Retrieves the format of the image."""
    if self._format is None:
      self._update_dimensions()
    return self._format

  def histogram(self, rpc=None):
    """Calculates the histogram of the image.

    Args:
      rpc: A UserRPC object.

    Returns:
      3 256-element lists containing the number of occurences of each value of
      each color in the order RGB. You can learn more about color histograms
      at `Wikipedia`_.

    Raises:
      NotImageError: When the image data supplied is not an image.
      BadImageError: When the image data supplied is corrupt.
      LargeImageError: When the image data supplied is too large to process.
      Error: All other error conditions.

    .. _Wikipedia:
        http://en.wikipedia.org/wiki/Color_histogram

    """

    rpc = self.histogram_async(rpc)
    return rpc.get_result()

  def histogram_async(self, rpc=None):
    """Asynchronously calculates the histogram of the image.

    Args:
      rpc: An optional UserRPC object.

    Returns:
      rpc: A UserRPC object.

    Raises:
      NotImageError: When the image data supplied is not an image.
      BadImageError: When the image data supplied is corrupt.
      LargeImageError: When the image data supplied is too large to process.
      Error: All other error conditions.
    """
    request = images_service_pb.ImagesHistogramRequest()
    response = images_service_pb.ImagesHistogramResponse()

    self._set_imagedata(request.mutable_image())

    def get_histogram_hook(rpc):
      """Checks success, handles exceptions, returns the converted RPC result.

      Args:
        rpc: A UserRPC object.

      Returns:
        3 256-element lists containing the number of occurrences of each value
        of each color in the order RGB. To learn more about color histograms,
        see the `Wikipedia article`_ for color histograms.

      Raises:
        See `histogram_async` for more details.

      .. _Wikipedia article:
         https://en.wikipedia.org/wiki/Color_histogram
      """
      try:
        rpc.check_success()
      except apiproxy_errors.ApplicationError, e:
        raise _ToImagesError(e, self._blob_key)

      histogram = rpc.response.histogram()
      return [histogram.red_list(),
              histogram.green_list(),
              histogram.blue_list()]

    return _make_async_call(rpc,
                            "Histogram",
                            request,
                            response,
                            get_histogram_hook,
                            None)

  @staticmethod
  def CheckValidIntParameter(parameter, min_value, max_value, name):
    """Checks that a parameter is an integer within the specified range."""

    if parameter is not None:
      if not isinstance(parameter, (int, long)):
        raise TypeError("%s must be an integer." % name)
      if parameter > max_value or parameter < min_value:
        raise BadRequestError("%s must be between %s and %s."
                              % name, str(min_value), str(max_value))








def create_rpc(deadline=None, callback=None):
  """Creates an RPC object for use with the Images API.

  Args:
    deadline: Optional deadline in seconds for the operation; the default
        value is a system-specific deadline (typically 5 seconds).
    callback: Optional callable to invoke on completion.

  Returns:
    An `apiproxy_stub_map.UserRPC` object specialized for this service.
  """
  return apiproxy_stub_map.UserRPC("images", deadline, callback)


def _make_async_call(rpc, method, request, response,
                     get_result_hook, user_data):
  if rpc is None:
    rpc = create_rpc()
  rpc.make_call(method, request, response, get_result_hook, user_data)
  return rpc


def resize(image_data, width=0, height=0, output_encoding=PNG, quality=None,
           correct_orientation=UNCHANGED_ORIENTATION,
           crop_to_fit=False, crop_offset_x=0.5, crop_offset_y=0.5,
           allow_stretch=False, rpc=None, transparent_substitution_rgb=None):
  """Resizes a given image file while maintaining the aspect ratio.

  If both `width` and `height` are specified, the more restricting of the two
  values will be used when resizing the image. The maximum dimension allowed
  for both width and height is 4000 pixels.

  If both `width` and `height` are specified and `crop_to_fit` is `True`, the
  less restricting of the two values will be used when resizing and the image
  will be cropped to fit the specified size. In this case, the center of
  cropping can be adjusted by `crop_offset_x` and `crop_offset_y`.

  Args:
    image_data: String of the source image data.
    width: Integer of the width (in pixels) to change the image width to.
    height: Integer of the height (in pixels) to change the image height to.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding.  This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    crop_to_fit: If `True`, and both `width` and `height` are specified, the
        image is cropped after being resized to fit the specified dimensions.
    crop_offset_x: Float value between 0.0 and 1.0. 0 is left and 1 is right.
        The default value is 0.5, or the center of image.
    crop_offset_y: Float value between 0.0 and 1.0. 0 is top and 1 is bottom.
        The default value is 0.5, or the center of image.
    allow_stretch: If `True`, and both `width` and `height` are specified, the
        image is stretched to fit the resized dimensions without maintaining
        the aspect ratio.
    rpc: Optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    Raw image data of the resized image.

  Raises:
    TypeError: When `width` or `height` are not of integer or long type.
    BadRequestError: When the specified `width` or `height` is invalid.
    Error: All other errors. See `Image.ExecuteTransforms` for more details.
  """
  rpc = resize_async(image_data,
                     width=width,
                     height=height,
                     output_encoding=output_encoding,
                     quality=quality,
                     correct_orientation=correct_orientation,
                     crop_to_fit=crop_to_fit,
                     crop_offset_x=crop_offset_x,
                     crop_offset_y=crop_offset_y,
                     allow_stretch=allow_stretch,
                     rpc=rpc,
                     transparent_substitution_rgb=transparent_substitution_rgb)
  return rpc.get_result()


def resize_async(image_data, width=0, height=0, output_encoding=PNG,
                 quality=None, correct_orientation=UNCHANGED_ORIENTATION,
                 crop_to_fit=False, crop_offset_x=0.5, crop_offset_y=0.5,
                 allow_stretch=False, rpc=None,
                 transparent_substitution_rgb=None):
  """Asynchronously resizes an image file, maintaining the aspect ratio.

  If both `width` and `height` are specified, the more restricting of the two
  values will be used when resizing the image. The maximum dimension allowed
  for both width and height is 4000 pixels.

  If both `width` and `height` are specified and `crop_to_fit` is `True`, the
  less restricting of the two values will be used when resizing and the image
  will be cropped to fit the specified size. In this case, the center of
  cropping can be adjusted by `crop_offset_x` and `crop_offset_y`.

  Args:
    image_data: String of the source image data.
    width: Integer of the width in pixels to change the image width to.
    height: Integer of the height in pixels to change the image height to.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    crop_to_fit: If `True`, and both `width` and `height` are specified, the
        image is cropped after being resized to fit the specified dimensions.
    crop_offset_x: Float value between 0.0 and 1.0. 0 is left and 1 is right.
        The default value is 0.5, or the center of image.
    crop_offset_y: Float value between 0.0 and 1.0. 0 is top and 1 is bottom.
        The default value is 0.5, or the center of image.
    allow_stretch: If `True`, and both `width` and `height` are specified, the
        image is stretched to fit the resized dimensions without maintaining
        the aspect ratio.
    rpc: Optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    A UserRPC object; call `get_result()` to obtain the result of the RPC.

  Raises:
    TypeError: When `width` or `height` are not of integer or long type.
    BadRequestError: When the specified `width` or `height` is invalid.
    Error: All other errors. See `Image.ExecuteTransforms` for more details.
  """
  image = Image(image_data)
  image.resize(width, height, crop_to_fit=crop_to_fit,
               crop_offset_x=crop_offset_x, crop_offset_y=crop_offset_y,
               allow_stretch=allow_stretch)
  image.set_correct_orientation(correct_orientation)
  return image.execute_transforms_async(output_encoding=output_encoding,
      quality=quality,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)


def rotate(image_data, degrees, output_encoding=PNG, quality=None,
           correct_orientation=UNCHANGED_ORIENTATION, rpc=None,
           transparent_substitution_rgb=None):
  """Rotates an image a given number of degrees clockwise.

  Args:
    image_data: String of the source image data.
    degrees: A value from `ROTATE_DEGREE_VALUES`.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    Raw image data of the rotated image.

  Raises:
    TypeError: When `degrees` is not of integer or long type.
    BadRequestError: When the specified `degrees` value is invalid.
    Error: All other errors. See `Image.ExecuteTransforms` for more details.
  """
  rpc = rotate_async(image_data,
      degrees,
      output_encoding=output_encoding,
      quality=quality,
      correct_orientation=correct_orientation,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)
  return rpc.get_result()


def rotate_async(image_data, degrees, output_encoding=PNG, quality=None,
                 correct_orientation=UNCHANGED_ORIENTATION, rpc=None,
                 transparent_substitution_rgb=None):
  """Asynchronously rotates an image a specified number of degrees clockwise.

  Args:
    image_data: String of the source image data.
    degrees: A value from `ROTATE_DEGREE_VALUES`.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    A UserRPC object; call `get_result()` to complete the RPC and obtain the
    crop result.

  Raises:
    TypeError: When `degrees` is not of integer or long type.
    BadRequestError: When the specified `degrees` value is invalid.
    Error: All other errors. See `Image.ExecuteTransforms` for more details.
  """
  image = Image(image_data)
  image.rotate(degrees)
  image.set_correct_orientation(correct_orientation)
  return image.execute_transforms_async(output_encoding=output_encoding,
      quality=quality,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)


def horizontal_flip(image_data, output_encoding=PNG, quality=None,
                    correct_orientation=UNCHANGED_ORIENTATION, rpc=None,
                    transparent_substitution_rgb=None):
  """Flips the image horizontally.

  Args:
    image_data: String of the source image data.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    Raw image data of the transformed image.

  Raises:
    Error: All errors. See `Image.ExecuteTransforms` for more details.
  """
  rpc = horizontal_flip_async(image_data,
      output_encoding=output_encoding,
      quality=quality,
      correct_orientation=correct_orientation,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)
  return rpc.get_result()


def horizontal_flip_async(image_data, output_encoding=PNG, quality=None,
                          correct_orientation=UNCHANGED_ORIENTATION,
                          rpc=None,
                          transparent_substitution_rgb=None):
  """Asynchronously flips the image horizontally.

  Args:
    image_data: String of the source image data.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding.  This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    A UserRPC object; call `get_result()` to complete the RPC and obtain the
    crop result.

  Raises:
    Error: All errors. See `Image.ExecuteTransforms` for more details.
  """
  image = Image(image_data)
  image.horizontal_flip()
  image.set_correct_orientation(correct_orientation)
  return image.execute_transforms_async(output_encoding=output_encoding,
      quality=quality,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)


def vertical_flip(image_data, output_encoding=PNG, quality=None,
                  correct_orientation=UNCHANGED_ORIENTATION, rpc=None,
                  transparent_substitution_rgb=None):
  """Flips the image vertically.

  Args:
    image_data: String of the source image data.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding.  This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    Raw image data of the transformed image.

  Raises:
    Error: All errors. See `Image.ExecuteTransforms` for more details.
  """
  rpc = vertical_flip_async(image_data,
      output_encoding=output_encoding,
      quality=quality,
      correct_orientation=correct_orientation,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)
  return rpc.get_result()


def vertical_flip_async(image_data, output_encoding=PNG, quality=None,
                        correct_orientation=UNCHANGED_ORIENTATION, rpc=None,
                        transparent_substitution_rgb=None):
  """Asynchronously flips the image vertically.

  Args:
    image_data: String of the source image data.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    A UserRPC object; call `get_result()` to complete the RPC and obtain the
    crop result.

  Raises:
    Error: All errors. See `Image.ExecuteTransforms` for more details.
  """
  image = Image(image_data)
  image.vertical_flip()
  image.set_correct_orientation(correct_orientation)
  return image.execute_transforms_async(output_encoding=output_encoding,
      quality=quality,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)


def crop(image_data, left_x, top_y, right_x, bottom_y, output_encoding=PNG,
         quality=None, correct_orientation=UNCHANGED_ORIENTATION, rpc=None,
         transparent_substitution_rgb=None):
  """Crops the image.

  The four arguments are the scaling numbers that describe the bounding box
  that will crop the image.  The upper left point of the bounding box will be at
  (`left_x*image_width`, `top_y*image_height`), and the lower right point will
  be at (`right_x*image_width`, `bottom_y*image_height`).

  Args:
    image_data: String of the source image data.
    left_x: Float value between 0.0 and 1.0, inclusive.
    top_y: Float value between 0.0 and 1.0, inclusive.
    right_x: Float value between 0.0 and 1.0, inclusive.
    bottom_y: Float value between 0.0 and 1.0, inclusive.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    Raw image data of the transformed image.

  Raises:
    TypeError: If the arguments are not of type float.
    BadRequestError: If the bounding box values are invalid.
    Error: All other errors. See `Image.ExecuteTransforms` for more details.
  """
  rpc = crop_async(image_data, left_x, top_y, right_x, bottom_y,
                   output_encoding=output_encoding, quality=quality,
                   correct_orientation=correct_orientation, rpc=rpc,
                   transparent_substitution_rgb=transparent_substitution_rgb)
  return rpc.get_result()


def crop_async(image_data, left_x, top_y, right_x, bottom_y,
               output_encoding=PNG, quality=None,
               correct_orientation=UNCHANGED_ORIENTATION, rpc=None,
               transparent_substitution_rgb=None):
  """Asynchronously crops the given image.

  The four arguments are the scaling numbers that describe the bounding box
  that will crop the image.  The upper left point of the bounding box will be at
  (`left_x*image_width`, `top_y*image_height`), and the lower right point will
  be at (`right_x*image_width`, `bottom_y*image_height`).

  Args:
    image_data: String of the source image data.
    left_x: Float value between 0.0 and 1.0, inclusive.
    top_y: Float value between 0.0 and 1.0, inclusive.
    right_x: Float value between 0.0 and 1.0, inclusive.
    bottom_y: Float value between 0.0 and 1.0, inclusive.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    A UserRPC object; call `get_result()` to complete the RPC and obtain the
    crop result.

  Raises:
    TypeError: If the args are not of type float.
    BadRequestError: When the bounding box values are invalid.
    Error: All other errors. See `Image.ExecuteTransforms` for more details.
  """
  image = Image(image_data)
  image.crop(left_x, top_y, right_x, bottom_y)
  image.set_correct_orientation(correct_orientation)
  return image.execute_transforms_async(output_encoding=output_encoding,
      quality=quality,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)


def im_feeling_lucky(image_data, output_encoding=PNG, quality=None,
                     correct_orientation=UNCHANGED_ORIENTATION, rpc=None,
                     transparent_substitution_rgb=None):
  """Automatically adjusts image levels.

  This is similar to the "I'm Feeling Lucky" button in Picasa.

  Args:
    image_data: String of the source image data.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    Raw image data of the transformed image.

  Raises:
    Error: All errors. See `Image.ExecuteTransforms` for more details.
  """
  rpc = im_feeling_lucky_async(image_data,
      output_encoding=output_encoding,
      quality=quality,
      correct_orientation=correct_orientation,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)
  return rpc.get_result()


def im_feeling_lucky_async(image_data, output_encoding=PNG, quality=None,
                           correct_orientation=UNCHANGED_ORIENTATION, rpc=None,
                           transparent_substitution_rgb=None):
  """Asynchronously automatically adjusts image levels.

  This is similar to the "I'm Feeling Lucky" button in Picasa.

  Args:
    image_data: String of the source image data.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    correct_orientation: A value from `ORIENTATION_CORRECTION_TYPE` to
        indicate if orientation correction should be performed during the
        transformation.
    rpc: An optional UserRPC object.
    transparent_substitution_rgb: When transparent pixels are not supported in
        the destination image format, then transparent pixels will be
        substituted for the specified color, which must be in 32-bit RGB
        format.

  Returns:
    A UserRPC object.

  Raises:
    Error: All errors. See `Image.ExecuteTransforms` for more details.
  """
  image = Image(image_data)
  image.im_feeling_lucky()
  image.set_correct_orientation(correct_orientation)
  return image.execute_transforms_async(output_encoding=output_encoding,
      quality=quality,
      rpc=rpc,
      transparent_substitution_rgb=transparent_substitution_rgb)


def composite(inputs, width, height, color=0, output_encoding=PNG,
              quality=None, rpc=None):
  """Composites one or more images onto a canvas.

  Args:
    inputs: a list of tuples (`image_data`, `x_offset`, `y_offset`, `opacity`,
        `anchor`) where:
              - image_data
                   String of the source image data.
              - x_offset
                   X offset, in pixels from the anchor position.
              - y_offset
                   Y offset, in piyels from the anchor position.
              - opacity
                   Opacity of the image, specified as a float in range [0.0,
                   1.0]
              - anchor
                   Anchoring point from `ANCHOR_POINTS`. The anchor point of the
                   image is aligned with the same anchor point of the canvas.
                   For example, `TOP_RIGHT` would place the top right corner of
                   the image at the top right corner of the canvas, then apply
                   the x and y offsets.
    width: Canvas width, in pixels.
    height: Canvas height, in pixels.
    color: Canvas background color, encoded as a 32-bit unsigned integer, where
        each color channel is represented by one byte in ARGB order.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used to control JPEG quality.
    rpc: Optional UserRPC object.

  Returns:
    A string that contains the image data of the composited image.

  Raises:
    TypeError: If `width`, `height`, `color`, `x_offset` or `y_offset` are not
        of type integer or long, or if `opacity` is not a float.
    BadRequestError: If more than `MAX_TRANSFORMS_PER_REQUEST` compositions
        have been requested, if the canvas width or height is greater than
        4000 or less than or equal to 0, if the color is invalid, if the opacity
        is outside the range [0,1] for any composition option, or if the anchor
        is invalid.
  """
  rpc = composite_async(inputs, width, height, color=color,
                        output_encoding=output_encoding, quality=quality,
                        rpc=rpc)
  return rpc.get_result()


def composite_async(inputs, width, height, color=0, output_encoding=PNG,
                    quality=None, rpc=None):
  """Asynchronously composites one or more images onto a canvas.

  Args:
    inputs: A list of tuples (`image_data`, `x_offset`, `y_offset`, `opacity`,
        `anchor`), where:
              - image_data
                   String of the source image data.
              - x_offset
                   X offset, in pixels from the anchor position.
              - y_offset
                   Y offset, in piyels from the anchor position.
              - opacity
                   Opacity of the image, specified as a float in range [0.0,
                   1.0]
              - anchor
                   Anchoring point from `ANCHOR_POINTS`. The anchor point of the
                   image is aligned with the same anchor point of the canvas.
                   For example, `TOP_RIGHT` would place the top right corner of
                   the image at the top right corner of the canvas, then apply
                   the x and y offsets.
    width: Canvas width, in pixels.
    height: Canvas height, in pixels.
    color: Canvas background color, encoded as a 32-bit unsigned integer, where
        each color channel is represented by one byte in ARGB order.
    output_encoding: A value from `OUTPUT_ENCODING_TYPES`.
    quality: A value between 1 and 100 to specify the quality of the
        encoding. This value is only used for JPEG quality control.
    rpc: Optional UserRPC object.

  Returns:
    A UserRPC object.

  Raises:
    TypeError: If `width`, `height`, `color`, `x_offset`, or `y_offset` are not
        an integer or long, or if `opacity` is not a float.
    BadRequestError: If more than `MAX_TRANSFORMS_PER_REQUEST` compositions
        have been requested, if the canvas width or height is greater than
        4000 or less than or equal to 0, if the color is invalid, if the opacity
        is outside the range [0,1] for any composition option, or the anchor is
        invalid.
  """
  if (not isinstance(width, (int, long)) or
      not isinstance(height, (int, long)) or
      not isinstance(color, (int, long))):
    raise TypeError("Width, height and color must be integers.")
  if output_encoding not in OUTPUT_ENCODING_TYPES:
    raise BadRequestError("Output encoding type '%s' not in recognized set "
                          "%s" % (output_encoding, OUTPUT_ENCODING_TYPES))

  if quality is not None:
    if not isinstance(quality, (int, long)):
      raise TypeError("Quality must be an integer.")
    if quality > 100 or quality < 1:
      raise BadRequestError("Quality must be between 1 and 100.")

  if not inputs:
    raise BadRequestError("Must provide at least one input")
  if len(inputs) > MAX_COMPOSITES_PER_REQUEST:
    raise BadRequestError("A maximum of %d composition operations can be"
                          "performed in a single request" %
                          MAX_COMPOSITES_PER_REQUEST)

  if width <= 0 or height <= 0:
    raise BadRequestError("Width and height must be > 0.")
  if width > 4000 or height > 4000:
    raise BadRequestError("Width and height must be <= 4000.")

  if color > 0xffffffff or color < 0:
    raise BadRequestError("Invalid color")

  if color >= 0x80000000:
    color -= 0x100000000

  image_map = {}

  request = images_service_pb.ImagesCompositeRequest()
  response = images_service_pb.ImagesTransformResponse()
  for (image, x, y, opacity, anchor) in inputs:
    if not image:
      raise BadRequestError("Each input must include an image")
    if (not isinstance(x, (int, long)) or
        not isinstance(y, (int, long)) or
        not isinstance(opacity, (float))):
      raise TypeError("x_offset, y_offset must be integers and opacity must"
                      "be a float")
    if x > 4000 or x < -4000:
      raise BadRequestError("xOffsets must be in range [-4000, 4000]")
    if y > 4000 or y < -4000:
      raise BadRequestError("yOffsets must be in range [-4000, 4000]")
    if opacity < 0 or opacity > 1:
      raise BadRequestError("Opacity must be in the range 0.0 to 1.0")
    if anchor not in ANCHOR_TYPES:
      raise BadRequestError("Anchor type '%s' not in recognized set %s" %
                            (anchor, ANCHOR_TYPES))
    if image not in image_map:
      image_map[image] = request.image_size()

      if isinstance(image, Image):
        image._set_imagedata(request.add_image())
      else:
        request.add_image().set_content(image)

    option = request.add_options()
    option.set_x_offset(x)
    option.set_y_offset(y)
    option.set_opacity(opacity)
    option.set_anchor(anchor)
    option.set_source_index(image_map[image])

  request.mutable_canvas().mutable_output().set_mime_type(output_encoding)
  request.mutable_canvas().set_width(width)
  request.mutable_canvas().set_height(height)
  request.mutable_canvas().set_color(color)

  if ((output_encoding == JPEG or output_encoding == WEBP) and
      (quality is not None)):
      request.mutable_canvas().mutable_output().set_quality(quality)

  def composite_hook(rpc):
    """Checks success, handles exceptions, and returns the converted RPC result.

    Args:
      rpc: A UserRPC object.

    Returns:
      Images bytes of the composite image.

    Raises:
      See `composite_async` for more details.
    """
    try:
      rpc.check_success()
    except apiproxy_errors.ApplicationError, e:
      raise _ToImagesError(e)
    return rpc.response.image().content()

  return _make_async_call(rpc,
                          "Composite",
                          request,
                          response,
                          composite_hook,
                          None)


def histogram(image_data, rpc=None):
  """Calculates the histogram of the given image.

  Args:
    image_data: String of the source image data.
    rpc: An optional UserRPC object.

  Returns:
    3 256-element lists containing the number of occurences of each value of
    each color in the order RGB.

  Raises:
    NotImageError: If the supplied image data is not an image.
    BadImageError: If the supplied image data is corrupt.
    LargeImageError: If the supplied image data is too large to process.
    Error: All other errors.
  """
  rpc = histogram_async(image_data, rpc=rpc)
  return rpc.get_result()


def histogram_async(image_data, rpc=None):
  """Calculates the histogram of the given image - async version.

  Args:
    image_data: String of the source image data.
    rpc: An optional UserRPC object.

  Returns:
    An UserRPC object.

  Raises:
    NotImageError: If the supplied image data is not an image.
    BadImageError: If the supplied image data is corrupt.
    LargeImageError: If the supplied image data is too large to process.
    Error: All other errors.
  """
  image = Image(image_data)
  return image.histogram_async(rpc)


IMG_SERVING_SIZES_LIMIT = 3200


IMG_SERVING_SIZES = [
    32, 48, 64, 72, 80, 90, 94, 104, 110, 120, 128, 144,
    150, 160, 200, 220, 288, 320, 400, 512, 576, 640, 720,
    800, 912, 1024, 1152, 1280, 1440, 1600]


IMG_SERVING_CROP_SIZES = [32, 48, 64, 72, 80, 104, 136, 144, 150, 160]


def get_serving_url(blob_key,
                    size=None,
                    crop=False,
                    secure_url=None,
                    filename=None,
                    rpc=None):
  """Obtains a URL that will serve the underlying image.

  This URL is served by a high-performance dynamic image serving infrastructure.
  This URL format also allows dynamic resizing and cropping with certain
  restrictions. To dynamically resize and crop, specify size and crop arguments,
  or simply append options to the end of the default URL obtained via this call.

  Example::

      get_serving_url -> "http://lh3.ggpht.com/SomeCharactersGoesHere"


  To get a 32-pixel-sized version (aspect-ratio preserved), append `=s32` to the
  URL::

      http://lh3.ggpht.com/SomeCharactersGoesHere=s32


  To get a 32-pixel cropped version, append `=s32-c`::

      http://lh3.ggpht.com/SomeCharactersGoesHere=s32-c


  Available sizes are any integer in the range [0, 1600] and is available as
  `IMG_SERVING_SIZES_LIMIT`.

  Args:
    blob_key: The `BlobKey`, `BlobInfo`, string, or Unicode representation of
        `BlobKey` of the blob whose URL you require.
    size: Integer of the size of resulting images.
    crop: Boolean value. `True` requests a cropped image, while `False` requests
        a resized image.
    secure_url: Boolean value. `True` requests a `https` URL, while `False`
        requests a `http` URL.
    filename: The file name of a Google Storage object whose URL you require.
    rpc: Optional UserRPC object.

  Returns:
    A URL string.

  Raises:
    BlobKeyRequiredError: If a blob key was not specified in the constructor.
    UnsupportedSizeError: If you specified an invalid size parameter.
    BadRequestError: If `crop` and `size` are present in the wrong
        combination, or if `blob_key` and `filename` are both specified.
    TypeError: If `secure_url` is not a boolean type.
    AccessDeniedError: If `blob_key` refers to a Google Storage object and
        the application does not have permission to access the object.
    ObjectNotFoundError: If `blob_key` refers to an object that doesn't
        exist.
  """
  rpc = get_serving_url_async(blob_key, size, crop, secure_url, filename, rpc)
  return rpc.get_result()


def get_serving_url_async(blob_key,
                          size=None,
                          crop=False,
                          secure_url=None,
                          filename=None,
                          rpc=None):
  """Asynchronously obtains a URL that will serve the underlying image.

  This URL is served by a high-performance dynamic image serving infrastructure.
  This URL format also allows dynamic resizing and cropping with certain
  restrictions. To dynamically resize and crop, specify size and crop arguments,
  or simply append options to the end of the default URL obtained via this call.

  Example::

      get_serving_url -> "http://lh3.ggpht.com/SomeCharactersGoesHere"


  To get a 32-pixel-sized version (aspect-ratio preserved), append `=s32` to the
  URL::

      http://lh3.ggpht.com/SomeCharactersGoesHere=s32


  To get a 32-pixel cropped version, append `=s32-c`::

      http://lh3.ggpht.com/SomeCharactersGoesHere=s32-c


  Available sizes are any integer in the range [0, 1600] and is available as
  `IMG_SERVING_SIZES_LIMIT`.

  Args:
    blob_key: The `BlobKey`, `BlobInfo`, string, or Unicode representation of
        the `BlobKey` of blob whose URL you require.
    size: Integer of the size of resulting images.
    crop: Boolean value. `True` requests a cropped image, while `False` requests
        a resized image.
    secure_url: Boolean value. `True` requests a `https` URL, while `False`
        requests a `http` URL.
    filename: The file name of a Google Storage object whose URL you require.
    rpc: Optional UserRPC object.

  Returns:
    A UserRPC whose result will be a string that is the serving URL.

  Raises:
    BlobKeyRequiredError: If a blob key was not specified in the constructor.
    UnsupportedSizeError: If you specified an invalid size parameter.
    BadRequestError: If `crop` and `size` are present in the wrong
        combination, or if `blob_key` and `filename` are both specified.
    TypeError: If `secure_url` is not a boolean type.
    AccessDeniedError: If `blob_key` refers to a Google Storage object and
        the application does not have permission to access the object.
  """
  if not blob_key and not filename:
    raise BlobKeyRequiredError(
        "A Blobkey or a filename is required for this operation.")

  if crop and not size:
    raise BadRequestError("Size should be set for crop operation")

  if size is not None and (size > IMG_SERVING_SIZES_LIMIT or size < 0):
    raise UnsupportedSizeError("Unsupported size")

  if secure_url and not isinstance(secure_url, bool):
    raise TypeError("secure_url must be boolean.")

  if filename and blob_key:
    raise BadRequestError("Cannot specify a blob_key and a filename.");

  if filename:
    _blob_key = blobstore.create_gs_key(filename)
    readable_blob_key = filename
  else:
    _blob_key = _extract_blob_key(blob_key)
    readable_blob_key = blob_key

  request = images_service_pb.ImagesGetUrlBaseRequest()
  response = images_service_pb.ImagesGetUrlBaseResponse()

  request.set_blob_key(_blob_key)

  if secure_url:
    request.set_create_secure_url(secure_url)

  def get_serving_url_hook(rpc):
    """Checks success, handles exceptions, and returns a converted RPC result.

    Args:
      rpc: A UserRPC object.

    Returns:
      The URL to serve the image.

    Raises:
      See `get_serving_url` for more details.
    """
    try:
      rpc.check_success()
    except apiproxy_errors.ApplicationError, e:
      raise _ToImagesError(e, readable_blob_key)

    url = rpc.response.url()

    if size is not None:
      url += "=s%s" % size
    if crop:
      url += "-c"

    return url

  return _make_async_call(rpc,
                          "GetUrlBase",
                          request,
                          response,
                          get_serving_url_hook,
                          None)


def delete_serving_url(blob_key, rpc=None):
  """Deletes a serving URL created for `blob_key` using `get_serving_url`.

  Args:
    blob_key: The `BlobKey`, `BlobInfo`, string, or Unicode representation of
        the BlobKey of a blob with an existing URL that you want to delete.
    rpc: Optional UserRPC object.

  Raises:
    BlobKeyRequiredError: If no `blob_key` was specified.
    InvalidBlobKeyError: If the `blob_key` supplied was invalid.
    Error: There was a generic error deleting the serving URL.
  """
  rpc = delete_serving_url_async(blob_key, rpc)
  rpc.get_result()


def delete_serving_url_async(blob_key, rpc=None):
  """Deletes a serving URL created using get_serving_url - async version.

  Args:
    blob_key: `BlobKey`, `BlobInfo`, string, or unicode representation of the
        BlobKey of a blob with an existing URL that you want to delete.
    rpc: Optional UserRPC object.

  Returns:
    A UserRPC object.

  Raises:
    BlobKeyRequiredError: If no `blob_key` was specified.
    InvalidBlobKeyError: If the `blob_key` supplied was invalid.
    Error: There was a generic error deleting the serving URL.
  """
  if not blob_key:
    raise BlobKeyRequiredError("A Blobkey is required for this operation.")

  request = images_service_pb.ImagesDeleteUrlBaseRequest()
  response = images_service_pb.ImagesDeleteUrlBaseResponse()

  request.set_blob_key(_extract_blob_key(blob_key))

  def delete_serving_url_hook(rpc):
    """Checks success, handles exceptions, and returns the converted RPC result.

    Args:
      rpc: A UserRPC object.

    Raises:
      See `delete_serving_url_async` for more details.
    """
    try:
      rpc.check_success()
    except apiproxy_errors.ApplicationError, e:
      raise _ToImagesError(e, blob_key)

  return _make_async_call(rpc,
                          "DeleteUrlBase",
                          request,
                          response,
                          delete_serving_url_hook,
                          None)


def _extract_blob_key(blob):
  """Extracts a Unicode blob key from a string, `BlobKey`, or `BlobInfo`.

  Args:
    blob: The string, Unicode, `BlobKey`, or `BlobInfo` that contains the blob
        key.

  Returns:
    Unicode string representing the blob key.
  """
  if isinstance(blob, str):
    return blob.decode('utf-8')
  elif isinstance(blob, BlobKey):
    return str(blob).decode('utf-8')
  elif blob.__class__.__name__ == 'BlobInfo':



    return str(blob.key()).decode('utf-8')


  return blob
