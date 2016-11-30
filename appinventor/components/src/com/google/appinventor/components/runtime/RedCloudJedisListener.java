// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//Notification Listener from: http://stackoverflow.com/questions/26406303/redis-key-expire-notification-with-jedis

package com.google.appinventor.components.runtime;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONException;

import android.util.Log;

public class RedCloudJedisListener extends JedisPubSub {
  public RedCloud redCloud;
  public RedCloudJedisListener(RedCloud thisRedCloud){
    redCloud = thisRedCloud;
  }
  
  @Override
  public void onPSubscribe(String pattern, int subscribedChannels) {
    Log.i("RedCloud", "onPSubscribe "+pattern+" "+subscribedChannels);
  }
  
  @Override
  public void onPMessage(String pattern, String channel, String message) {
    Log.i("RedCloud","onPMessage pattern "+pattern+", channel: "+channel+", message: "+message);
    if(channel.substring(channel.length() - 3).equals("set")){
      Log.i("RedCloud", "tag "+message+" is newly set");
      Jedis jedis = getJedis();
      redCloud.DataChanged(message, jedis.get(message));
    }
  }
  
  private Jedis getJedis(){
    Jedis jedis = new Jedis("128.52.179.76", 6379);
    jedis.auth("test6789");
    return jedis;
  }
  //add other Unimplemented methods
}