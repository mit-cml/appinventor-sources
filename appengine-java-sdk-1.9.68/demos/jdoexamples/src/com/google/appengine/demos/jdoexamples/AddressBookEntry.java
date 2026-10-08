/* Copyright (c) 2009 Google Inc.
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

package com.google.appengine.demos.jdoexamples;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION)
public class AddressBookEntry {

  private static final int ENTITIES_PER_PAGE = 3;

  @PersistenceCapable
  @EmbeddedOnly
  public static class PersonalInfo {
    public String lastName;
    public String firstName;
    
    public PersonalInfo(String firstName, String lastName) {
      this.lastName = lastName;
      this.firstName = firstName;
    }
  }
  
  @PersistenceCapable
  @EmbeddedOnly
  public static class AddressInfo {
    public String city;
    public String state;
    
    public AddressInfo(String city, String state) {
      this.city = city;
      this.state = state;
    }
  }

  @PersistenceCapable
  @EmbeddedOnly
  public static class ContactInfo {
    public String phoneNumber;
    
    public ContactInfo(String phoneNumber) {
      this.phoneNumber = phoneNumber;
    }
  }
  
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  @Embedded
  private PersonalInfo personalInfo;
  
  @Persistent
  @Embedded
  private AddressInfo addressInfo;
  
  @Persistent
  @Embedded
  private ContactInfo contactInfo;
  
  public PersonalInfo getPersonalInfo() {
    return personalInfo;
  }

  public void setPersonalInfo(PersonalInfo personalInfo) {
    this.personalInfo = personalInfo;
  }

  public ContactInfo getContactInfo() {
    return contactInfo;
  }

  public void setContactInfo(ContactInfo contactInfo) {
    this.contactInfo = contactInfo;
  }

  public void setAddressInfo(AddressInfo addressInfo) {
    this.addressInfo = addressInfo;
  }

  public AddressInfo getAddressInfo() {
    return addressInfo;
  }
  
  public Long getId() {
    return id;
  }
}
