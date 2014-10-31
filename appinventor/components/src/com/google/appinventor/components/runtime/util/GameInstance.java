// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for information about a GameInstance for use
 * with the App Inventor game framework.
 *
 *
 */
public class GameInstance {
  private String instanceId;
  private String leader;

  // players in the current game
  private List<String> players;

  // Use this to store the most recent time stamp of each message type received.
  private Map<String, String> messageTimes;


  /**
   * A GameInstance contains the most recent values
   * for the leader and players of a particular game instance.
   *
   * This object is also used to keep track of the most recent
   * time that a particular message type was retrieved from the
   * server.
   *
   * @param instanceId The unique String that identifies this
   * instance.
   */
  public GameInstance(String instanceId) {
    players = new ArrayList<String>(0);
    messageTimes = new HashMap<String, String>();
    this.instanceId = instanceId;
    this.leader = "";
  }

  /**
   * Return the instance id of this instance.
   * @return the instance id.
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Return the current leader of this instance.
   * @return The email address of the current leader.
   */
  public String getLeader() {
    return leader;
  }

  /**
   * Sets the leader of this instance.
   * @param leader The email address of the new leader.
   */
  public void setLeader(String leader) {
    this.leader = leader;
  }

  /**
   * Sets the players of this instances to currentPlayersList.
   *
   * Compares the current players list with the new one and returns
   * a delta to the caller.
   *
   * @param newPlayersList All players currently in the instance.
   * @return PlayersListDelta.NO_CHANGE if there is no change in
   * membership. Otherwise returns a PlayersListDelta with the
   * appropriate player lists.
   */
  public PlayerListDelta setPlayers(List<String> newPlayersList) {
    if (newPlayersList.equals(players)) {
      return PlayerListDelta.NO_CHANGE;
    }
    List<String> removed = players;
    List<String> added = new ArrayList<String>(newPlayersList);
    players = new ArrayList<String>(newPlayersList);

    added.removeAll(removed);
    removed.removeAll(newPlayersList);
    // This happens if the players list is the same but the ordering
    // has changed for some reason.
    if (added.size() == 0 && removed.size() == 0) {
      return PlayerListDelta.NO_CHANGE;
    }

    return new PlayerListDelta(removed, added);
  }

  /**
   * Return the list of players currently in this instance.
   *
   * @return A list of the players in the instance.
   */
  public List<String> getPlayers() {
    return players;
  }

  /**
   * Return the most recently put time string for this type.
   *
   * This should represent the creation time of the most
   * recently received message of the specified type and can
   * be used to filter available messages to find those that
   * have not been received.
   *
   * @param type The message type.
   * @return The most recently put value for this type.
   */
  public String getMessageTime(String type) {
    if (messageTimes.containsKey(type)) {
      return messageTimes.get(type);
    }
    return "";
  }

  /**
   * Puts a new time string for the specified message type.
   *
   * The string should be some value that can be understood
   * by its eventual consumer. It is left as a string here
   * to remove the need to convert back and forth from DateTime
   * objects when dealing with web services.
   *
   * @param type The message type.
   * @param time A string representing the time the message
   * was created.
   */
  public void putMessageTime(String type, String time) {
    messageTimes.put(type, time);
  }
}
