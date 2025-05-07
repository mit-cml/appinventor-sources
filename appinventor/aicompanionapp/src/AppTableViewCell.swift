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
  
  weak var delegate: AppTableCellDelegate?
  
  
  override func awakeFromNib() {
    super.awakeFromNib()
    
    appName.font = UIFont.boldSystemFont(ofSize: 22)
    appName.textColor = UIColor(red: 146/255.0, green: 37/255.0, blue: 123/255.0, alpha: 1.0)
    
    lastOpened.font = UIFont.systemFont(ofSize: 16)
    lastOpened.textColor = UIColor(red: 100/255.0, green: 100/255.0, blue: 100/255.0, alpha: 1.0)
    
    let settingsIcon = UIImage(named: "SettingsIcon")
    settingsButton.setImage(settingsIcon, for: .normal)
    settingsButton.setTitle("", for: .normal)
    
    settingsButton.addTarget(self, action: #selector(settingsButtonTapped), for: .touchUpInside)
  }
  
  @objc func settingsButtonTapped() {
    let currentCell = self
    print("Settings button tapped")
    delegate?.didTapSettingsButton(for: currentCell)
  }
  
}
