//
//  Screen3.swift
//  Mood Ring
//
//  Created by Evan Patton on 12/13/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import UIKit
import AIComponentKit

func rgbToInt32(r: Int32, g: Int32, b: Int32) -> Int32 {
  return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF)
}

class Screen3: Form {
  var g$screensList = ["Screen3", "Screen4", "Screen5", "Screen6"]
  var g$quick2weekReport = ""
  var g$HRPurple = rgbToInt32(r: 60, g: 53, b: 74)
  var g$teal = rgbToInt32(r: 154, g: 184, b: 197)
  var g$screensIndex: Int32 = 0
  var g$quoteList = [
    "\"The best revenge is massive success.\"\n- Frank Sinatra",
    "\"The only person you are destined to become is the person you decide to be.\"\n- Ralph Waldo Emerson",
    "\"Believe you can and you\u{2019}re halfway there.\"\n\u{2013}Theodore Roosevelt",
    "\"Everything you\u{2019}ve ever wanted is on the other side of fear.\"\n- George Addair",
    "\"When I let go of what I am, I become what I might be.\"\n- Lao Tzu",
    "\"Life is not measured by the number of breaths we take, but by the moments that take our breath away.\"\n\u{2013}Maya Angelou",
    "\"You can\u{2019}t fall if you don\u{2019}t climb.  But there\u{2019}s no joy in living your whole life on the ground.\"\n- Unknown",
    "\"If you want to lift yourself up, lift up someone else.\"\n- Booker T. Washington",
    "\"Limitations live only in our minds.  But if we use our imaginations, our possibilities become limitless.\"\n \u{2013}Jamie Paolinetti",
    "\"Build your own dreams, or someone else will hire you to build theirs.\"\n- Farrah Gray",
    "\"I have learned over the years that when one\u{2019}s mind is made up, this diminishes fear.\"\n \u{2013}Rosa Parks",
    "\"It does not matter how slowly you go as long as you do not stop.\"\n\u{2013}Confucius",
    "\"If you look at what you have in life, you\u{2019}ll always have more. If you look at what you don\u{2019}t have in life, you\u{2019}ll never have enough.\"\n - Oprah Winfrey",
    "\"Remember that not getting what you want is sometimes a wonderful stroke of luck.\"\n - Dalai Lama",
    "\"Dream big and dare to fail.\"\n -Norman Vaughan",
    "\"Do what you can, where you are, with what you have.\"\n \u{2013}Teddy Roosevelt",
    "\"If you do what you\u{2019}ve always done, you\u{2019}ll get what you\u{2019}ve always gotten.\"\n- Tony Robbins",
    "\"It\u{2019}s your place in the world; it\u{2019}s your life. Go on and do all you can with it, and make it the life you want to live.\"\n- Mae Jemison",
    "\"Remember no one can make you feel inferior without your consent.\"\n \u{2013}Eleanor Roosevelt",
    "\"The question isn\u{2019}t who is going to let me; it\u{2019}s who is going to stop me.\"\n\u{2013}Ayn Rand",
    "\"A journey of a thousand miles begins with a single step.\"\n - Lao Tzu",
    "\"You have enemies? Good. That means you've stood up for something, sometime in your life.\"\n- Winston Churchill",
    "\"Perfection is not attainable, but if we chase perfection we can catch excellence.\"\n- Vince Lombardi",
    "\"I can\u{2019}t change the direction of the wind, but I can adjust my sails to always reach my destination.\"\n \u{2014}Jimmy Dean",
    "\"What\u{2019}s money? A man is a success if he gets up in the morning and goes to bed at night and in between does what he wants to do.\"\n- Bob Dylan",
    "\"I alone cannot change the world, but I can cast a stone across the water to create many ripples.\"\n- Mother Teresa",
    "\u{201c}It is never too late to be what you might have been.\u{201d} \n- George Elliot",
    "\u{201c}To hell with circumstances. I create opportunities.\u{201d}\n - Bruce Lee",
    "\u{201c}If you can\u{2019}t fly, then run, if you can\u{2019}t run, then walk, if you can\u{2019}t walk, then crawl, but whatever you do, keep moving forward.\u{201d} \n- Martin Luther King Jr. ",
    "\u{201c}It\u{2019}s hard to beat a person who never gives up.\u{201d} \n- Babe Ruth",
    "\u{201c}If you\u{2019}re going through hell, keep going.\u{201d}\n - Winston Churchill",
    "\"You\u{2019}re off to Great places! Today is your day! Your mountain is waiting! So.....get on your way!\u{201d}\n - Dr. Suess",
    "\"Differences of habit and language are nothing at all if our aims are identical and our hearts are identical.\"\n - Albus Dumbledore, Harry Potter in the Goblet of fire",
    "\u{201c}Perfection cannot be obtained, but if we chase perfection we can catch excellence.\u{201d}\n- Vince Lombardi",
    "\u{201c}The greatest waste in the world is the difference between what we are and what we could become.\u{201d}\n- Ben Herbster",
    "\u{201c}Faith is not knowing that if you must take a step off a cliff, either a step will appear or you will learn how to fly.\u{201d}\n",
    "\u{201c}Worrying is using your imagination to create something you don\u{2019}t want.\u{201d}\n- Mayan Proverb",
    "\u{201c}Nothing will work unless you do.\u{201d}\n- Maya Angelou",
    "\u{201c}Growing old is mandatory. Growing up is optional.\u{201d}\n- Carroll Bryant",
    "\u{201c}Certain things catch your eye. But pursue only those that capture your heart.\u{201d}\n- Native American Proverb",
    "\"Strive not to be a success, but rather to be of value. \"\n- Albert Einstein",
    "\u{201c}Listen with your heart. Learn from your experiences and always be open to new ones.\u{201d}\n- Cherokee Proverb",
    "\u{201c}You can\u{2019}t calm the storm, so stop trying. What you can do is calm yourself. The storm will pass.\u{201d}\n- Timber Hawkeye",
    "\"You miss 100% of the shots you don\u{2019}t take.\"\n -Wayne Gretzky",
    "\"Every strike brings me closer to the next home run.\"\n -Babe Ruth",
    "\"I am not a product of my circumstances. I am a product of my decisions.\"\n- Stephen Covey",
    "\"The two most important days in your life are the day you are born and the day you find out why.\"\n- Mark Twain",
    "\"Make mistakes, take chances and make messes.\"\n- Ms. Frizzle, The Magic School Bus",
    "\"Oh yes, the past can hurt. But you can either run from it or learn from it.\"\n- Rafiki, Lion King.",
    "\"Anything\u{2019}s possible when you have inner peace.\"\n- Master Shifu, Kung Fu Panda",
    "\u{201c}When one is released from the prison of self, that is indeed freedom! For self is the greatest prison.\u{201d} \nAbdu\u{2019}l - Baha ",
    "\"Anything\u{2019}s possible when you have inner peace.\"\n- Master Shifu, Kung Fu Panda",
    "\"Make mistakes, take chances and make messes.\"\n- Ms. Frizzle, The Magic School Bus"
  ]

  var HorizontalArrangement1: HorizontalArrangement!
  var VerticalArrangement6: VerticalArrangement!
  var ListPicker1: ListPicker!
  var VerticalArrangement4: VerticalArrangement!
  var moodring_label: Label!
  var VerticalArrangement5: VerticalArrangement!
  var Button_settings: Button!
  var ListPicker_Resources: ListPicker!
  var VerticalArrangement1: VerticalArrangement!
  var HorizontalArrangement4: HorizontalArrangement!
  var Label3: Label!
  var Label_Hello: Label!
  var HorizontalArrangement2: HorizontalArrangement!
  var Button_emoji: Button!
  var HorizontalArrangement3: HorizontalArrangement!
  var Label_quote: Label!
  var Label4: Label!
  var enter_button: Button!
  var HorizontalArrangement6: HorizontalArrangement!
  var journal_Button1: Button!
  var TinyDB1: TinyDB!
  var Notifier_username: Notifier!
  var Notifier_friendName: Notifier!
  var Notifier_friendNum: Notifier!
  var Clock1: Clock!
  var Sound1: Sound!
  var Notifier1: Notifier!
  var Notifier_send: Notifier!
  var Sharing1: Sharing!
  var Notifier_text: Notifier!
  var Texting1: Texting!
  var PhoneCall1: PhoneCall!
  var ActivityStarter1: ActivityStarter!

  init?(_ coder: NSCoder? = nil) {
    if let coder = coder {
      super.init(coder: coder)
    } else {
      super.init(nibName: nil, bundle: nil)
    }
    application = UIApplication.shared.delegate as? Application
  }

  required convenience init?(coder aCoder: NSCoder) {
    self.init(aCoder)
  }

  override func viewDidLoad() {
    super.viewDidLoad()

    // do-after-form-creation
    AlignHorizontal = 3
    AppName = "moodring1"
    BackgroundImage = "background.png"
    ScreenOrientation = "portrait"
    Scrollable = true
    ShowStatusBar = false
    Sizing = "Responsive"
    TitleVisible = false

    // HorizontalArrangement1
    HorizontalArrangement1 = HorizontalArrangement(self)
    HorizontalArrangement1.AlignVertical = 2
    HorizontalArrangement1.BackgroundColor = Int32(bitPattern: Color.white.rawValue)
    HorizontalArrangement1.Height = -1010
    HorizontalArrangement1.Width = -2

    // VerticalArrangement6
    VerticalArrangement6 = VerticalArrangement(HorizontalArrangement1)
    VerticalArrangement6.Width = 10

    // ListPicker1
    ListPicker1 = ListPicker(HorizontalArrangement1)
    ListPicker1.ElementsFromString = "Home, Today, Journal, Settings, Resources"
    ListPicker1.Height = 30
    ListPicker1.Width = 30
    ListPicker1.Image = "menu2.png"
    ListPicker1.ItemBackgroundColor = Int32(bitPattern: Color.blue.rawValue)
    ListPicker1.TextAlignment = 0
    EventDispatcher.registerEventForDelegation(self, "ListPicker1", "AfterPicking")

    // VerticalArrangement1
    VerticalArrangement4 = VerticalArrangement(HorizontalArrangement1)
    VerticalArrangement4.BackgroundColor = Int32(bitPattern: Color.none.rawValue)
    VerticalArrangement4.Width = 10

    // moodring_label
    moodring_label = Label(HorizontalArrangement1)
    moodring_label.FontSize = 30
    moodring_label.HasMargins = false
    moodring_label.Width = -1060
    moodring_label.Text = "Mood Ring"
    moodring_label.TextColor = Int32(bitPattern: Color.white.rawValue)

    // VerticalArrangement5
    VerticalArrangement5 = VerticalArrangement(HorizontalArrangement1)
    VerticalArrangement5.BackgroundColor = Int32(bitPattern: Color.none.rawValue)
    VerticalArrangement5.Width = -1010

    // Button_settings
    Button_settings = Button(HorizontalArrangement1)
    Button_settings.Height = 35
    Button_settings.Width = 30
    Button_settings.Image = "settings.png"
    Button_settings.Shape = 3
    EventDispatcher.registerEventForDelegation(self, "Button_settings", "Click")

    // ListPicker_Resources
    ListPicker_Resources = ListPicker(self)
    ListPicker_Resources.Text = "Text for ListPicker2"
    ListPicker_Resources.Title = "Resources"
    ListPicker_Resources.Visible = false
    EventDispatcher.registerEventForDelegation(self, "ListPicker_Resources", "AfterPicking")

    // VerticalArrangement1
    VerticalArrangement1 = VerticalArrangement(self)
    VerticalArrangement1.AlignVertical = 3
    VerticalArrangement1.BackgroundColor = Int32(bitPattern: Color.none.rawValue)
    VerticalArrangement1.Width = -1100

    // HorizontalArrangement4
    HorizontalArrangement4 = HorizontalArrangement(VerticalArrangement1)
    HorizontalArrangement4.AlignHorizontal = 3
    HorizontalArrangement4.AlignVertical = 2
    HorizontalArrangement4.BackgroundColor = Int32(bitPattern: Color.none.rawValue)
    HorizontalArrangement4.Height = -1015
    HorizontalArrangement4.Width = -2

    // Label3
    Label3 = Label(HorizontalArrangement4)
    Label3.HasMargins = false
    Label3.Height = 20
    Label3.Width = 20

    // Label_Hello
    Label_Hello = Label(HorizontalArrangement4)
    Label_Hello.FontSize = 30
    Label_Hello.FontTypeface = 1
    Label_Hello.HasMargins = false
    Label_Hello.Width = -2
    Label_Hello.Text = "Hello !"
    Label_Hello.TextAlignment = 1
    Label_Hello.TextColor = Int32(bitPattern: Color.white.rawValue)

    // HorizontalArrangement2
    HorizontalArrangement2 = HorizontalArrangement(VerticalArrangement1)
    HorizontalArrangement2.AlignHorizontal = 3
    HorizontalArrangement2.AlignVertical = 2
    HorizontalArrangement2.BackgroundColor = Int32(bitPattern: Color.none.rawValue)
    HorizontalArrangement2.Height = -1025
    HorizontalArrangement2.Width = -1100

    // Button_emoji
    Button_emoji = Button(HorizontalArrangement2)
    Button_emoji.Height = 143
    Button_emoji.Width = 143
    Button_emoji.Image = "Emoji_home.png"
    EventDispatcher.registerEventForDelegation(self, "Button_emoji", "Click")

    // HorizontalArrangement3
    HorizontalArrangement3 = HorizontalArrangement(VerticalArrangement1)
    HorizontalArrangement3.AlignHorizontal = 3
    HorizontalArrangement3.AlignVertical = 2
    HorizontalArrangement3.BackgroundColor = Int32(bitPattern: Color.none.rawValue)
    HorizontalArrangement3.Height = -1025
    HorizontalArrangement3.Width = -2

    // Label_quote
    Label_quote = Label(HorizontalArrangement3)
    Label_quote.FontItalic = true
    Label_quote.FontSize = 18
    Label_quote.HasMargins = false
    Label_quote.Width = -1080
    Label_quote.Text = "Good vibes. Love peace and soul!"
    Label_quote.TextAlignment = 1
    Label_quote.TextColor = Int32(bitPattern: Color.white.rawValue)

    // Label4
    Label4 = Label(HorizontalArrangement3)
    Label4.HasMargins = false
    Label4.Width = 20
    Label4.Visible = false

    // enter_button
    enter_button = Button(self)
    enter_button.BackgroundColor = Int32(bitPattern: Color.none.rawValue)
    enter_button.FontSize = 14
    enter_button.Height = -1007
    enter_button.Width = -1075
    enter_button.Image = "button_test.png"
    enter_button.Text = "How Are You?"
    enter_button.TextColor = Int32(bitPattern: Color.white.rawValue)
    EventDispatcher.registerEventForDelegation(self, "enter_button", "Click")

    // HorizontalArrangement6
    HorizontalArrangement6 = HorizontalArrangement(self)
    HorizontalArrangement6.Height = -1003
    HorizontalArrangement6.Width = -2

    // journal_Button1
    journal_Button1 = Button(self)
    journal_Button1.BackgroundColor = Int32(bitPattern: Color.none.rawValue)
    journal_Button1.Height = -1007
    journal_Button1.Width = -1075
    journal_Button1.Image = "button_test.png"
    journal_Button1.Text = "Check Your Journal"
    journal_Button1.TextColor = Int32(bitPattern: Color.white.rawValue)
    EventDispatcher.registerEventForDelegation(self, "journal_Button1", "Click")

    // TinyDB1
    TinyDB1 = TinyDB(self)

    // Notifier_username
    Notifier_username = Notifier(self)
    EventDispatcher.registerEventForDelegation(self, "Notifier_username", "AfterTextInput")

    // Notifier_friendName
    Notifier_friendName = Notifier(self)
    EventDispatcher.registerEventForDelegation(self, "Notifier_friendName", "AfterTextInput")

    // Notifier_friendNum
    Notifier_friendNum = Notifier(self)
    EventDispatcher.registerEventForDelegation(self, "Notifier_friendNum", "AfterChoosing")
    EventDispatcher.registerEventForDelegation(self, "Notifier_friendNum", "AfterTextInput")

    // Clock1
    Clock1 = Clock(self)
    EventDispatcher.registerEventForDelegation(self, "Clock1", "Timer")

    // Sound1
    Sound1 = Sound(self)

    // Notifier1
    Notifier1 = Notifier(self)

    // Notifier_send
    Notifier_send = Notifier(self)
    EventDispatcher.registerEventForDelegation(self, "Notifier_send", "AfterChoosing")

    // Sharing1
    Sharing1 = Sharing(self)

    // Notifier_text
    Notifier_text = Notifier(self)
    EventDispatcher.registerEventForDelegation(self, "Notifier_text", "AfterTextInput")

    // Texting1
    Texting1 = Texting(self)

    // PhoneCall1
    PhoneCall1 = PhoneCall(self)

    // ActivityStarter1
    ActivityStarter1 = ActivityStarter(self)
    ActivityStarter1.Action = "android.intent.action.VIEW"
  }

  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    Initialize()
  }

  override func Initialize() {
    super.Initialize()
//    ListPicker_Resources.ItemBackgroundColor = g$teal
//    Label_quote.Text = g$quoteList[Int(arc4random_uniform(UInt32(g$quoteList.count)))]
//    HorizontalArrangement1.BackgroundColor = g$teal
//    ListPicker1.ItemBackgroundColor = g$HRPurple
//    if TinyDB1.GetValue("username", "" as AnyObject) as! String == "" {
//      Notifier_username.ShowTextDialog("Welcome to Mood Ring: a youth-made app that helps you track your emotions and reach out when you need to.\n\nLet's start with your name. (Only you will see your info!)", "Welcome to Mood Ring!", false)
//    } else {
//      Label_Hello.Text = "Hello " + (TinyDB1.GetValue("username", "" as AnyObject) as! String)
//    }
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }

  override func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    if componentName == "ListPicker1" && eventName == "AfterPicking" {
      ListPicker1$AfterPicking()
    } else if componentName == "Button_settings" && eventName == "Click" {
      Button_settings$Click()
    } else if componentName == "ListPicker_Resources" && eventName == "AfterPicking" {
      ListPicker_Resources$AfterPicking()
    } else if componentName == "Button_emoji" && eventName == "Click" {
      Button_emoji$Click()
    } else if componentName == "enter_button" && eventName == "Click" {
      enter_button$Click()
    } else if componentName == "journal_Button1" && eventName == "Click" {
      journal_Button1$Click()
    } else if componentName == "Notifier_username" && eventName == "AfterTextInput" {
      Notifier_username$AfterTextInput(response: args[0] as! String)
    } else if componentName == "Notifier_friendName" && eventName == "AfterTextInput" {
      Notifier_friendName$AfterTextInput(response: args[0] as! String)
    } else if componentName == "Notifier_friendNum" && eventName == "AfterChoosing" {
      Notifier_friendNum$AfterChoosing(choice: args[0] as! String)
    } else if componentName == "Notifier_friendNum" && eventName == "AfterTextInput" {
      Notifier_friendNum$AfterTextInput(response: args[0] as! String)
    } else if componentName == "Clock1" && eventName == "Timer" {
      Clock1$Timer()
    } else if componentName == "Notifier_send" && eventName == "AfterChoosing" {
      Notifier_send$AfterChoosing(choice: args[0] as! String)
    } else if componentName == "Notifier_text" && eventName == "AfterTextInput" {
      Notifier_text$AfterTextInput(response: args[0] as! String)
    } else {
      return false  // no registered component
    }
    return true
  }

  func p$createQuick2WeekReport() {
    let formatter = NumberFormatter()
    formatter.maximumFractionDigits = 0
    g$quick2weekReport = "This is your Emotion Report on how you’ve been feeling over the last two weeks. Of all the emojis you picked, here’s how much of each feeling you’ve experienced.\n\n" +
      "Good - " + formatter.string(for: TinyDB1.GetValue("goodlistPercent", "0" as AnyObject))! + "%\n" +
      "Sad - " + formatter.string(for: TinyDB1.GetValue("sadlistPercent", "0" as AnyObject))! + "%\n" +
      "Confused - " + formatter.string(for: TinyDB1.GetValue("confusedlistPercent", "0" as AnyObject))! + "%\n" +
      "Happy - " + formatter.string(for: TinyDB1.GetValue("happylistPercent", "0" as AnyObject))! + "%\n" +
      "Irritated - " + formatter.string(for: TinyDB1.GetValue("irritatedlistPercent", "0" as AnyObject))! + "%\n" +
      "Worried - " + formatter.string(for: TinyDB1.GetValue("worriedlistPercent", "0" as AnyObject))! + "%\n" +
      "Excited - " + formatter.string(for: TinyDB1.GetValue("excitedlistPercent", "0" as AnyObject))! + "%\n" +
      "Lost - " + formatter.string(for: TinyDB1.GetValue("lostlistPercent", "0" as AnyObject))! + "%\n" +
      "Mad - " + formatter.string(for: TinyDB1.GetValue("madlistPercent", "0" as AnyObject))! + "%\n" +
      "%\n\nTo send yourself the report, hit \"Save.\" To reach out, hit \"Contact.\""
    Notifier_send.ShowChooseDialog(g$quick2weekReport, "Emotion Report", "Save", "Contact", true)
  }

  func p$sendData() {
    Sharing1.ShareMessage(
      (TinyDB1.GetValue("goodSend", "" as AnyObject) as! String) +
      (TinyDB1.GetValue("sadSend", "" as AnyObject) as! String) +
      (TinyDB1.GetValue("confusedSend", "" as AnyObject) as! String) +
      (TinyDB1.GetValue("happySend", "" as AnyObject) as! String) +
      (TinyDB1.GetValue("irritatedSend", "" as AnyObject) as! String) +
      (TinyDB1.GetValue("worriedSend", "" as AnyObject) as! String) +
      (TinyDB1.GetValue("excitedSend", "" as AnyObject) as! String) +
      (TinyDB1.GetValue("lostSend", "" as AnyObject) as! String) +
      (TinyDB1.GetValue("madSend", "" as AnyObject) as! String)
    )
  }

  func ListPicker1$AfterPicking() {
    g$screensIndex = ListPicker1.SelectionIndex
    if g$screensIndex == 5 {
      ListPicker_Resources.Elements = [
        "View: Mental Health Myths and Facts",
        "Call: National Suicide Prevention Lifeline 1-800-273-TALK(8255)",
        "Visit: Suicide Prevention Lifeline.com",
        "Call: Substance Abuse and Mental Health Services Administration Treatment Referral Helpline 1-800-662-HELP (4357)",
        "Visit: Behavioral Health Treatment Services Locator https://findtreatment.samhsa.gov/",
        "Call: Trevor Project (LGBT) Helpline 866-488-7386",
        "Call: Teen Dating Abuse Helpline  866-331-9474",
        "Text: Teen Dating Helpline Text  LOVEIS to 22522 ",
        "Visit: Love Is Respect.org",
        "Text: Crisis Text Line - Text \u{201c}START\u{201d} to 741741"
      ]
      ListPicker_Resources.Open()
    } else {
      openAnotherScreen(named: g$screensList[g$screensIndex - 1])
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

  func Button_emoji$Click() {
    Label_quote.Text = g$quoteList[Int(arc4random_uniform(UInt32(g$quoteList.count)))]
  }

  func enter_button$Click() {
    openAnotherScreen(named: "Screen4")
  }

  func journal_Button1$Click() {
    openAnotherScreen(named: "Screen5")
  }

  func Notifier_username$AfterTextInput(response: String) {
    TinyDB1.StoreValue("username", response as AnyObject)
    Label_Hello.Text = "Hello " + (TinyDB1.GetValue("username", "" as AnyObject) as! String) + "!"
    Notifier_friendName.ShowTextDialog("Who do you talk to when you aren\u{2019}t feeling your best?",
                                       "Mood Ring helps you reach out", true)
  }

  func Notifier_friendName$AfterTextInput(response: String) {
    TinyDB1.StoreValue("friendName", response as AnyObject)
    Notifier_friendNum.ShowTextDialog("This app will never contact your friends or share your info without your permission.\nWhat's their number?", "Mood Ring helps you reach out", false)
  }

  func Notifier_friendNum$AfterChoosing(choice: String) {
    if choice == "Settings" {
      openAnotherScreen(named: "Screen6")
    }
  }

  func Notifier_friendNum$AfterTextInput(response: String) {
    TinyDB1.StoreValue("friendNum", response as AnyObject)
    Notifier_friendNum.ShowChooseDialog("Thanks. This info is saved in your Settings. You can go to Settings to add another contact.", "Mood Ring", "Settings", "Ok", false)
  }

  func Clock1$Timer() {
    TinyDB1.StoreValue("timeNow", Clock.FormatDate(Clock1.Now(), "MM/dd/yyyy hh:mm:ss a") as AnyObject)
    if TinyDB1.GetValue("timeNow", "" as AnyObject) as! String == TinyDB1.GetValue("dateInTwo", "" as AnyObject) as! String {
      Sound1.Vibrate(800)
      p$createQuick2WeekReport()
    }
  }

  func Notifier_send$AfterChoosing(choice: String) {
    if choice == "Save" {
      p$sendData()
    } else if choice == "Contact" {
      if TinyDB1.GetValue("customText", "" as AnyObject) as! String == "" {
        Notifier_text.ShowTextDialog("Sometimes it's good to reach out to a friend, and check in about how you are feeling. Lets send a text to " + (TinyDB1.GetValue("friendName", "" as AnyObject) as! String) + ": \"Hey I could really use someone to talk to right now\"\n\nIf you would like to send a custom message type it in below.", "Text A Fried", true)
      } else {
        Notifier_text.ShowTextDialog("Sometimes it's good to reach out to a friend, and check in about how you are feeling. Lets send a text to " + (TinyDB1.GetValue("friendName", "" as AnyObject) as! String) + ":" + (TinyDB1.GetValue("customText", "" as AnyObject) as! String) + "\n\nIf you would like to send a different message type it in below.", "Text A Friend", true)
      }
    }
    TinyDB1.StoreValue("dateInTwo", "noData" as AnyObject)
    TinyDB1.StoreValue("firstDate", "noData" as AnyObject)
  }

  func Notifier_text$AfterTextInput(response: String) {
    if response == "" {
      Texting1.Message = "Hey, do you have a minute? I could use a friend."
      Texting1.PhoneNumber = TinyDB1.GetValue("friendNum", "" as AnyObject) as! String
      Texting1.SendMessage()
    } else {
      Texting1.PhoneNumber = TinyDB1.GetValue("friendNum", "" as AnyObject) as! String
      Texting1.Message = response
      Texting1.SendMessage()
    }
  }
}

