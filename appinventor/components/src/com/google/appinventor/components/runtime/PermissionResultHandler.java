// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * PermissionResultHandler -- This handler is called from
 *                            Form.askPermission after the end-user
 *                            has either granted or denied a
 *                            permission request.
 */

public interface PermissionResultHandler {

    /**
     * HandlePermissionResponse -- Take action based on the response
     *                             to Form.askPermission
     *
     * @param permission - The requested permission (as a string)
     * @param granted    - boolean, true if permission granted, false otherwise
     */

    public void HandlePermissionResponse(String permission, boolean granted);
}
