// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//Notification Listener from: http://stackoverflow.com/questions/26406303/redis-key-expire-notification-with-jedis

package edu.mit.appinventor;

import android.util.Log;
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
    if(channel.substring(channel.length() - 3).equals("set")){
      Log.i("CloudDB", "tag "+message+" is newly set");
      Jedis jedis = getJedis();
      cloudDB.DataChanged(message, jedis.get(message));
    }
  }
  
  private Jedis getJedis(){
    Jedis jedis = new Jedis("jis.csail.mit.edu", 9001);
    jedis.auth("test6789");
    return jedis;
  }
  //add other Unimplemented methods
}
