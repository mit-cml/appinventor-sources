//
//  AppLibraryViewController.swift
//  AICompanionApp
//
//  Created by Mark Razanau on 4/8/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//

import UIKit
import AIComponentKit


class AppLibraryViewController: UIViewController {
  @IBOutlet var libraryView: UIView!
  let scrollView = UIScrollView()
  public var form: ReplForm!
  
  override func viewWillAppear(_ animated: Bool){
    super.viewWillAppear(animated)
      configure()
  }
  
  private func configure(){

    scrollView.translatesAutoresizingMaskIntoConstraints = false
    libraryView.addSubview(scrollView)
    let libraryPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
      .appendingPathComponent("samples", isDirectory: true)
    do {
      let apps = try FileManager.default.contentsOfDirectory(at: libraryPath, includingPropertiesForKeys: nil)
      NSLayoutConstraint.activate([
        scrollView.topAnchor.constraint(equalTo: libraryView.topAnchor),
        scrollView.leadingAnchor.constraint(equalTo: libraryView.leadingAnchor),
        scrollView.trailingAnchor.constraint(equalTo: libraryView.trailingAnchor),
        scrollView.bottomAnchor.constraint(equalTo: libraryView.bottomAnchor)
      ])
      
      let pageView = UIView()
      pageView.translatesAutoresizingMaskIntoConstraints = false
      scrollView.addSubview(pageView)
      NSLayoutConstraint.activate([
        pageView.topAnchor.constraint(equalTo: scrollView.topAnchor),
        pageView.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor),
        pageView.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor),
        pageView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor),
        pageView.widthAnchor.constraint(equalTo: scrollView.widthAnchor),
        pageView.heightAnchor.constraint(equalTo: libraryView.heightAnchor, multiplier: 2)
      ])
      
      var appTitles : [String] = []
      for app in apps {
        let projectFile = app.path.components(separatedBy: "/").last!
        appTitles.append(projectFile.components(separatedBy: ".")[0])
      }
      
      if appTitles.count != 0 {
        var previousButton: UIButton? = nil
        
        for title in appTitles {
          let button = UIButton()
          button.setTitle(title, for: .normal)
          button.titleLabel?.font = UIFont.systemFont(ofSize: 18, weight: .bold)
          button.backgroundColor = .purple
          button.setTitleColor(.white, for: .normal)
          button.layer.cornerRadius = 8
          button.clipsToBounds = true
          button.translatesAutoresizingMaskIntoConstraints = false
          pageView.addSubview(button)
          
          NSLayoutConstraint.activate([
            button.centerXAnchor.constraint(equalTo: pageView.centerXAnchor),
            button.topAnchor.constraint(equalTo: previousButton?.bottomAnchor ?? pageView.topAnchor, constant: 15)
          ])
          
          button.addTarget(self, action: #selector(openDownloadedProject(_:)), for: .touchUpInside)
          
          previousButton = button
        }
      } else {
        let label1 = UILabel()
        let label2 = UILabel()
        label1.text = "You haven't downloaded any apps yet."
        label1.font = UIFont.systemFont(ofSize: 24, weight: .bold)
        label1.textColor = .black
        label1.numberOfLines = 0
        label1.lineBreakMode = .byWordWrapping
        label1.textAlignment = .center
        label2.text = "Connect to your project first to save it to you library."
        label2.font = UIFont.systemFont(ofSize: 18, weight: .bold)
        label2.textColor = .gray
        label2.numberOfLines = 0
        label2.lineBreakMode = .byWordWrapping
        label2.textAlignment = .center
        label1.translatesAutoresizingMaskIntoConstraints = false
        label2.translatesAutoresizingMaskIntoConstraints = false
        pageView.addSubview(label1)
        pageView.addSubview(label2)
        
        NSLayoutConstraint.activate([
          label1.topAnchor.constraint(equalTo: pageView.topAnchor, constant: 50),
          label1.centerXAnchor.constraint(equalTo: pageView.centerXAnchor),
          label1.leadingAnchor.constraint(equalTo: pageView.leadingAnchor, constant: 30),
          label1.trailingAnchor.constraint(equalTo: pageView.trailingAnchor, constant: -30),
          label2.topAnchor.constraint(equalTo: label1.bottomAnchor , constant: 15),
          label2.leadingAnchor.constraint(equalTo: pageView.leadingAnchor, constant: 30),
          label2.trailingAnchor.constraint(equalTo: pageView.trailingAnchor, constant: -30),
        ])
      }
    } catch {
      DispatchQueue.main.async {
        guard let window = UIApplication.shared.keyWindow else {
          return
        }
        let center = CGPoint(x: window.frame.size.width / 2.0, y: window.frame.size.height / 2.0)
        window.makeToast("Unable to find your downloaded projects :(", point: center,
                         title: nil, image: nil, completion: nil)
      }
    }
  }
  
  @objc func openDownloadedProject(_ sender: UIButton){
    let name = sender.title(for: .normal)!
    let newapp = BundledApp(aiaPath: FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
      .appendingPathComponent("samples/\(name).aia", isDirectory: false))
    newapp.makeCurrent()
    newapp.loadScreen1(self.form)
    self.navigationController?.popViewController(animated: true)
  }
}
