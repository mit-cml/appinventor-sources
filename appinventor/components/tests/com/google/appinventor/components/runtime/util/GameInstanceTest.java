// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests GameInstance class.
 *
 *
 */
public class GameInstanceTest extends TestCase {
  public void testSetPlayersForNewPlayers() {
    GameInstance instance = new GameInstance("test_iid");
    assertEquals(instance.getPlayers().size(), 0);

    PlayerListDelta returnList;
    List<String> players = new ArrayList<String>();
    players.add("p1@p.com");
    players.add("p2@p.com");

    returnList = instance.setPlayers(players);
    assertEquals(returnList.getPlayersRemoved(), new ArrayList<String>());
    assertEquals(returnList.getPlayersAdded(), instance.getPlayers());
    assertEquals(returnList.getPlayersAdded(), players);

    players.add("p3@p.com");
    players.add("p4@p.com");

    List<String> expectedReturnList = new ArrayList<String>();
    expectedReturnList.add("p3@p.com");
    expectedReturnList.add("p4@p.com");
    assertEquals(instance.getPlayers().size(), 2);
    returnList = instance.setPlayers(players);
    assertEquals(returnList.getPlayersRemoved(), new ArrayList<String>());
    assertEquals(returnList.getPlayersAdded(), expectedReturnList);

    assertEquals(instance.getPlayers(), players);
  }

  public void testSetPlayersForNoNewPlayers() {
    GameInstance instance = new GameInstance("test_iid");
    PlayerListDelta returnList;
    List<String> players = new ArrayList<String>();
    players.add("p1@p.com");
    players.add("p2@p.com");

    returnList = instance.setPlayers(players);
    returnList = instance.setPlayers(players);
    assertEquals(returnList, PlayerListDelta.NO_CHANGE);
  }

  public void testSetPlayersForLeavingPlayers() {
    GameInstance instance = new GameInstance("test_iid");
    PlayerListDelta returnList;
    List<String> players = new ArrayList<String>();
    players.add("p1@p.com");
    players.add("p2@p.com");

    returnList = instance.setPlayers(players);

    players.remove("p1@p.com");
    returnList = instance.setPlayers(players);
    List<String> expectedRemoveList = new ArrayList<String>();
    expectedRemoveList.add("p1@p.com");
    assertEquals(returnList.getPlayersRemoved(), expectedRemoveList);
    assertEquals(returnList.getPlayersAdded(), new ArrayList<String>());
  }

  public void testSetPlayersLeaveAndAdd() {
    GameInstance instance = new GameInstance("test_iid");
    PlayerListDelta returnList;
    List<String> players = new ArrayList<String>();
    players.add("p1@p.com");
    players.add("p2@p.com");

    returnList = instance.setPlayers(players);

    players.add("p3@p.com");
    players.remove("p1@p.com");
    returnList = instance.setPlayers(players);
    List<String> expectedAddList = new ArrayList<String>();
    List<String> expectedRemoveList = new ArrayList<String>();
    expectedAddList.add("p3@p.com");
    expectedRemoveList.add("p1@p.com");
    assertEquals(returnList.getPlayersRemoved(), expectedRemoveList);
    assertEquals(returnList.getPlayersAdded(), expectedAddList);
  }

  public void testMessageTimesUpdate() {
    GameInstance instance = new GameInstance("test_iid");

    assertEquals(instance.getMessageTime("test1"), "");
    instance.putMessageTime("test1", "yesterday");
    assertEquals(instance.getMessageTime("test1"), "yesterday");
    instance.putMessageTime("test1", "tomorrow");
    assertEquals(instance.getMessageTime("test1"), "tomorrow");
  }
}
