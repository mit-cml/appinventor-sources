// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.user.User;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class YoungAndroidProjectServiceTest extends TestCase {

  private static final String USER_ID = "user";
  private static final long PROJECT_ID = 5L;
	
  /* Testing the case of a null build URL */
  @Test
  public void testBuildErrorMsgDoesntThrowNPE() {
    YoungAndroidProjectService obj = new YoungAndroidProjectService(null);
    obj.buildErrorMsg("TestException", null, "userID", 0); 
  }

  /* The nonce must stay short so people can type the download link by hand */
  @Test
  public void testGenerateNonceIsShortBase36() {
    for (int i = 0; i < 1000; i++) {
      String nonce = YoungAndroidProjectService.generateNonce();
      assertTrue(nonce, nonce.matches("[0-9a-z]{1,5}"));
      assertTrue(nonce, Integer.parseInt(nonce, 36) < 10000000);
    }
  }

  @Test
  public void testStoreNewNonceReturnsStoredNonce() {
    StorageIo storageIo = mock(StorageIo.class);
    when(storageIo.storeNonce(anyString(), eq(USER_ID), eq(PROJECT_ID))).thenReturn(true);
    YoungAndroidProjectService service = new YoungAndroidProjectService(storageIo);

    String nonce = service.storeNewNonce(USER_ID, PROJECT_ID);

    assertNotNull(nonce);
    verify(storageIo, times(1)).storeNonce(nonce, USER_ID, PROJECT_ID);
  }

  /* A nonce that is already in use must not be reused, a new one is picked instead */
  @Test
  public void testStoreNewNonceRetriesWhenNonceIsTaken() {
    StorageIo storageIo = mock(StorageIo.class);
    when(storageIo.storeNonce(anyString(), eq(USER_ID), eq(PROJECT_ID)))
        .thenReturn(false, false, true);
    YoungAndroidProjectService service = new YoungAndroidProjectService(storageIo);

    String nonce = service.storeNewNonce(USER_ID, PROJECT_ID);

    ArgumentCaptor<String> tried = ArgumentCaptor.forClass(String.class);
    verify(storageIo, times(3)).storeNonce(tried.capture(), eq(USER_ID), eq(PROJECT_ID));
    assertEquals(tried.getAllValues().get(2), nonce);
  }

  @Test
  public void testStoreNewNonceGivesUpAfterMaxAttempts() {
    StorageIo storageIo = mock(StorageIo.class);
    when(storageIo.storeNonce(anyString(), eq(USER_ID), eq(PROJECT_ID))).thenReturn(false);
    YoungAndroidProjectService service = new YoungAndroidProjectService(storageIo);

    assertNull(service.storeNewNonce(USER_ID, PROJECT_ID));
    verify(storageIo, times(YoungAndroidProjectService.MAX_NONCE_ATTEMPTS))
        .storeNonce(anyString(), eq(USER_ID), eq(PROJECT_ID));
  }

  /* If no nonce can be stored the build fails before anything is sent to the build server */
  @Test
  public void testBuildFailsWhenNoFreeNonce() {
    StorageIo storageIo = mock(StorageIo.class);
    when(storageIo.getProjectName(USER_ID, PROJECT_ID)).thenReturn("Project");
    when(storageIo.storeNonce(anyString(), eq(USER_ID), eq(PROJECT_ID))).thenReturn(false);
    User user = new User(USER_ID, "user@example.com", true, false, null);
    YoungAndroidProjectService service = new YoungAndroidProjectService(storageIo);

    RpcResult result = service.build(user, PROJECT_ID, "Android", false, false, false, false);

    assertTrue(result.failed());
    verify(storageIo, never()).getProjectOutputFiles(USER_ID, PROJECT_ID);
  }
}
