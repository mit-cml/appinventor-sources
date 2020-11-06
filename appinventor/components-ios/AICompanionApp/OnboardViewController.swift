// mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2020 Massachusetts Institute of Technology, All rights reserved.

import UIKit

class OnboardViewController: UIViewController {

  @IBOutlet var holderView: UIView!
  let scrollView = UIScrollView()
  let titles = ["Welcome to MIT App Inventor!", "Use your computer to access ai2.appinventor.mit.edu or code.appinventor.mit.edu", "Select Connect then AI Companion to start a connection", "Scan the code to finish the connection"]

  override func viewDidLoad() {
    super.viewDidLoad()

    // Do any additional setup after loading the view.
  }

  override func viewDidLayoutSubviews() {
    super.viewDidLayoutSubviews()
    configure()
  }

  private func configure() {
    // Set up onboarding scrollview
    scrollView.frame = holderView.bounds
    holderView.addSubview(scrollView)
    
    // Choose a font size that will not wrap the URLs
    var labelFont = UIFont(name:"Helvetica-Bold", size: 26)
    while ((("code.appinventor.mit.edu" as NSString).size(withAttributes: [.font: labelFont!])).width > holderView.frame.size.width-20) {
      labelFont = labelFont!.withSize(labelFont!.pointSize - 2)
    }

    for x in 0..<titles.count {
      let pageView = UIView(frame:CGRect(x: CGFloat(x) * (holderView.frame.size.width), y:0, width:holderView.frame.size.width, height:holderView.frame.size.height))
      scrollView.addSubview(pageView)

      // Title, image, button
      let label = UILabel()
      label.textAlignment = .center
      label.numberOfLines = 0
      label.frame.origin.x = 10
      label.frame.origin.y = 10
      label.frame.size.width = pageView.frame.size.width-20
      label.preferredMaxLayoutWidth = pageView.frame.size.width-20
      label.font = labelFont
      label.text = titles[x]
      pageView.addSubview(label)
      label.sizeToFit()
      label.translatesAutoresizingMaskIntoConstraints = false
      // Ensure that label is always centered
      label.centerXAnchor.constraint(equalTo: pageView.centerXAnchor).isActive = true

      let button = UIButton(frame: CGRect(x: 10, y: pageView.frame.size.height-60, width: pageView.frame.size.width-20, height: 50))
      button.backgroundColor = UIColor(red: 142/255, green: 38/255, blue: 124/255, alpha: 1.0)  // RGB value for logo purple
      button.setTitleColor(.white, for: .normal)
      button.setTitle("Continue", for: .normal)
      if x == titles.count - 1 { // Max number of onboard screens
        button.setTitle("Get Started", for: .normal)
      }
      button.addTarget(self, action: #selector(didTapButton(_:)), for: .touchUpInside)
      button.tag = x+1
      pageView.addSubview(button)

      let imageView = UIImageView(frame: CGRect(x: 10, y: label.frame.size.height + 20, width: pageView.frame.size.width-20, height: pageView.frame.size.height - label.frame.size.height - button.frame.size.height - 60))
      imageView.contentMode = .scaleAspectFit
      imageView.image = UIImage(named: "Onboard-\(x+1)")
      pageView.addSubview(imageView)
    }
    scrollView.contentSize = CGSize(width: (holderView.frame.size.width * CGFloat(titles.count)), height: 0)
    scrollView.isPagingEnabled = true
  }

  @objc func didTapButton(_ button: UIButton) {
    guard button.tag < titles.count else {
      SystemVariables.shared.setIsNotNewUser()
      dismiss(animated: true, completion: nil)
      return
    }
    scrollView.setContentOffset(CGPoint(x: holderView.frame.size.width * CGFloat(button.tag), y: 0), animated: true)
  }
}
