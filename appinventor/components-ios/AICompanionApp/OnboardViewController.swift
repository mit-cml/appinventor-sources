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
    configure()
  }

  private func configure() {
    // Set up onboarding scrollview
    scrollView.translatesAutoresizingMaskIntoConstraints = false
    holderView.addSubview(scrollView)
    scrollView.topAnchor.constraint(equalTo: holderView.topAnchor).isActive = true
    scrollView.leadingAnchor.constraint(equalTo: holderView.leadingAnchor).isActive = true
    scrollView.heightAnchor.constraint(equalTo: holderView.heightAnchor).isActive = true
    scrollView.widthAnchor.constraint(equalTo: holderView.widthAnchor).isActive = true

    let labelFont = UIFont(name:"Helvetica-Bold", size: 26)
    var leadingAnchor = scrollView.leadingAnchor

    for x in 0..<titles.count {
      let pageView = UIView()
      pageView.translatesAutoresizingMaskIntoConstraints = false
      scrollView.addSubview(pageView)
      pageView.widthAnchor.constraint(equalTo: holderView.widthAnchor).isActive = true
      pageView.heightAnchor.constraint(equalTo: scrollView.heightAnchor).isActive = true
      pageView.heightAnchor.constraint(equalTo: holderView.heightAnchor).isActive = true
      pageView.topAnchor.constraint(equalTo: scrollView.topAnchor).isActive = true
      pageView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
      leadingAnchor = pageView.trailingAnchor

      // Title, image, button
      let label = UILabel()
      label.textAlignment = .center
      label.numberOfLines = 0
      label.font = labelFont
      label.text = titles[x]
      label.translatesAutoresizingMaskIntoConstraints = false
      label.adjustsFontSizeToFitWidth = true
      pageView.addSubview(label)
      // Ensure that label is always centered
      label.topAnchor.constraint(equalTo: pageView.topAnchor, constant: 10.0).isActive = true
      label.widthAnchor.constraint(equalTo: pageView.widthAnchor, constant: -20.0).isActive = true
      label.centerXAnchor.constraint(equalTo: pageView.centerXAnchor).isActive = true
      label.sizeToFit()
      label.setContentHuggingPriority(.required, for: .vertical)
      label.setContentCompressionResistancePriority(.required, for: .vertical)

      let button = UIButton()
      button.translatesAutoresizingMaskIntoConstraints = false
      pageView.addSubview(button)
      button.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor).isActive = true
      button.heightAnchor.constraint(equalToConstant: 50.0).isActive = true
      button.leadingAnchor.constraint(equalTo: pageView.leadingAnchor, constant: 10.0).isActive = true
      button.trailingAnchor.constraint(equalTo: pageView.trailingAnchor, constant: -10.0).isActive = true
      button.backgroundColor = UIColor(red: 142/255, green: 38/255, blue: 124/255, alpha: 1.0)  // RGB value for logo purple
      button.setTitleColor(.white, for: .normal)
      button.setTitle("Continue", for: .normal)
      if x == titles.count - 1 { // Max number of onboard screens
        button.setTitle("Get Started", for: .normal)
      }
      button.addTarget(self, action: #selector(didTapButton(_:)), for: .touchUpInside)
      button.tag = x+1

      let imageView = UIImageView()
      imageView.translatesAutoresizingMaskIntoConstraints = false
      pageView.addSubview(imageView)
      imageView.widthAnchor.constraint(lessThanOrEqualTo: pageView.widthAnchor, constant: -40.0).isActive = true
      imageView.centerXAnchor.constraint(equalTo: pageView.centerXAnchor).isActive = true
      imageView.topAnchor.constraint(equalTo: label.bottomAnchor, constant: 10.0).isActive = true
      imageView.bottomAnchor.constraint(equalTo: button.topAnchor, constant: -10.0).isActive = true
      imageView.contentMode = .scaleAspectFit
      imageView.image = UIImage(named: "Onboard-\(x+1)")
      imageView.setContentHuggingPriority(.defaultHigh, for: .vertical)
    }
    scrollView.trailingAnchor.constraint(equalTo: leadingAnchor).isActive = true
    scrollView.contentSize = CGSize(width: (holderView.frame.size.width * CGFloat(titles.count)),
                                    height: holderView.frame.size.height)
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
