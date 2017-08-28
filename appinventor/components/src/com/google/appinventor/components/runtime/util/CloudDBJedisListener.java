// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//Notification Listener from: http://stackoverflow.com/questions/26406303/redis-key-expire-notification-with-jedis

package com.google.appinventor.components.runtime.util;

import android.util.Log;
import com.google.appinventor.components.runtime.CloudDB;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class CloudDBJedisListener extends JedisPubSub {
  public CloudDB cloudDB;
  public CloudDBJedisListener(CloudDB thisCloudDB){
    cloudDB = thisCloudDB;
  }

  @Override
  public void onPSubscribe(String pattern, int subscribedChannels) {
    Log.i("CloudDB", "onPSubscribe "+pattern+" "+subscribedChannels);
  }

  @Override
  public void onPMessage(String pattern, String channel, String message) {
    Log.i("CloudDB","onPMessage pattern "+pattern+", channel: "+channel+", message: "+message);
    if (channel.substring(channel.length() - 3).equals("set")) {
      Log.i("CloudDB", "tag "+message+" is newly set");
      Jedis jedis = cloudDB.getJedis();
      cloudDB.DataChanged(message, jedis.get(message));
    } else if(channel.substring(channel.length() - 4).equals("zadd")){
      Log.i("CloudDB", "tag "+message+" is newly zadd");
      // Jedis jedis = cloudDB.getJedis();
      // cloudDB.DataChanged(message, jedis.get(message));
    }

  }

  //add other Unimplemented methods
}
