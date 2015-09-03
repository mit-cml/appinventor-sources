// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for text-to-speech countries.
 *
 */
public class CountryChoicePropertyEditor extends ChoicePropertyEditor {

  // countries supported by AppInventor's Android 2.2 emulator
  private static final Choice[] countries = new Choice[] {
    new Choice(MESSAGES.defaultText(), ""),
    new Choice("AUS", "AUS"),
    new Choice("AUT", "AUT"),
    new Choice("BEL", "BEL"),
    new Choice("BLZ", "BLZ"),
    new Choice("BWA", "BWA"),
    new Choice("CAN", "CAN"),
    new Choice("CHE", "CHE"),
    new Choice("DEU", "DEU"),
    new Choice("ESP", "ESP"),
    new Choice("FRA", "FRA"),
    new Choice("GBR", "GBR"),
    new Choice("HKG", "HKG"),
    new Choice("IND", "IND"),
    new Choice("IRL", "IRL"),
    new Choice("ITA", "ITA"),
    new Choice("JAM", "JAM"),
    new Choice("LIE", "LIE"),
    new Choice("LUX", "LUX"),
    new Choice("MCO", "MCO"),
    new Choice("MHL", "MHL"),
    new Choice("MLT", "MLT"),
    new Choice("NAM", "NAM"),
    new Choice("NZL", "NZL"),
    new Choice("PAK", "PAK"),
    new Choice("PHL", "PHL"),
    new Choice("SGP", "SGP"),
    new Choice("TTO", "TTO"),
    new Choice("USA", "USA"),
    new Choice("VIR", "VIR"),
    new Choice("ZAF", "ZAF"),
    new Choice("ZWE", "ZWE")
  };

  public CountryChoicePropertyEditor() {
    super(countries);
  }
}
