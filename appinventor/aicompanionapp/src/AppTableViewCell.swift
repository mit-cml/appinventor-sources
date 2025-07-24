//
//  CustomTableViewCell.swift
//  AICompanionApp
//
//  Created by Mark Razanau on 4/19/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//

import UIKit

protocol AppTableCellDelegate: AnyObject {
  func didTapSettingsButton(for cell: AppTableViewCell)
}

class AppTableViewCell: UITableViewCell {
  
  @IBOutlet weak var appIconImage: UIImageView!
  @IBOutlet weak var appName: UILabel!
  @IBOutlet weak var lastOpened: UILabel!
  @IBOutlet weak var settingsButton: UIButton!
  @IBOutlet weak var warningLabel: UILabel!
  
  
  weak var delegate: AppTableCellDelegate?
  
  override func awakeFromNib() {
    super.awakeFromNib()
    
    appName.font = UIFont.boldSystemFont(ofSize: 22)
    appName.textColor = UIColor(red: 146/255.0, green: 37/255.0, blue: 123/255.0, alpha: 1.0)
    
    lastOpened.font = UIFont.systemFont(ofSize: 16)
    lastOpened.textColor = UIColor(red: 100/255.0, green: 100/255.0, blue: 100/255.0, alpha: 1.0)
    
    let settingsIcon = UIImage(named: "SettingsIcon2")
    settingsButton.setImage(settingsIcon, for: .normal)
    settingsButton.setTitle("", for: .normal)
    
    appIconImage.layer.borderColor = UIColor.purple.cgColor
    appIconImage.layer.borderWidth = 1
    appIconImage.layer.cornerRadius = 16
    
    warningLabel.text = "⚠️ WARNING: This project is out of date with the Companion App. \nPlease redownload this project in order to open it without issues."
    warningLabel.textColor = .red
    warningLabel.numberOfLines = 0
    warningLabel.lineBreakMode = .byWordWrapping
    warningLabel.font = UIFont.systemFont(ofSize: 14)
    warningLabel.isHidden = true
    
    settingsButton.addTarget(self, action: #selector(settingsButtonTapped), for: .touchUpInside)
  }
  
  @objc func settingsButtonTapped() {
    let currentCell = self
    delegate?.didTapSettingsButton(for: currentCell)
  }
  
}
