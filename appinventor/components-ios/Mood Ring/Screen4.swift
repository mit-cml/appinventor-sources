//
//  Screen4.swift
//  Mood Ring
//
//  Created by Evan Patton on 12/14/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import AIComponentKit

class Screen4: Form {
  var HorizontalArrangement_header: HorizontalArrangement!
  var VerticalArrangement10: VerticalArrangement!
  var ListPicker1: ListPicker!
  var VerticalArrangement12: VerticalArrangement!
  var moodring_label: Label!
  var VerticalArrangement11: VerticalArrangement!
  var Button_settings: Button!
  var VerticalArrangement9: VerticalArrangement!
  var ListPicker_Resources: ListPicker!
  var VerticalArrangement1: VerticalArrangement!
  var VerticalArrangement2: VerticalArrangement!
  var Label_date: Label!
  var HorizontalArrangement1: HorizontalArrangement!
  var TextBox_journalentry: TextBox!
  var HorizontalArrangement6: HorizontalArrangement!
  var HorizontalArrangement7: HorizontalArrangement!
  var VerticalArrangement3: VerticalArrangement!
  var Button_good: Button!
  var Label11: Label!
  var Button_happy: Button!
  var Label14: Label!
  var Button_excited: Button!
  var Label17: Label!
  var VerticalArrangement6: VerticalArrangement!
  var VerticalArrangement4: VerticalArrangement!
  var Button_sad: Button!
  var Label12: Label!
  var Button_irritated: Button!
  var Label15: Label!
  var Button1: Button!
  var Button_mad: Button!
  var Label18: Label!
  var TableArrangement1: TableArrangement!
  var Label20: Label!
  var Label21: Label!
  var Label22: Label!
  var Label24: Label!
  var Label25: Label!
  var VerticalArrangement7: VerticalArrangement!
  var VerticalArrangement5: VerticalArrangement!
  var Button_confused: Button!
  var Label13: Label!
  var Button_worried: Button!
  var Label16: Label!
  var Button_lost: Button!
  var Label19: Label!
  var HorizontalArrangement9: HorizontalArrangement!
  var Button2_enter: Button!
  var Label23: Label!
  var Clock1: Clock!
  var Sound1: Sound!
  var TinyDB1: TinyDB!
  var Notifier1: Notifier!
  var Texting1: Texting!
  var PhoneCall1: PhoneCall!
  var ActivityStarter1: ActivityStarter!

  var g$imageList2 = ["goodS.png", "sadS.png", "confusedS.png", "happyS.png", "irritatedS.png",
                      "worriedS.png", "excitedS.png", "madS.png", "gogingCrazyS.png"]
  var g$lNum = 0
  var g$irritatedList: [Any] = []
  var g$hNum = 0
  var g$cNum = 0
  var g$Purple = rgbToInt32(r: 24, g: 20, b: 38)
  var g$emoji: [Button] = []
  var g$HRpurple = rgbToInt32(r: 62, g: 34, b: 108)
  var g$wNum = 0
  var g$sNum = 0
  var g$orange = rgbToInt32(r: 237, g: 131, b: 87)
  var g$happyList: [Any] = []
  var g$screensList = ["Screen3", "Screen4", "Screen5", "Screen6"]
  var g$sadList: [Any] = []
  var g$feeling = 0
  var g$lostList: [Any] = []
  var g$confusedList: [Any] = []
  var g$HRpurple2 = rgbToInt32(r: 60, g: 53, b: 74)
  var g$madList: [Any] = []
  var g$iNum = 0
  var g$worriedList: [Any] = []
  var g$screensIndex = 0
  var g$goodList: [Any] = []
  var g$teal = rgbToInt32(r: 154, g: 184, b: 197)
  var g$gNum = 0
  var g$mNum = 0
  var g$teal2 = rgbToInt32(r: 153, g: 184, b: 197)
  var g$emotionList = ["good", "sad", "confused", "happy", "irritated", "worried", "excited", "mad", "lost"]
  var g$feelingsListList = ["goodlist", "sadlist", "confusedlist", "happylist", "irritatedlist", "excitedlist", "lostlist", "madlist", "worriedlist"]
  var g$imageList = ["good.png", "sad.png", "confused.png", "happy.png", "irritated.png", "worried.png", "excited.png", "mad.png", "goingCrazy.png"]
  var g$eNum = 0
  var g$excitedList: [Any] = []

  func p$all_return() {
    for button in g$emoji {
      button.Image = g$imageList[g$emoji.index(of: button)!]
    }
  }

  func p$greyButtons() {
    for button in g$emoji {
      button.Image = ""
      button.BackgroundColor = Int32(bitPattern: 0xFF333333)
      button.Height = 50
      button.Width = 50
    }
  }

  func p$allButtons() {
    g$emoji.append(contentsOf: [Button_good, Button_sad, Button_confused, Button_happy, Button_irritated, Button_worried, Button_excited, Button_mad, Button_lost])
  }

  func p$select_unselect(_ button: Button, _ index: Int32) {
    if button.Image == g$imageList[Int(index)] {
      button.Image = g$imageList2[Int(index)]
    } else {
      button.Image = g$imageList[Int(index)]
    }
  }

  func p$deleteAllList() {
    for item in g$feelingsListList {
      TinyDB1.ClearTag(item)
    }
    openAnotherScreen(named: "Screen3")
  }

  init?(_ coder: NSCoder? = nil) {
    if let coder = coder {
      super.init(coder: coder)
    } else {
      super.init(nibName: nil, bundle: nil)
    }
  }

  required convenience init?(coder aCoder: NSCoder) {
    self.init(coder: aCoder)
  }

  override func viewDidLoad() {
    super.viewDidLoad()

    // do-after-form-creation
    AppName = "moodring1"
    ScreenOrientation = "portrait"
    Scrollable = true
    ShowStatusBar = false
    Sizing = "Responsive"
    TitleVisible = false

    // HorizontalArragement_header
    HorizontalArrangement_header = HorizontalArrangement(self)
    HorizontalArrangement_header.AlignVertical = 2
    HorizontalArrangement_header.Height = -1010
    HorizontalArrangement_header.Width = -2

    // VerticalArrangement10
    VerticalArrangement10 = VerticalArrangement(HorizontalArrangement_header)
    VerticalArrangement10.AlignHorizontal = 3
    VerticalArrangement10.AlignVertical = 2
    VerticalArrangement10.Height = -2
    VerticalArrangement10.Width = 10

    // ListPicker1
    ListPicker1 = ListPicker(HorizontalArrangement_header)
    ListPicker1.ElementsFromString = "Home, Today, Journal, Settings, Resources"
    ListPicker1.Height = 30
    ListPicker1.Width = 30
    ListPicker1.Image = "menu2.png"
    ListPicker1.Title = "MENU"
    EventDispatcher.registerEventForDelegation(self, "ListPicker1", "AfterPicking")

    // VerticalArrangement12
    VerticalArrangement12 = VerticalArrangement(HorizontalArrangement_header)
    VerticalArrangement12.Width = 10

    // moodring_label
    moodring_label = Label(HorizontalArrangement_header)
    moodring_label.FontSize = 30
    moodring_label.FontTypeface = 1
    moodring_label.HasMargins = false
    moodring_label.Width = -1060
    moodring_label.Text = "Today"
    moodring_label.TextColor = Int32(bitPattern: Color.white.rawValue)

    // VerticalArrangement11
    VerticalArrangement11 = VerticalArrangement(HorizontalArrangement_header)
    VerticalArrangement11.AlignVertical = 2
    VerticalArrangement11.Width = -1010

    // Button_settings
    Button_settings = Button(HorizontalArrangement_header)
    Button_settings.BackgroundColor = Int32(bitPattern: Color.none.rawValue)
    Button_settings.Height = 35
    Button_settings.Width = 30
    Button_settings.Image = "settings.png"
    EventDispatcher.registerEventForDelegation(self, "Button_settings", "Click")

    // VerticalArrangement9
    VerticalArrangement9 = VerticalArrangement(HorizontalArrangement_header)
    VerticalArrangement9.Width = -1005
    VerticalArrangement9.Visible = false

    // ListPicker_Resources
    ListPicker_Resources = ListPicker(self)
    ListPicker_Resources.Text = "Text for ListPicker2"
    ListPicker_Resources.Visible = false
    EventDispatcher.registerEventForDelegation(self, "ListPicker_Resources", "AfterPicking")

    // VerticalArrangement1
    VerticalArrangement1 = VerticalArrangement(self)
    VerticalArrangement1.AlignHorizontal = 3
    VerticalArrangement1.Height = -2
    VerticalArrangement1.Width = -1100

    // VerticalArrangement2
    VerticalArrangement2 = VerticalArrangement(VerticalArrangement1)
    VerticalArrangement2.AlignHorizontal = 3
    VerticalArrangement2.AlignVertical = 3
    VerticalArrangement2.Height = -1008
    VerticalArrangement2.Width = -1080

    // Label_date
    Label_date = Label(VerticalArrangement2)
    Label_date.FontSize = 24
    Label_date.HasMargins = false
    Label_date.Text = "Todays date"
    Label_date.TextAlignment = 1

    // HorizontalArrangement1
    HorizontalArrangement1 = HorizontalArrangement(VerticalArrangement1)
    HorizontalArrangement1.AlignHorizontal = 3
    HorizontalArrangement1.AlignVertical = 3
    HorizontalArrangement1.Height = -1020
    HorizontalArrangement1.Width = -1080

    // TextBox_journalentry
    TextBox_journalentry = TextBox(HorizontalArrangement1)
    TextBox_journalentry.Height = -1015
    TextBox_journalentry.Width = -1073
    TextBox_journalentry.Hint = "How are you feeling?"
    TextBox_journalentry.TextAlignment = 1

    // HorizontalArrangement6
    HorizontalArrangement6 = HorizontalArrangement(VerticalArrangement1)
    HorizontalArrangement6.Height = 15

    // HorizontalArrangement7
    HorizontalArrangement7 = HorizontalArrangement(VerticalArrangement1)
    HorizontalArrangement7.AlignHorizontal = 3
    HorizontalArrangement7.Height = -1050
    HorizontalArrangement7.Width = -1080

    // VerticalArrangement3
    VerticalArrangement3 = VerticalArrangement(HorizontalArrangement7)
    VerticalArrangement3.AlignHorizontal = 3

    // Button_good
    Button_good = Button(VerticalArrangement3)
    Button_good.Height = 71
    Button_good.Width = 75
    Button_good.Image = "good.png"
    Button_good.Shape = 1
    EventDispatcher.registerEventForDelegation(self, "Button_good", "Click")

    // Label11
    Label11 = Label(VerticalArrangement3)
    Label11.HasMargins = false
    Label11.Text = "good"
    Label11.TextAlignment = 1

    // Button_happy
    Button_happy = Button(VerticalArrangement3)
    Button_happy.Height = 71
    Button_happy.Width = 75
    Button_happy.Image = "happy.png"
    Button_happy.Shape = 3
    EventDispatcher.registerEventForDelegation(self, "Button_happy", "Click")

    // Label14
    Label14 = Label(VerticalArrangement3)
    Label14.HasMargins = false
    Label14.Text = "happy"
    Label14.TextAlignment = 1

    // Button_excited
    Button_excited = Button(VerticalArrangement3)
    Button_excited.Height = 71
    Button_excited.Width = 75
    Button_excited.Image = "excited.png"
    Button_excited.Shape = 3
    EventDispatcher.registerEventForDelegation(self, "Button_excited", "Click")

    // Label17
    Label17 = Label(VerticalArrangement3)
    Label17.HasMargins = false
    Label17.Text = "excited"
    Label17.TextAlignment = 1

    // VerticalArrangement6
    VerticalArrangement6 = VerticalArrangement(HorizontalArrangement7)
    VerticalArrangement6.Width = 15

    // VerticalArrangement4
    VerticalArrangement4 = VerticalArrangement(HorizontalArrangement7)
    VerticalArrangement4.AlignHorizontal = 3

    // Button_sad
    Button_sad = Button(VerticalArrangement4)
    Button_sad.BackgroundColor = Int32(bitPattern: Color.cyan.rawValue)
    Button_sad.Height = 71
    Button_sad.Width = 75
    Button_sad.Image = "sad.png"
    Button_sad.Shape = 3
    EventDispatcher.registerEventForDelegation(self, "Button_sad", "Click")

    // Button
  }

  override func Initialize() {
    super.Initialize()
    ListPicker_Resources.ItemBackgroundColor = g$teal
    ListPicker1.ItemBackgroundColor = g$HRpurple2
    Label_date.TextColor = g$HRpurple
    Button2_enter.TextColor = g$HRpurple
    HorizontalArrangement_header.BackgroundColor = g$orange
    Label_date.Text = Clock.FormatDate(Clock1.Now(), "MMM d, yyyy")
    g$goodList = TinyDB1.GetValue("goodlist", [String]() as AnyObject) as! [String]
    g$sadList = TinyDB1.GetValue("sadlist", [String]() as AnyObject) as! [String]
    g$confusedList = TinyDB1.GetValue("confusedlist", [String]() as AnyObject) as! [String]
    g$happyList = TinyDB1.GetValue("happylist", [String]() as AnyObject) as! [String]
    g$irritatedList = TinyDB1.GetValue("irritatedlist", [String]() as AnyObject) as! [String]
    g$worriedList = TinyDB1.GetValue("worriedlist", [String]() as AnyObject) as! [String]
    g$excitedList = TinyDB1.GetValue("excitedlist", [String]() as AnyObject) as! [String]
    g$madList = TinyDB1.GetValue("madlist", [String]() as AnyObject) as! [String]
    g$lostList = TinyDB1.GetValue("lostlist", [String]() as AnyObject) as! [String]
    TextBox_journalentry.Text = ""
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }

  override func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    if componentName == "ListPicker1" && eventName == "AfterPicking" {
      ListPicker1$AfterPicking()
    } else if componentName == "Button_settings" && eventName == "Click" {
      Button_settings$Click()
    }
    return true
  }

  func ListPicker1$AfterPicking() {
    g$screensIndex = Int(ListPicker1.SelectionIndex)
    if g$screensIndex == 5 {
      ListPicker_Resources.Elements = [
        "View: Mental Health Myths and Facts",
        "Call: National Suicide Prevention Lifeline 1-800-273-TALK(8255)",
        "Visit: Suicide Prevention Lifeline.com",
        "Call: Substance Abuse and Mental Health Services Administration Treatment Referral Helpline 1-800-662-HELP (4357)",
        "Visit: Behavioral Health Treatment Services Locator https://findtreatment.samhsa.gov/",
        "Call: Trevor Project (LGBT) Helpline 866-488-7386",
        "Call: Teen Dating Abuse Helpline  866-331-9474",
        "Text: Teen Dating Helpline Text  LOVEIS to 22522",
        "Visit: Love Is Respect.org",
        "Text: Crisis Text Line - Text “START” to 741741"
      ]
      ListPicker_Resources.Open()
    } else {
      openAnotherScreen(named: g$screensList[g$screensIndex-1])
    }
  }

  func Button_settings$Click() {
    openAnotherScreen(named: "Screen6")
  }

  func ListPicker_Resources$AfterPicking() {
    if ListPicker_Resources.SelectionIndex == 1 {
      ActivityStarter1.DataUri = "http://www.mentalhealth.gov/basics/myths-facts/"
      ActivityStarter1.StartActivity()
    } else if ListPicker_Resources.SelectionIndex == 2 {
      PhoneCall1.PhoneNumber = "18002738255"
      PhoneCall1.MakePhoneCall()
    } else if ListPicker_Resources.SelectionIndex == 3 {
      ActivityStarter1.DataUri = "http://www.suicidepreventionlifeline.com/"
      ActivityStarter1.StartActivity()
    } else if ListPicker_Resources.SelectionIndex == 4 {
      PhoneCall1.PhoneNumber = "18006624357"
      PhoneCall1.MakePhoneCall()
    } else if ListPicker_Resources.SelectionIndex == 5 {
      ActivityStarter1.DataUri = "https://findtreatment.samhsa.gov/"
      ActivityStarter1.StartActivity()
    } else if ListPicker_Resources.SelectionIndex == 6 {
      PhoneCall1.PhoneNumber = "18664887386"
      PhoneCall1.MakePhoneCall()
    } else if ListPicker_Resources.SelectionIndex == 7 {
      PhoneCall1.PhoneNumber = "18663319474"
      PhoneCall1.MakePhoneCall()
    } else if ListPicker_Resources.SelectionIndex == 8 {
      Texting1.Message = "loveis"
      Texting1.PhoneNumber = "22522"
      Texting1.SendMessage()
    } else if ListPicker_Resources.SelectionIndex == 9 {
      ActivityStarter1.DataUri = "http://www.loveisrespect.org/"
      ActivityStarter1.StartActivity()
    } else if ListPicker_Resources.SelectionIndex == 10 {
      Texting1.Message = "START"
      Texting1.PhoneNumber = "741741"
      Texting1.SendMessage()
    }
  }

  func Button_good$Click() {
  }

  func Button_happy$Click() {
  }

  func Button_excited$Click() {
  }

  func Button_sad$Click() {

  }

  func Button_irritated$Click() {

  }

  func Button_mad$Click() {

  }

  func Button_confused$Click() {

  }

  func Button_worried$Click() {

  }

  func Button_lost$Click() {

  }

}
