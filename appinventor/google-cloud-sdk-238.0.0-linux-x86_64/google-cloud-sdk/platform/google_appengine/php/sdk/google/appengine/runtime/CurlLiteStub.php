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
// @codingStandardsIgnoreFile
/**
 * Generated extension stub script for the extension 'curl'.
 */

// Constant Definitions

if (!defined('CURLAUTH_ANYSAFE')) {
  define('CURLAUTH_ANYSAFE', -18);
}
if (!defined('CURLAUTH_ANY')) {
  define('CURLAUTH_ANY', -17);
}
if (!defined('CURLSSH_AUTH_DEFAULT')) {
  define('CURLSSH_AUTH_DEFAULT', -1);
}
if (!defined('CURLSSH_AUTH_ANY')) {
  define('CURLSSH_AUTH_ANY', -1);
}
if (!defined('CURLM_CALL_MULTI_PERFORM')) {
  define('CURLM_CALL_MULTI_PERFORM', -1);
}
if (!defined('CURLOPT_SAFE_UPLOAD')) {
  define('CURLOPT_SAFE_UPLOAD', -1);
}
if (!defined('CURLPROTO_ALL')) {
  define('CURLPROTO_ALL', -1);
}
if (!defined('CURL_TIMECOND_NONE')) {
  define('CURL_TIMECOND_NONE', 0);
}
if (!defined('CURL_NETRC_IGNORED')) {
  define('CURL_NETRC_IGNORED', 0);
}
if (!defined('CURLSHOPT_NONE')) {
  define('CURLSHOPT_NONE', 0);
}
if (!defined('CURLAUTH_NONE')) {
  define('CURLAUTH_NONE', 0);
}
if (!defined('CURLPROXY_HTTP')) {
  define('CURLPROXY_HTTP', 0);
}
if (!defined('CURLM_OK')) {
  define('CURLM_OK', 0);
}
if (!defined('CURL_HTTP_VERSION_NONE')) {
  define('CURL_HTTP_VERSION_NONE', 0);
}
if (!defined('CURLFTPAUTH_DEFAULT')) {
  define('CURLFTPAUTH_DEFAULT', 0);
}
if (!defined('CURLPAUSE_RECV_CONT')) {
  define('CURLPAUSE_RECV_CONT', 0);
}
if (!defined('CURLPAUSE_SEND_CONT')) {
  define('CURLPAUSE_SEND_CONT', 0);
}
if (!defined('CURLSSH_AUTH_NONE')) {
  define('CURLSSH_AUTH_NONE', 0);
}
if (!defined('CURL_FNMATCHFUNC_MATCH')) {
  define('CURL_FNMATCHFUNC_MATCH', 0);
}
if (!defined('CURLPAUSE_CONT')) {
  define('CURLPAUSE_CONT', 0);
}
if (!defined('CURLUSESSL_NONE')) {
  define('CURLUSESSL_NONE', 0);
}
if (!defined('CURLFTPSSL_NONE')) {
  define('CURLFTPSSL_NONE', 0);
}
if (!defined('CURLE_OK')) {
  define('CURLE_OK', 0);
}
if (!defined('CURLFTPSSL_CCC_NONE')) {
  define('CURLFTPSSL_CCC_NONE', 0);
}
if (!defined('CURL_IPRESOLVE_WHATEVER')) {
  define('CURL_IPRESOLVE_WHATEVER', 0);
}
if (!defined('CURL_SSLVERSION_DEFAULT')) {
  define('CURL_SSLVERSION_DEFAULT', 0);
}
if (!defined('CURLMSG_DONE')) {
  define('CURLMSG_DONE', 1);
}
if (!defined('CURLCLOSEPOLICY_OLDEST')) {
  define('CURLCLOSEPOLICY_OLDEST', 1);
}
if (!defined('CURL_RTSPREQ_OPTIONS')) {
  define('CURL_RTSPREQ_OPTIONS', 1);
}
if (!defined('CURLAUTH_BASIC')) {
  define('CURLAUTH_BASIC', 1);
}
if (!defined('CURL_TIMECOND_IFMODSINCE')) {
  define('CURL_TIMECOND_IFMODSINCE', 1);
}
if (!defined('CURLUSESSL_TRY')) {
  define('CURLUSESSL_TRY', 1);
}
if (!defined('CURL_IPRESOLVE_V4')) {
  define('CURL_IPRESOLVE_V4', 1);
}
if (!defined('CURLPROTO_HTTP')) {
  define('CURLPROTO_HTTP', 1);
}
if (!defined('CURLFTPSSL_CCC_PASSIVE')) {
  define('CURLFTPSSL_CCC_PASSIVE', 1);
}
if (!defined('CURLPAUSE_RECV')) {
  define('CURLPAUSE_RECV', 1);
}
if (!defined('CURLFTPMETHOD_MULTICWD')) {
  define('CURLFTPMETHOD_MULTICWD', 1);
}
if (!defined('CURLSSH_AUTH_PUBLICKEY')) {
  define('CURLSSH_AUTH_PUBLICKEY', 1);
}
if (!defined('CURLFTPSSL_TRY')) {
  define('CURLFTPSSL_TRY', 1);
}
if (!defined('CURLFTPAUTH_SSL')) {
  define('CURLFTPAUTH_SSL', 1);
}
if (!defined('CURL_SSLVERSION_TLSv1')) {
  define('CURL_SSLVERSION_TLSv1', 1);
}
if (!defined('CURL_VERSION_IPV6')) {
  define('CURL_VERSION_IPV6', 1);
}
if (!defined('CURL_FNMATCHFUNC_NOMATCH')) {
  define('CURL_FNMATCHFUNC_NOMATCH', 1);
}
if (!defined('CURL_HTTP_VERSION_1_0')) {
  define('CURL_HTTP_VERSION_1_0', 1);
}
if (!defined('CURLGSSAPI_DELEGATION_POLICY_FLAG')) {
  define('CURLGSSAPI_DELEGATION_POLICY_FLAG', 1);
}
if (!defined('CURLSSLOPT_ALLOW_BEAST')) {
  define('CURLSSLOPT_ALLOW_BEAST', 1);
}
if (!defined('CURLM_BAD_HANDLE')) {
  define('CURLM_BAD_HANDLE', 1);
}
if (!defined('CURLSHOPT_SHARE')) {
  define('CURLSHOPT_SHARE', 1);
}
if (!defined('CURLE_UNSUPPORTED_PROTOCOL')) {
  define('CURLE_UNSUPPORTED_PROTOCOL', 1);
}
if (!defined('CURL_TLSAUTH_SRP')) {
  define('CURL_TLSAUTH_SRP', 1);
}
if (!defined('CURL_NETRC_OPTIONAL')) {
  define('CURL_NETRC_OPTIONAL', 1);
}
if (!defined('CURLINFO_HEADER_OUT')) {
  define('CURLINFO_HEADER_OUT', 2);
}
if (!defined('CURLFTPAUTH_TLS')) {
  define('CURLFTPAUTH_TLS', 2);
}
if (!defined('CURLFTPMETHOD_NOCWD')) {
  define('CURLFTPMETHOD_NOCWD', 2);
}
if (!defined('CURLFTPSSL_CCC_ACTIVE')) {
  define('CURLFTPSSL_CCC_ACTIVE', 2);
}
if (!defined('CURLM_BAD_EASY_HANDLE')) {
  define('CURLM_BAD_EASY_HANDLE', 2);
}
if (!defined('CURLUSESSL_CONTROL')) {
  define('CURLUSESSL_CONTROL', 2);
}
if (!defined('CURL_NETRC_REQUIRED')) {
  define('CURL_NETRC_REQUIRED', 2);
}
if (!defined('CURL_TIMECOND_IFUNMODSINCE')) {
  define('CURL_TIMECOND_IFUNMODSINCE', 2);
}
if (!defined('CURL_LOCK_DATA_COOKIE')) {
  define('CURL_LOCK_DATA_COOKIE', 2);
}
if (!defined('CURLCLOSEPOLICY_LEAST_RECENTLY_USED')) {
  define('CURLCLOSEPOLICY_LEAST_RECENTLY_USED', 2);
}
if (!defined('CURL_VERSION_KERBEROS4')) {
  define('CURL_VERSION_KERBEROS4', 2);
}
if (!defined('CURLE_FAILED_INIT')) {
  define('CURLE_FAILED_INIT', 2);
}
if (!defined('CURL_SSLVERSION_SSLv2')) {
  define('CURL_SSLVERSION_SSLv2', 2);
}
if (!defined('CURL_IPRESOLVE_V6')) {
  define('CURL_IPRESOLVE_V6', 2);
}
if (!defined('CURLSHOPT_UNSHARE')) {
  define('CURLSHOPT_UNSHARE', 2);
}
if (!defined('CURLFTPSSL_CONTROL')) {
  define('CURLFTPSSL_CONTROL', 2);
}
if (!defined('CURL_HTTP_VERSION_1_1')) {
  define('CURL_HTTP_VERSION_1_1', 2);
}
if (!defined('CURLAUTH_DIGEST')) {
  define('CURLAUTH_DIGEST', 2);
}
if (!defined('CURLGSSAPI_DELEGATION_FLAG')) {
  define('CURLGSSAPI_DELEGATION_FLAG', 2);
}
if (!defined('CURLPROTO_HTTPS')) {
  define('CURLPROTO_HTTPS', 2);
}
if (!defined('CURL_RTSPREQ_DESCRIBE')) {
  define('CURL_RTSPREQ_DESCRIBE', 2);
}
if (!defined('CURL_FNMATCHFUNC_FAIL')) {
  define('CURL_FNMATCHFUNC_FAIL', 2);
}
if (!defined('CURLSSH_AUTH_PASSWORD')) {
  define('CURLSSH_AUTH_PASSWORD', 2);
}
if (!defined('CURLUSESSL_ALL')) {
  define('CURLUSESSL_ALL', 3);
}
if (!defined('CURL_SSLVERSION_SSLv3')) {
  define('CURL_SSLVERSION_SSLv3', 3);
}
if (!defined('CURLM_OUT_OF_MEMORY')) {
  define('CURLM_OUT_OF_MEMORY', 3);
}
if (!defined('CURLFTPSSL_ALL')) {
  define('CURLFTPSSL_ALL', 3);
}
if (!defined('CURL_TIMECOND_LASTMOD')) {
  define('CURL_TIMECOND_LASTMOD', 3);
}
if (!defined('CURL_RTSPREQ_ANNOUNCE')) {
  define('CURL_RTSPREQ_ANNOUNCE', 3);
}
if (!defined('CURLCLOSEPOLICY_LEAST_TRAFFIC')) {
  define('CURLCLOSEPOLICY_LEAST_TRAFFIC', 3);
}
if (!defined('CURLE_URL_MALFORMAT')) {
  define('CURLE_URL_MALFORMAT', 3);
}
if (!defined('CURLOPT_PORT')) {
  define('CURLOPT_PORT', 3);
}
if (!defined('CURLMOPT_PIPELINING')) {
  define('CURLMOPT_PIPELINING', 3);
}
if (!defined('CURLFTPMETHOD_SINGLECWD')) {
  define('CURLFTPMETHOD_SINGLECWD', 3);
}
if (!defined('CURL_LOCK_DATA_DNS')) {
  define('CURL_LOCK_DATA_DNS', 3);
}
if (!defined('CURLVERSION_NOW')) {
  define('CURLVERSION_NOW', 3);
}
if (!defined('CURL_LOCK_DATA_SSL_SESSION')) {
  define('CURL_LOCK_DATA_SSL_SESSION', 4);
}
if (!defined('CURL_VERSION_SSL')) {
  define('CURL_VERSION_SSL', 4);
}
if (!defined('CURLCLOSEPOLICY_SLOWEST')) {
  define('CURLCLOSEPOLICY_SLOWEST', 4);
}
if (!defined('CURL_RTSPREQ_SETUP')) {
  define('CURL_RTSPREQ_SETUP', 4);
}
if (!defined('CURLAUTH_GSSNEGOTIATE')) {
  define('CURLAUTH_GSSNEGOTIATE', 4);
}
if (!defined('CURL_SSLVERSION_TLSv1_0')) {
  define('CURL_SSLVERSION_TLSv1_0', 4);
}
if (!defined('CURLE_URL_MALFORMAT_USER')) {
  define('CURLE_URL_MALFORMAT_USER', 4);
}
if (!defined('CURLPROTO_FTP')) {
  define('CURLPROTO_FTP', 4);
}
if (!defined('CURLM_INTERNAL_ERROR')) {
  define('CURLM_INTERNAL_ERROR', 4);
}
if (!defined('CURLSSH_AUTH_HOST')) {
  define('CURLSSH_AUTH_HOST', 4);
}
if (!defined('CURLPAUSE_SEND')) {
  define('CURLPAUSE_SEND', 4);
}
if (!defined('CURLPROXY_SOCKS4')) {
  define('CURLPROXY_SOCKS4', 4);
}
if (!defined('CURLE_COULDNT_RESOLVE_PROXY')) {
  define('CURLE_COULDNT_RESOLVE_PROXY', 5);
}
if (!defined('CURLCLOSEPOLICY_CALLBACK')) {
  define('CURLCLOSEPOLICY_CALLBACK', 5);
}
if (!defined('CURL_RTSPREQ_PLAY')) {
  define('CURL_RTSPREQ_PLAY', 5);
}
if (!defined('CURL_SSLVERSION_TLSv1_1')) {
  define('CURL_SSLVERSION_TLSv1_1', 5);
}
if (!defined('CURLPAUSE_ALL')) {
  define('CURLPAUSE_ALL', 5);
}
if (!defined('CURLPROXY_SOCKS5')) {
  define('CURLPROXY_SOCKS5', 5);
}
if (!defined('CURLE_COULDNT_RESOLVE_HOST')) {
  define('CURLE_COULDNT_RESOLVE_HOST', 6);
}
if (!defined('CURLMOPT_MAXCONNECTS')) {
  define('CURLMOPT_MAXCONNECTS', 6);
}
if (!defined('CURL_SSLVERSION_TLSv1_2')) {
  define('CURL_SSLVERSION_TLSv1_2', 6);
}
if (!defined('CURL_RTSPREQ_PAUSE')) {
  define('CURL_RTSPREQ_PAUSE', 6);
}
if (!defined('CURL_RTSPREQ_TEARDOWN')) {
  define('CURL_RTSPREQ_TEARDOWN', 7);
}
if (!defined('CURLE_COULDNT_CONNECT')) {
  define('CURLE_COULDNT_CONNECT', 7);
}
if (!defined('CURL_RTSPREQ_GET_PARAMETER')) {
  define('CURL_RTSPREQ_GET_PARAMETER', 8);
}
if (!defined('CURLE_FTP_WEIRD_SERVER_REPLY')) {
  define('CURLE_FTP_WEIRD_SERVER_REPLY', 8);
}
if (!defined('CURLPROTO_FTPS')) {
  define('CURLPROTO_FTPS', 8);
}
if (!defined('CURL_VERSION_LIBZ')) {
  define('CURL_VERSION_LIBZ', 8);
}
if (!defined('CURLSSH_AUTH_KEYBOARD')) {
  define('CURLSSH_AUTH_KEYBOARD', 8);
}
if (!defined('CURLAUTH_NTLM')) {
  define('CURLAUTH_NTLM', 8);
}
if (!defined('CURLE_FTP_ACCESS_DENIED')) {
  define('CURLE_FTP_ACCESS_DENIED', 9);
}
if (!defined('CURL_RTSPREQ_SET_PARAMETER')) {
  define('CURL_RTSPREQ_SET_PARAMETER', 9);
}
if (!defined('CURLE_FTP_USER_PASSWORD_INCORRECT')) {
  define('CURLE_FTP_USER_PASSWORD_INCORRECT', 10);
}
if (!defined('CURL_RTSPREQ_RECORD')) {
  define('CURL_RTSPREQ_RECORD', 10);
}
if (!defined('CURLE_FTP_WEIRD_PASS_REPLY')) {
  define('CURLE_FTP_WEIRD_PASS_REPLY', 11);
}
if (!defined('CURL_RTSPREQ_RECEIVE')) {
  define('CURL_RTSPREQ_RECEIVE', 11);
}
if (!defined('CURLE_FTP_WEIRD_USER_REPLY')) {
  define('CURLE_FTP_WEIRD_USER_REPLY', 12);
}
if (!defined('CURLE_FTP_WEIRD_PASV_REPLY')) {
  define('CURLE_FTP_WEIRD_PASV_REPLY', 13);
}
if (!defined('CURLOPT_TIMEOUT')) {
  define('CURLOPT_TIMEOUT', 13);
}
if (!defined('CURLOPT_INFILESIZE')) {
  define('CURLOPT_INFILESIZE', 14);
}
if (!defined('CURLE_FTP_WEIRD_227_FORMAT')) {
  define('CURLE_FTP_WEIRD_227_FORMAT', 14);
}
if (!defined('CURLE_FTP_CANT_GET_HOST')) {
  define('CURLE_FTP_CANT_GET_HOST', 15);
}
if (!defined('CURLE_FTP_CANT_RECONNECT')) {
  define('CURLE_FTP_CANT_RECONNECT', 16);
}
if (!defined('CURLAUTH_DIGEST_IE')) {
  define('CURLAUTH_DIGEST_IE', 16);
}
if (!defined('CURLPROTO_SCP')) {
  define('CURLPROTO_SCP', 16);
}
if (!defined('CURLE_FTP_COULDNT_SET_BINARY')) {
  define('CURLE_FTP_COULDNT_SET_BINARY', 17);
}
if (!defined('CURLE_PARTIAL_FILE')) {
  define('CURLE_PARTIAL_FILE', 18);
}
if (!defined('CURLE_FTP_PARTIAL_FILE')) {
  define('CURLE_FTP_PARTIAL_FILE', 18);
}
if (!defined('CURLOPT_LOW_SPEED_LIMIT')) {
  define('CURLOPT_LOW_SPEED_LIMIT', 19);
}
if (!defined('CURLE_FTP_COULDNT_RETR_FILE')) {
  define('CURLE_FTP_COULDNT_RETR_FILE', 19);
}
if (!defined('CURLOPT_LOW_SPEED_TIME')) {
  define('CURLOPT_LOW_SPEED_TIME', 20);
}
if (!defined('CURLE_FTP_WRITE_ERROR')) {
  define('CURLE_FTP_WRITE_ERROR', 20);
}
if (!defined('CURLE_FTP_QUOTE_ERROR')) {
  define('CURLE_FTP_QUOTE_ERROR', 21);
}
if (!defined('CURLOPT_RESUME_FROM')) {
  define('CURLOPT_RESUME_FROM', 21);
}
if (!defined('CURLE_HTTP_RETURNED_ERROR')) {
  define('CURLE_HTTP_RETURNED_ERROR', 22);
}
if (!defined('CURLE_HTTP_NOT_FOUND')) {
  define('CURLE_HTTP_NOT_FOUND', 22);
}
if (!defined('CURLE_WRITE_ERROR')) {
  define('CURLE_WRITE_ERROR', 23);
}
if (!defined('CURLE_MALFORMAT_USER')) {
  define('CURLE_MALFORMAT_USER', 24);
}
if (!defined('CURLE_FTP_COULDNT_STOR_FILE')) {
  define('CURLE_FTP_COULDNT_STOR_FILE', 25);
}
if (!defined('CURLE_READ_ERROR')) {
  define('CURLE_READ_ERROR', 26);
}
if (!defined('CURLOPT_CRLF')) {
  define('CURLOPT_CRLF', 27);
}
if (!defined('CURLE_OUT_OF_MEMORY')) {
  define('CURLE_OUT_OF_MEMORY', 27);
}
if (!defined('CURLE_OPERATION_TIMEDOUT')) {
  define('CURLE_OPERATION_TIMEDOUT', 28);
}
if (!defined('CURLE_OPERATION_TIMEOUTED')) {
  define('CURLE_OPERATION_TIMEOUTED', 28);
}
if (!defined('CURLE_FTP_COULDNT_SET_ASCII')) {
  define('CURLE_FTP_COULDNT_SET_ASCII', 29);
}
if (!defined('CURLE_FTP_PORT_FAILED')) {
  define('CURLE_FTP_PORT_FAILED', 30);
}
if (!defined('CURLE_FTP_COULDNT_USE_REST')) {
  define('CURLE_FTP_COULDNT_USE_REST', 31);
}
if (!defined('CURLOPT_SSLVERSION')) {
  define('CURLOPT_SSLVERSION', 32);
}
if (!defined('CURLPROTO_SFTP')) {
  define('CURLPROTO_SFTP', 32);
}
if (!defined('CURLE_FTP_COULDNT_GET_SIZE')) {
  define('CURLE_FTP_COULDNT_GET_SIZE', 32);
}
if (!defined('CURLOPT_TIMECONDITION')) {
  define('CURLOPT_TIMECONDITION', 33);
}
if (!defined('CURLE_HTTP_RANGE_ERROR')) {
  define('CURLE_HTTP_RANGE_ERROR', 33);
}
if (!defined('CURLE_HTTP_POST_ERROR')) {
  define('CURLE_HTTP_POST_ERROR', 34);
}
if (!defined('CURLOPT_TIMEVALUE')) {
  define('CURLOPT_TIMEVALUE', 34);
}
if (!defined('CURLE_SSL_CONNECT_ERROR')) {
  define('CURLE_SSL_CONNECT_ERROR', 35);
}
if (!defined('CURLE_BAD_DOWNLOAD_RESUME')) {
  define('CURLE_BAD_DOWNLOAD_RESUME', 36);
}
if (!defined('CURLE_FTP_BAD_DOWNLOAD_RESUME')) {
  define('CURLE_FTP_BAD_DOWNLOAD_RESUME', 36);
}
if (!defined('CURLE_FILE_COULDNT_READ_FILE')) {
  define('CURLE_FILE_COULDNT_READ_FILE', 37);
}
if (!defined('CURLE_LDAP_CANNOT_BIND')) {
  define('CURLE_LDAP_CANNOT_BIND', 38);
}
if (!defined('CURLE_LDAP_SEARCH_FAILED')) {
  define('CURLE_LDAP_SEARCH_FAILED', 39);
}
if (!defined('CURLE_LIBRARY_NOT_FOUND')) {
  define('CURLE_LIBRARY_NOT_FOUND', 40);
}
if (!defined('CURLE_FUNCTION_NOT_FOUND')) {
  define('CURLE_FUNCTION_NOT_FOUND', 41);
}
if (!defined('CURLOPT_VERBOSE')) {
  define('CURLOPT_VERBOSE', 41);
}
if (!defined('CURLOPT_HEADER')) {
  define('CURLOPT_HEADER', 42);
}
if (!defined('CURLE_ABORTED_BY_CALLBACK')) {
  define('CURLE_ABORTED_BY_CALLBACK', 42);
}
if (!defined('CURLINFO_LASTONE')) {
  define('CURLINFO_LASTONE', 43);
}
if (!defined('CURLOPT_NOPROGRESS')) {
  define('CURLOPT_NOPROGRESS', 43);
}
if (!defined('CURLE_BAD_FUNCTION_ARGUMENT')) {
  define('CURLE_BAD_FUNCTION_ARGUMENT', 43);
}
if (!defined('CURLOPT_NOBODY')) {
  define('CURLOPT_NOBODY', 44);
}
if (!defined('CURLE_BAD_CALLING_ORDER')) {
  define('CURLE_BAD_CALLING_ORDER', 44);
}
if (!defined('CURLOPT_FAILONERROR')) {
  define('CURLOPT_FAILONERROR', 45);
}
if (!defined('CURLE_HTTP_PORT_FAILED')) {
  define('CURLE_HTTP_PORT_FAILED', 45);
}
if (!defined('CURLE_BAD_PASSWORD_ENTERED')) {
  define('CURLE_BAD_PASSWORD_ENTERED', 46);
}
if (!defined('CURLOPT_UPLOAD')) {
  define('CURLOPT_UPLOAD', 46);
}
if (!defined('CURLE_TOO_MANY_REDIRECTS')) {
  define('CURLE_TOO_MANY_REDIRECTS', 47);
}
if (!defined('CURLOPT_POST')) {
  define('CURLOPT_POST', 47);
}
if (!defined('CURLE_UNKNOWN_TELNET_OPTION')) {
  define('CURLE_UNKNOWN_TELNET_OPTION', 48);
}
if (!defined('CURLOPT_DIRLISTONLY')) {
  define('CURLOPT_DIRLISTONLY', 48);
}
if (!defined('CURLOPT_FTPLISTONLY')) {
  define('CURLOPT_FTPLISTONLY', 48);
}
if (!defined('CURLE_TELNET_OPTION_SYNTAX')) {
  define('CURLE_TELNET_OPTION_SYNTAX', 49);
}
if (!defined('CURLOPT_FTPAPPEND')) {
  define('CURLOPT_FTPAPPEND', 50);
}
if (!defined('CURLE_OBSOLETE')) {
  define('CURLE_OBSOLETE', 50);
}
if (!defined('CURLOPT_APPEND')) {
  define('CURLOPT_APPEND', 50);
}
if (!defined('CURLOPT_NETRC')) {
  define('CURLOPT_NETRC', 51);
}
if (!defined('CURLE_SSL_PEER_CERTIFICATE')) {
  define('CURLE_SSL_PEER_CERTIFICATE', 51);
}
if (!defined('CURLOPT_FOLLOWLOCATION')) {
  define('CURLOPT_FOLLOWLOCATION', 52);
}
if (!defined('CURLE_GOT_NOTHING')) {
  define('CURLE_GOT_NOTHING', 52);
}
if (!defined('CURLE_SSL_ENGINE_NOTFOUND')) {
  define('CURLE_SSL_ENGINE_NOTFOUND', 53);
}
if (!defined('CURLOPT_TRANSFERTEXT')) {
  define('CURLOPT_TRANSFERTEXT', 53);
}
if (!defined('CURLOPT_PUT')) {
  define('CURLOPT_PUT', 54);
}
if (!defined('CURLE_SSL_ENGINE_SETFAILED')) {
  define('CURLE_SSL_ENGINE_SETFAILED', 54);
}
if (!defined('CURLE_SEND_ERROR')) {
  define('CURLE_SEND_ERROR', 55);
}
if (!defined('CURLE_RECV_ERROR')) {
  define('CURLE_RECV_ERROR', 56);
}
if (!defined('CURLE_SHARE_IN_USE')) {
  define('CURLE_SHARE_IN_USE', 57);
}
if (!defined('CURLE_SSL_CERTPROBLEM')) {
  define('CURLE_SSL_CERTPROBLEM', 58);
}
if (!defined('CURLOPT_AUTOREFERER')) {
  define('CURLOPT_AUTOREFERER', 58);
}
if (!defined('CURLOPT_PROXYPORT')) {
  define('CURLOPT_PROXYPORT', 59);
}
if (!defined('CURLE_SSL_CIPHER')) {
  define('CURLE_SSL_CIPHER', 59);
}
if (!defined('CURLE_SSL_CACERT')) {
  define('CURLE_SSL_CACERT', 60);
}
if (!defined('CURLE_BAD_CONTENT_ENCODING')) {
  define('CURLE_BAD_CONTENT_ENCODING', 61);
}
if (!defined('CURLOPT_HTTPPROXYTUNNEL')) {
  define('CURLOPT_HTTPPROXYTUNNEL', 61);
}
if (!defined('CURLE_LDAP_INVALID_URL')) {
  define('CURLE_LDAP_INVALID_URL', 62);
}
if (!defined('CURLE_FILESIZE_EXCEEDED')) {
  define('CURLE_FILESIZE_EXCEEDED', 63);
}
if (!defined('CURLE_FTP_SSL_FAILED')) {
  define('CURLE_FTP_SSL_FAILED', 64);
}
if (!defined('CURLPROTO_TELNET')) {
  define('CURLPROTO_TELNET', 64);
}
if (!defined('CURLOPT_SSL_VERIFYPEER')) {
  define('CURLOPT_SSL_VERIFYPEER', 64);
}
if (!defined('CURLOPT_MAXREDIRS')) {
  define('CURLOPT_MAXREDIRS', 68);
}
if (!defined('CURLOPT_FILETIME')) {
  define('CURLOPT_FILETIME', 69);
}
if (!defined('CURLOPT_MAXCONNECTS')) {
  define('CURLOPT_MAXCONNECTS', 71);
}
if (!defined('CURLOPT_CLOSEPOLICY')) {
  define('CURLOPT_CLOSEPOLICY', 72);
}
if (!defined('CURLOPT_FRESH_CONNECT')) {
  define('CURLOPT_FRESH_CONNECT', 74);
}
if (!defined('CURLOPT_FORBID_REUSE')) {
  define('CURLOPT_FORBID_REUSE', 75);
}
if (!defined('CURLOPT_CONNECTTIMEOUT')) {
  define('CURLOPT_CONNECTTIMEOUT', 78);
}
if (!defined('CURLE_SSH')) {
  define('CURLE_SSH', 79);
}
if (!defined('CURLOPT_HTTPGET')) {
  define('CURLOPT_HTTPGET', 80);
}
if (!defined('CURLOPT_SSL_VERIFYHOST')) {
  define('CURLOPT_SSL_VERIFYHOST', 81);
}
if (!defined('CURLOPT_HTTP_VERSION')) {
  define('CURLOPT_HTTP_VERSION', 84);
}
if (!defined('CURLOPT_FTP_USE_EPSV')) {
  define('CURLOPT_FTP_USE_EPSV', 85);
}
if (!defined('CURLOPT_SSLENGINE_DEFAULT')) {
  define('CURLOPT_SSLENGINE_DEFAULT', 90);
}
if (!defined('CURLOPT_DNS_USE_GLOBAL_CACHE')) {
  define('CURLOPT_DNS_USE_GLOBAL_CACHE', 91);
}
if (!defined('CURLOPT_DNS_CACHE_TIMEOUT')) {
  define('CURLOPT_DNS_CACHE_TIMEOUT', 92);
}
if (!defined('CURLOPT_COOKIESESSION')) {
  define('CURLOPT_COOKIESESSION', 96);
}
if (!defined('CURLOPT_BUFFERSIZE')) {
  define('CURLOPT_BUFFERSIZE', 98);
}
if (!defined('CURLOPT_NOSIGNAL')) {
  define('CURLOPT_NOSIGNAL', 99);
}
if (!defined('CURLOPT_PROXYTYPE')) {
  define('CURLOPT_PROXYTYPE', 101);
}
if (!defined('CURLOPT_UNRESTRICTED_AUTH')) {
  define('CURLOPT_UNRESTRICTED_AUTH', 105);
}
if (!defined('CURLOPT_FTP_USE_EPRT')) {
  define('CURLOPT_FTP_USE_EPRT', 106);
}
if (!defined('CURLOPT_HTTPAUTH')) {
  define('CURLOPT_HTTPAUTH', 107);
}
if (!defined('CURLOPT_FTP_CREATE_MISSING_DIRS')) {
  define('CURLOPT_FTP_CREATE_MISSING_DIRS', 110);
}
if (!defined('CURLOPT_PROXYAUTH')) {
  define('CURLOPT_PROXYAUTH', 111);
}
if (!defined('CURLOPT_FTP_RESPONSE_TIMEOUT')) {
  define('CURLOPT_FTP_RESPONSE_TIMEOUT', 112);
}
if (!defined('CURLOPT_IPRESOLVE')) {
  define('CURLOPT_IPRESOLVE', 113);
}
if (!defined('CURLOPT_MAXFILESIZE')) {
  define('CURLOPT_MAXFILESIZE', 114);
}
if (!defined('CURLOPT_FTP_SSL')) {
  define('CURLOPT_FTP_SSL', 119);
}
if (!defined('CURLOPT_USE_SSL')) {
  define('CURLOPT_USE_SSL', 119);
}
if (!defined('CURLOPT_TCP_NODELAY')) {
  define('CURLOPT_TCP_NODELAY', 121);
}
if (!defined('CURLPROTO_LDAP')) {
  define('CURLPROTO_LDAP', 128);
}
if (!defined('CURLOPT_FTPSSLAUTH')) {
  define('CURLOPT_FTPSSLAUTH', 129);
}
if (!defined('CURLOPT_IGNORE_CONTENT_LENGTH')) {
  define('CURLOPT_IGNORE_CONTENT_LENGTH', 136);
}
if (!defined('CURLOPT_FTP_SKIP_PASV_IP')) {
  define('CURLOPT_FTP_SKIP_PASV_IP', 137);
}
if (!defined('CURLOPT_FTP_FILEMETHOD')) {
  define('CURLOPT_FTP_FILEMETHOD', 138);
}
if (!defined('CURLOPT_LOCALPORT')) {
  define('CURLOPT_LOCALPORT', 139);
}
if (!defined('CURLOPT_LOCALPORTRANGE')) {
  define('CURLOPT_LOCALPORTRANGE', 140);
}
if (!defined('CURLOPT_CONNECT_ONLY')) {
  define('CURLOPT_CONNECT_ONLY', 141);
}
if (!defined('CURLOPT_SSL_SESSIONID_CACHE')) {
  define('CURLOPT_SSL_SESSIONID_CACHE', 150);
}
if (!defined('CURLOPT_SSH_AUTH_TYPES')) {
  define('CURLOPT_SSH_AUTH_TYPES', 151);
}
if (!defined('CURLOPT_FTP_SSL_CCC')) {
  define('CURLOPT_FTP_SSL_CCC', 154);
}
if (!defined('CURLOPT_TIMEOUT_MS')) {
  define('CURLOPT_TIMEOUT_MS', 155);
}
if (!defined('CURLOPT_CONNECTTIMEOUT_MS')) {
  define('CURLOPT_CONNECTTIMEOUT_MS', 156);
}
if (!defined('CURLOPT_HTTP_TRANSFER_DECODING')) {
  define('CURLOPT_HTTP_TRANSFER_DECODING', 157);
}
if (!defined('CURLOPT_HTTP_CONTENT_DECODING')) {
  define('CURLOPT_HTTP_CONTENT_DECODING', 158);
}
if (!defined('CURLOPT_NEW_FILE_PERMS')) {
  define('CURLOPT_NEW_FILE_PERMS', 159);
}
if (!defined('CURLOPT_NEW_DIRECTORY_PERMS')) {
  define('CURLOPT_NEW_DIRECTORY_PERMS', 160);
}
if (!defined('CURLOPT_POSTREDIR')) {
  define('CURLOPT_POSTREDIR', 161);
}
if (!defined('CURLOPT_PROXY_TRANSFER_MODE')) {
  define('CURLOPT_PROXY_TRANSFER_MODE', 166);
}
if (!defined('CURLOPT_ADDRESS_SCOPE')) {
  define('CURLOPT_ADDRESS_SCOPE', 171);
}
if (!defined('CURLOPT_CERTINFO')) {
  define('CURLOPT_CERTINFO', 172);
}
if (!defined('CURLOPT_TFTP_BLKSIZE')) {
  define('CURLOPT_TFTP_BLKSIZE', 178);
}
if (!defined('CURLOPT_SOCKS5_GSSAPI_NEC')) {
  define('CURLOPT_SOCKS5_GSSAPI_NEC', 180);
}
if (!defined('CURLOPT_PROTOCOLS')) {
  define('CURLOPT_PROTOCOLS', 181);
}
if (!defined('CURLOPT_REDIR_PROTOCOLS')) {
  define('CURLOPT_REDIR_PROTOCOLS', 182);
}
if (!defined('CURLOPT_FTP_USE_PRET')) {
  define('CURLOPT_FTP_USE_PRET', 188);
}
if (!defined('CURLOPT_RTSP_REQUEST')) {
  define('CURLOPT_RTSP_REQUEST', 189);
}
if (!defined('CURLOPT_RTSP_CLIENT_CSEQ')) {
  define('CURLOPT_RTSP_CLIENT_CSEQ', 193);
}
if (!defined('CURLOPT_RTSP_SERVER_CSEQ')) {
  define('CURLOPT_RTSP_SERVER_CSEQ', 194);
}
if (!defined('CURLOPT_WILDCARDMATCH')) {
  define('CURLOPT_WILDCARDMATCH', 197);
}
if (!defined('CURLOPT_TRANSFER_ENCODING')) {
  define('CURLOPT_TRANSFER_ENCODING', 207);
}
if (!defined('CURLOPT_GSSAPI_DELEGATION')) {
  define('CURLOPT_GSSAPI_DELEGATION', 210);
}
if (!defined('CURLOPT_ACCEPTTIMEOUT_MS')) {
  define('CURLOPT_ACCEPTTIMEOUT_MS', 212);
}
if (!defined('CURLOPT_TCP_KEEPALIVE')) {
  define('CURLOPT_TCP_KEEPALIVE', 213);
}
if (!defined('CURLOPT_TCP_KEEPIDLE')) {
  define('CURLOPT_TCP_KEEPIDLE', 214);
}
if (!defined('CURLOPT_TCP_KEEPINTVL')) {
  define('CURLOPT_TCP_KEEPINTVL', 215);
}
if (!defined('CURLOPT_SSL_OPTIONS')) {
  define('CURLOPT_SSL_OPTIONS', 216);
}
if (!defined('CURLPROTO_LDAPS')) {
  define('CURLPROTO_LDAPS', 256);
}
if (!defined('CURLPROTO_DICT')) {
  define('CURLPROTO_DICT', 512);
}
if (!defined('CURLPROTO_FILE')) {
  define('CURLPROTO_FILE', 1024);
}
if (!defined('CURLPROTO_TFTP')) {
  define('CURLPROTO_TFTP', 2048);
}
if (!defined('CURLPROTO_IMAP')) {
  define('CURLPROTO_IMAP', 4096);
}
if (!defined('CURLPROTO_IMAPS')) {
  define('CURLPROTO_IMAPS', 8192);
}
if (!defined('CURLOPT_FILE')) {
  define('CURLOPT_FILE', 10001);
}
if (!defined('CURLOPT_URL')) {
  define('CURLOPT_URL', 10002);
}
if (!defined('CURLOPT_PROXY')) {
  define('CURLOPT_PROXY', 10004);
}
if (!defined('CURLOPT_USERPWD')) {
  define('CURLOPT_USERPWD', 10005);
}
if (!defined('CURLOPT_PROXYUSERPWD')) {
  define('CURLOPT_PROXYUSERPWD', 10006);
}
if (!defined('CURLOPT_RANGE')) {
  define('CURLOPT_RANGE', 10007);
}
if (!defined('CURLOPT_INFILE')) {
  define('CURLOPT_INFILE', 10009);
}
if (!defined('CURLOPT_READDATA')) {
  define('CURLOPT_READDATA', 10009);
}
if (!defined('CURLOPT_POSTFIELDS')) {
  define('CURLOPT_POSTFIELDS', 10015);
}
if (!defined('CURLOPT_REFERER')) {
  define('CURLOPT_REFERER', 10016);
}
if (!defined('CURLOPT_FTPPORT')) {
  define('CURLOPT_FTPPORT', 10017);
}
if (!defined('CURLOPT_USERAGENT')) {
  define('CURLOPT_USERAGENT', 10018);
}
if (!defined('CURLOPT_COOKIE')) {
  define('CURLOPT_COOKIE', 10022);
}
if (!defined('CURLOPT_HTTPHEADER')) {
  define('CURLOPT_HTTPHEADER', 10023);
}
if (!defined('CURLOPT_SSLCERT')) {
  define('CURLOPT_SSLCERT', 10025);
}
if (!defined('CURLOPT_KEYPASSWD')) {
  define('CURLOPT_KEYPASSWD', 10026);
}
if (!defined('CURLOPT_SSLCERTPASSWD')) {
  define('CURLOPT_SSLCERTPASSWD', 10026);
}
if (!defined('CURLOPT_SSLKEYPASSWD')) {
  define('CURLOPT_SSLKEYPASSWD', 10026);
}
if (!defined('CURLOPT_QUOTE')) {
  define('CURLOPT_QUOTE', 10028);
}
if (!defined('CURLOPT_WRITEHEADER')) {
  define('CURLOPT_WRITEHEADER', 10029);
}
if (!defined('CURLOPT_COOKIEFILE')) {
  define('CURLOPT_COOKIEFILE', 10031);
}
if (!defined('CURLOPT_CUSTOMREQUEST')) {
  define('CURLOPT_CUSTOMREQUEST', 10036);
}
if (!defined('CURLOPT_STDERR')) {
  define('CURLOPT_STDERR', 10037);
}
if (!defined('CURLOPT_POSTQUOTE')) {
  define('CURLOPT_POSTQUOTE', 10039);
}
if (!defined('CURLOPT_INTERFACE')) {
  define('CURLOPT_INTERFACE', 10062);
}
if (!defined('CURLOPT_KRB4LEVEL')) {
  define('CURLOPT_KRB4LEVEL', 10063);
}
if (!defined('CURLOPT_KRBLEVEL')) {
  define('CURLOPT_KRBLEVEL', 10063);
}
if (!defined('CURLOPT_CAINFO')) {
  define('CURLOPT_CAINFO', 10065);
}
if (!defined('CURLOPT_TELNETOPTIONS')) {
  define('CURLOPT_TELNETOPTIONS', 10070);
}
if (!defined('CURLOPT_RANDOM_FILE')) {
  define('CURLOPT_RANDOM_FILE', 10076);
}
if (!defined('CURLOPT_EGDSOCKET')) {
  define('CURLOPT_EGDSOCKET', 10077);
}
if (!defined('CURLOPT_COOKIEJAR')) {
  define('CURLOPT_COOKIEJAR', 10082);
}
if (!defined('CURLOPT_SSL_CIPHER_LIST')) {
  define('CURLOPT_SSL_CIPHER_LIST', 10083);
}
if (!defined('CURLOPT_SSLCERTTYPE')) {
  define('CURLOPT_SSLCERTTYPE', 10086);
}
if (!defined('CURLOPT_SSLKEY')) {
  define('CURLOPT_SSLKEY', 10087);
}
if (!defined('CURLOPT_SSLKEYTYPE')) {
  define('CURLOPT_SSLKEYTYPE', 10088);
}
if (!defined('CURLOPT_SSLENGINE')) {
  define('CURLOPT_SSLENGINE', 10089);
}
if (!defined('CURLOPT_PREQUOTE')) {
  define('CURLOPT_PREQUOTE', 10093);
}
if (!defined('CURLOPT_CAPATH')) {
  define('CURLOPT_CAPATH', 10097);
}
if (!defined('CURLOPT_SHARE')) {
  define('CURLOPT_SHARE', 10100);
}
if (!defined('CURLOPT_ENCODING')) {
  define('CURLOPT_ENCODING', 10102);
}
if (!defined('CURLOPT_ACCEPT_ENCODING')) {
  define('CURLOPT_ACCEPT_ENCODING', 10102);
}
if (!defined('CURLOPT_PRIVATE')) {
  define('CURLOPT_PRIVATE', 10103);
}
if (!defined('CURLOPT_HTTP200ALIASES')) {
  define('CURLOPT_HTTP200ALIASES', 10104);
}
if (!defined('CURLOPT_NETRC_FILE')) {
  define('CURLOPT_NETRC_FILE', 10118);
}
if (!defined('CURLOPT_FTP_ACCOUNT')) {
  define('CURLOPT_FTP_ACCOUNT', 10134);
}
if (!defined('CURLOPT_COOKIELIST')) {
  define('CURLOPT_COOKIELIST', 10135);
}
if (!defined('CURLOPT_FTP_ALTERNATIVE_TO_USER')) {
  define('CURLOPT_FTP_ALTERNATIVE_TO_USER', 10147);
}
if (!defined('CURLOPT_SSH_PUBLIC_KEYFILE')) {
  define('CURLOPT_SSH_PUBLIC_KEYFILE', 10152);
}
if (!defined('CURLOPT_SSH_PRIVATE_KEYFILE')) {
  define('CURLOPT_SSH_PRIVATE_KEYFILE', 10153);
}
if (!defined('CURLOPT_SSH_HOST_PUBLIC_KEY_MD5')) {
  define('CURLOPT_SSH_HOST_PUBLIC_KEY_MD5', 10162);
}
if (!defined('CURLOPT_CRLFILE')) {
  define('CURLOPT_CRLFILE', 10169);
}
if (!defined('CURLOPT_ISSUERCERT')) {
  define('CURLOPT_ISSUERCERT', 10170);
}
if (!defined('CURLOPT_USERNAME')) {
  define('CURLOPT_USERNAME', 10173);
}
if (!defined('CURLOPT_PASSWORD')) {
  define('CURLOPT_PASSWORD', 10174);
}
if (!defined('CURLOPT_PROXYUSERNAME')) {
  define('CURLOPT_PROXYUSERNAME', 10175);
}
if (!defined('CURLOPT_PROXYPASSWORD')) {
  define('CURLOPT_PROXYPASSWORD', 10176);
}
if (!defined('CURLOPT_NOPROXY')) {
  define('CURLOPT_NOPROXY', 10177);
}
if (!defined('CURLOPT_SOCKS5_GSSAPI_SERVICE')) {
  define('CURLOPT_SOCKS5_GSSAPI_SERVICE', 10179);
}
if (!defined('CURLOPT_SSH_KNOWNHOSTS')) {
  define('CURLOPT_SSH_KNOWNHOSTS', 10183);
}
if (!defined('CURLOPT_MAIL_FROM')) {
  define('CURLOPT_MAIL_FROM', 10186);
}
if (!defined('CURLOPT_MAIL_RCPT')) {
  define('CURLOPT_MAIL_RCPT', 10187);
}
if (!defined('CURLOPT_RTSP_SESSION_ID')) {
  define('CURLOPT_RTSP_SESSION_ID', 10190);
}
if (!defined('CURLOPT_RTSP_STREAM_URI')) {
  define('CURLOPT_RTSP_STREAM_URI', 10191);
}
if (!defined('CURLOPT_RTSP_TRANSPORT')) {
  define('CURLOPT_RTSP_TRANSPORT', 10192);
}
if (!defined('CURLOPT_RESOLVE')) {
  define('CURLOPT_RESOLVE', 10203);
}
if (!defined('CURLOPT_TLSAUTH_USERNAME')) {
  define('CURLOPT_TLSAUTH_USERNAME', 10204);
}
if (!defined('CURLOPT_TLSAUTH_PASSWORD')) {
  define('CURLOPT_TLSAUTH_PASSWORD', 10205);
}
if (!defined('CURLOPT_TLSAUTH_TYPE')) {
  define('CURLOPT_TLSAUTH_TYPE', 10206);
}
if (!defined('CURLOPT_DNS_SERVERS')) {
  define('CURLOPT_DNS_SERVERS', 10211);
}
if (!defined('CURLOPT_MAIL_AUTH')) {
  define('CURLOPT_MAIL_AUTH', 10217);
}
if (!defined('CURLPROTO_POP3')) {
  define('CURLPROTO_POP3', 16384);
}
if (!defined('CURLOPT_RETURNTRANSFER')) {
  define('CURLOPT_RETURNTRANSFER', 19913);
}
if (!defined('CURLOPT_BINARYTRANSFER')) {
  define('CURLOPT_BINARYTRANSFER', 19914);
}
if (!defined('CURLOPT_WRITEFUNCTION')) {
  define('CURLOPT_WRITEFUNCTION', 20011);
}
if (!defined('CURLOPT_READFUNCTION')) {
  define('CURLOPT_READFUNCTION', 20012);
}
if (!defined('CURLOPT_PROGRESSFUNCTION')) {
  define('CURLOPT_PROGRESSFUNCTION', 20056);
}
if (!defined('CURLOPT_HEADERFUNCTION')) {
  define('CURLOPT_HEADERFUNCTION', 20079);
}
if (!defined('CURLOPT_FNMATCH_FUNCTION')) {
  define('CURLOPT_FNMATCH_FUNCTION', 20200);
}
if (!defined('CURLOPT_MAX_SEND_SPEED_LARGE')) {
  define('CURLOPT_MAX_SEND_SPEED_LARGE', 30145);
}
if (!defined('CURLOPT_MAX_RECV_SPEED_LARGE')) {
  define('CURLOPT_MAX_RECV_SPEED_LARGE', 30146);
}
if (!defined('CURLPROTO_POP3S')) {
  define('CURLPROTO_POP3S', 32768);
}
if (!defined('CURLPROTO_SMTP')) {
  define('CURLPROTO_SMTP', 65536);
}
if (!defined('CURLPROTO_SMTPS')) {
  define('CURLPROTO_SMTPS', 131072);
}
if (!defined('CURLPROTO_RTSP')) {
  define('CURLPROTO_RTSP', 262144);
}
if (!defined('CURLPROTO_RTMP')) {
  define('CURLPROTO_RTMP', 524288);
}
if (!defined('CURLPROTO_RTMPT')) {
  define('CURLPROTO_RTMPT', 1048576);
}
if (!defined('CURLINFO_EFFECTIVE_URL')) {
  define('CURLINFO_EFFECTIVE_URL', 1048577);
}
if (!defined('CURLINFO_CONTENT_TYPE')) {
  define('CURLINFO_CONTENT_TYPE', 1048594);
}
if (!defined('CURLINFO_PRIVATE')) {
  define('CURLINFO_PRIVATE', 1048597);
}
if (!defined('CURLINFO_FTP_ENTRY_PATH')) {
  define('CURLINFO_FTP_ENTRY_PATH', 1048606);
}
if (!defined('CURLINFO_REDIRECT_URL')) {
  define('CURLINFO_REDIRECT_URL', 1048607);
}
if (!defined('CURLINFO_PRIMARY_IP')) {
  define('CURLINFO_PRIMARY_IP', 1048608);
}
if (!defined('CURLINFO_RTSP_SESSION_ID')) {
  define('CURLINFO_RTSP_SESSION_ID', 1048612);
}
if (!defined('CURLINFO_LOCAL_IP')) {
  define('CURLINFO_LOCAL_IP', 1048617);
}
if (!defined('CURLPROTO_RTMPE')) {
  define('CURLPROTO_RTMPE', 2097152);
}
if (!defined('CURLINFO_RESPONSE_CODE')) {
  define('CURLINFO_RESPONSE_CODE', 2097154);
}
if (!defined('CURLINFO_HTTP_CODE')) {
  define('CURLINFO_HTTP_CODE', 2097154);
}
if (!defined('CURLINFO_HEADER_SIZE')) {
  define('CURLINFO_HEADER_SIZE', 2097163);
}
if (!defined('CURLINFO_REQUEST_SIZE')) {
  define('CURLINFO_REQUEST_SIZE', 2097164);
}
if (!defined('CURLINFO_SSL_VERIFYRESULT')) {
  define('CURLINFO_SSL_VERIFYRESULT', 2097165);
}
if (!defined('CURLINFO_FILETIME')) {
  define('CURLINFO_FILETIME', 2097166);
}
if (!defined('CURLINFO_REDIRECT_COUNT')) {
  define('CURLINFO_REDIRECT_COUNT', 2097172);
}
if (!defined('CURLINFO_HTTP_CONNECTCODE')) {
  define('CURLINFO_HTTP_CONNECTCODE', 2097174);
}
if (!defined('CURLINFO_HTTPAUTH_AVAIL')) {
  define('CURLINFO_HTTPAUTH_AVAIL', 2097175);
}
if (!defined('CURLINFO_PROXYAUTH_AVAIL')) {
  define('CURLINFO_PROXYAUTH_AVAIL', 2097176);
}
if (!defined('CURLINFO_OS_ERRNO')) {
  define('CURLINFO_OS_ERRNO', 2097177);
}
if (!defined('CURLINFO_NUM_CONNECTS')) {
  define('CURLINFO_NUM_CONNECTS', 2097178);
}
if (!defined('CURLINFO_CONDITION_UNMET')) {
  define('CURLINFO_CONDITION_UNMET', 2097187);
}
if (!defined('CURLINFO_RTSP_CLIENT_CSEQ')) {
  define('CURLINFO_RTSP_CLIENT_CSEQ', 2097189);
}
if (!defined('CURLINFO_RTSP_SERVER_CSEQ')) {
  define('CURLINFO_RTSP_SERVER_CSEQ', 2097190);
}
if (!defined('CURLINFO_RTSP_CSEQ_RECV')) {
  define('CURLINFO_RTSP_CSEQ_RECV', 2097191);
}
if (!defined('CURLINFO_PRIMARY_PORT')) {
  define('CURLINFO_PRIMARY_PORT', 2097192);
}
if (!defined('CURLINFO_LOCAL_PORT')) {
  define('CURLINFO_LOCAL_PORT', 2097194);
}
if (!defined('CURLINFO_TOTAL_TIME')) {
  define('CURLINFO_TOTAL_TIME', 3145731);
}
if (!defined('CURLINFO_NAMELOOKUP_TIME')) {
  define('CURLINFO_NAMELOOKUP_TIME', 3145732);
}
if (!defined('CURLINFO_CONNECT_TIME')) {
  define('CURLINFO_CONNECT_TIME', 3145733);
}
if (!defined('CURLINFO_PRETRANSFER_TIME')) {
  define('CURLINFO_PRETRANSFER_TIME', 3145734);
}
if (!defined('CURLINFO_SIZE_UPLOAD')) {
  define('CURLINFO_SIZE_UPLOAD', 3145735);
}
if (!defined('CURLINFO_SIZE_DOWNLOAD')) {
  define('CURLINFO_SIZE_DOWNLOAD', 3145736);
}
if (!defined('CURLINFO_SPEED_DOWNLOAD')) {
  define('CURLINFO_SPEED_DOWNLOAD', 3145737);
}
if (!defined('CURLINFO_SPEED_UPLOAD')) {
  define('CURLINFO_SPEED_UPLOAD', 3145738);
}
if (!defined('CURLINFO_CONTENT_LENGTH_DOWNLOAD')) {
  define('CURLINFO_CONTENT_LENGTH_DOWNLOAD', 3145743);
}
if (!defined('CURLINFO_CONTENT_LENGTH_UPLOAD')) {
  define('CURLINFO_CONTENT_LENGTH_UPLOAD', 3145744);
}
if (!defined('CURLINFO_STARTTRANSFER_TIME')) {
  define('CURLINFO_STARTTRANSFER_TIME', 3145745);
}
if (!defined('CURLINFO_REDIRECT_TIME')) {
  define('CURLINFO_REDIRECT_TIME', 3145747);
}
if (!defined('CURLINFO_APPCONNECT_TIME')) {
  define('CURLINFO_APPCONNECT_TIME', 3145761);
}
if (!defined('CURLPROTO_RTMPTE')) {
  define('CURLPROTO_RTMPTE', 4194304);
}
if (!defined('CURLINFO_SSL_ENGINES')) {
  define('CURLINFO_SSL_ENGINES', 4194331);
}
if (!defined('CURLINFO_COOKIELIST')) {
  define('CURLINFO_COOKIELIST', 4194332);
}
if (!defined('CURLINFO_CERTINFO')) {
  define('CURLINFO_CERTINFO', 4194338);
}
if (!defined('CURLPROTO_RTMPS')) {
  define('CURLPROTO_RTMPS', 8388608);
}
if (!defined('CURLPROTO_RTMPTS')) {
  define('CURLPROTO_RTMPTS', 16777216);
}
if (!defined('CURLPROTO_GOPHER')) {
  define('CURLPROTO_GOPHER', 33554432);
}
if (!defined('CURL_READFUNC_PAUSE')) {
  define('CURL_READFUNC_PAUSE', 268435457);
}
if (!defined('CURL_WRITEFUNC_PAUSE')) {
  define('CURL_WRITEFUNC_PAUSE', 268435457);
}
if (!defined('CURLAUTH_ONLY')) {
  define('CURLAUTH_ONLY', 2147483648);
}

// Function Definitions

use google\appengine\runtime\CurlLite;
use google\appengine\runtime\CurlLiteMethodNotSupportedException;

if (!function_exists('curl_init')) {
  function curl_init($url = null) {
    return new CurlLite($url);
  }
}

if (!function_exists('curl_copy_handle')) {
  function curl_copy_handle($ch) {
    return clone $ch;
  }
}

if (!function_exists('curl_version')) {
  function curl_version($version = CURLVERSION_NOW) {
    return CurlLite::version($version);
  }
}

if (!function_exists('curl_setopt')) {
  function curl_setopt($ch, $option, $value) {
    return curl_setopt_array($ch, [$option => $value]);
  }
}

if (!function_exists('curl_setopt_array')) {
  function curl_setopt_array($ch, $options) {
    return $ch->setOptionsArray($options);
  }
}

if (!function_exists('curl_exec')) {
  function curl_exec($ch) {
    return $ch->exec();
  }
}

if (!function_exists('curl_getinfo')) {
  function curl_getinfo($ch, $option = 0) {
    return $ch->getInfo($option);
  }
}

if (!function_exists('curl_error')) {
  function curl_error($ch) {
    return $ch->errorString();
  }
}

if (!function_exists('curl_errno')) {
  function curl_errno($ch) {
    return $ch->errorNumber();
  }
}

if (!function_exists('curl_close')) {
  function curl_close($ch) {
    unset($ch);
  }
}
if (!function_exists('curl_strerror')) {
  function curl_strerror($errornum) {
    return CurlLite::strerror($errornum);
  }
}
if (!function_exists('curl_multi_strerror')) {
  function curl_multi_strerror($errornum) {
    throw new CurlLiteMethodNotSupportedException('curl_multi_strerror');
  }
}
if (!function_exists('curl_reset')) {
  function curl_reset(&$ch) {
    $ch = new CurlLite();
  }
}
if (!function_exists('curl_escape')) {
  function curl_escape($ch, $str) {
    return $ch->escape($str);
  }
}
if (!function_exists('curl_unescape')) {
  function curl_unescape($ch, $str) {
    return $ch->unescape($str);
  }
}
if (!function_exists('curl_pause')) {
  function curl_pause($ch, $bitmask) {
    throw new CurlLiteMethodNotSupportedException('curl_pause');
  }
}

if (!function_exists('curl_multi_init')) {
  function curl_multi_init() {
    throw new CurlLiteMethodNotSupportedException('curl_multi_init');
  }
}

if (!function_exists('curl_multi_add_handle')) {
  function curl_multi_add_handle($mh, $ch) {
    throw new CurlLiteMethodNotSupportedException('curl_multi_add_handle');
  }
}

if (!function_exists('curl_multi_remove_handle')) {
  function curl_multi_remove_handle($mh, $ch) {
    throw new CurlLiteMethodNotSupportedException('curl_multi_remove_handle');
  }
}

if (!function_exists('curl_multi_select')) {
  function curl_multi_select($mh, $timeout = 1.0) {
    throw new CurlLiteMethodNotSupportedException('curl_multi_select');
  }
}

if (!function_exists('curl_multi_exec')) {
  function curl_multi_exec($mh, &$still_running) {
    throw new CurlLiteMethodNotSupportedException('curl_multi_exec');
  }
}

if (!function_exists('curl_multi_getcontent')) {
  function curl_multi_getcontent($ch) {
    throw new CurlLiteMethodNotSupportedException('curl_multi_getcontent');
  }
}

if (!function_exists('curl_multi_info_read')) {
  function curl_multi_info_read($mh, &$msgs_in_queue = null) {
    throw new CurlLiteMethodNotSupportedException('curl_multi_info_read');
  }
}

if (!function_exists('curl_multi_close')) {
  function curl_multi_close($mh) {
    throw new CurlLiteMethodNotSupportedException('curl_multi_close');
  }
}

if (!function_exists('curl_multi_setopt')) {
  function curl_multi_setopt($sh, $option, $value) {
    throw new CurlLiteMethodNotSupportedException('curl_multi_setopt');
  }
}

if (!function_exists('curl_share_init')) {
  function curl_share_init() {
    throw new CurlLiteMethodNotSupportedException('curl_share_init');
  }
}

if (!function_exists('curl_share_close')) {
  function curl_share_close($sh) {
    throw new CurlLiteMethodNotSupportedException('curl_share_close');
  }
}

if (!function_exists('curl_share_setopt')) {
  function curl_share_setopt($sh, $option, $value) {
    throw new CurlLiteMethodNotSupportedException('curl_share_setopt');
  }
}

if (!function_exists('curl_file_create')) {
  function curl_file_create($filename, $mimetype = '', $postname = '') {
    throw new CurlLiteMethodNotSupportedException('curl_file_create');
  }
}

