//
//  AppLibraryViewController.swift
//  AICompanionApp
//
//  Created by Mark Razanau on 4/8/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//

import UIKit
import AIComponentKit


class AppLibraryViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, UISearchBarDelegate, AppTableCellDelegate {
  
  @IBOutlet var libraryView: UIView!
  @IBOutlet var tableView: UITableView!
  @IBOutlet var searchBar: UISearchBar!
  @IBOutlet var libraryViewHeaderTitle: UILabel!
  public var form: ReplForm!
  private var downloadedAppsTitles: [String] = []
  private var downloadedApps: [String : DownloadedApp] = [:]
  private var filteredAppsTitles: [String] = []
  private var filteredApps: [String : DownloadedApp] = [:]
  private var isSearching: Bool = false
  private let noDownloadedAppsTitle: UILabel = {
    let label = UILabel()
    label.text = "No Apps Downloaded"
    label.textAlignment = .center
    label.font = UIFont.systemFont(ofSize: 24, weight: .medium)
    label.textColor = .black
    label.translatesAutoresizingMaskIntoConstraints = false
    label.isHidden = true
    return label
  }()
  private let noDownloadedAppsDescription: UILabel = {
    let label = UILabel()
    label.text = "Connect to your project first to download it to your library!"
    label.textAlignment = .center
    label.font = UIFont.systemFont(ofSize: 18, weight: .medium)
    label.textColor = .gray
    label.translatesAutoresizingMaskIntoConstraints = false
    label.isHidden = true
    label.numberOfLines = 0
    label.lineBreakMode = .byWordWrapping
    label.textAlignment = .center

    return label
  }()
  
  private struct DownloadedApp {
    var title: String
    var iconPath: String?
    var aiVersioning: Int?
  }
  
  private static var AIVersioning: Int = 231;
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    self.initDownloadedApps()
    self.getAppProperties()

    self.configureScreen()
    self.updateUIOnAppAvailability()
  
    self.searchBar.delegate = self
    self.tableView.delegate = self
    self.tableView.dataSource = self
  }
  
  private func configureScreen(){
    libraryViewHeaderTitle.translatesAutoresizingMaskIntoConstraints = false
    searchBar.translatesAutoresizingMaskIntoConstraints = false
    tableView.translatesAutoresizingMaskIntoConstraints = false
    libraryView.addSubview(noDownloadedAppsTitle)
    libraryView.addSubview(noDownloadedAppsDescription)

    NSLayoutConstraint.activate([
      libraryViewHeaderTitle.topAnchor.constraint(equalTo: libraryView.topAnchor, constant: 16),
      libraryViewHeaderTitle.leadingAnchor.constraint(equalTo: libraryView.leadingAnchor, constant: 16),

      searchBar.topAnchor.constraint(equalTo: libraryViewHeaderTitle.bottomAnchor, constant: 4),
      searchBar.centerXAnchor.constraint(equalTo: libraryView.centerXAnchor),
      searchBar.leadingAnchor.constraint(equalTo: libraryView.leadingAnchor, constant: 8),
      searchBar.trailingAnchor.constraint(equalTo: libraryView.trailingAnchor, constant: -8),
      searchBar.heightAnchor.constraint(equalToConstant: 44),

      tableView.topAnchor.constraint(equalTo: searchBar.bottomAnchor, constant: 8),
      tableView.centerXAnchor.constraint(equalTo: libraryView.centerXAnchor),
      tableView.bottomAnchor.constraint(equalTo: libraryView.bottomAnchor),
      
      noDownloadedAppsTitle.centerXAnchor.constraint(equalTo: libraryView.centerXAnchor),
      noDownloadedAppsTitle.centerYAnchor.constraint(equalTo: libraryView.centerYAnchor, constant: -48),
      noDownloadedAppsDescription.centerXAnchor.constraint(equalTo: libraryView.centerXAnchor),
      noDownloadedAppsDescription.leadingAnchor.constraint(equalTo: libraryView.leadingAnchor, constant: 36),
      noDownloadedAppsDescription.trailingAnchor.constraint(equalTo: libraryView.trailingAnchor, constant: -36),
      noDownloadedAppsDescription.topAnchor.constraint(equalTo: noDownloadedAppsTitle.bottomAnchor, constant: 8)
    ])
  }
  
  /**
    Check if the user has any downloaded apps.
    If yes, present the user with title, searchBar, and tableView to display the apps.
    If not, present a simple screen that informs the user they have no apps downloaded yet, with a description how to do so.
   */
  private func updateUIOnAppAvailability() {
    let hasDownloadedApps = downloadedApps.isEmpty
    tableView.isHidden = hasDownloadedApps
    libraryViewHeaderTitle.isHidden = hasDownloadedApps
    searchBar.isHidden = hasDownloadedApps
    noDownloadedAppsTitle.isHidden = !hasDownloadedApps
    noDownloadedAppsDescription.isHidden = !hasDownloadedApps
  }
  
  
  private func initDownloadedApps() {
    let libraryPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        .appendingPathComponent("samples", isDirectory: true)
    do {
      // Creates the samples directory for new users in case that it has not be created yet
      try FileManager.default.createDirectory(
          at: libraryPath,
          withIntermediateDirectories: true,
          attributes: nil)
      let apps = try FileManager.default.contentsOfDirectory(at: libraryPath, includingPropertiesForKeys: nil)
      for app in apps {
        let projectFile = app.path.components(separatedBy: "/").last!
        let appTitle = projectFile.components(separatedBy: ".")[0]
        self.downloadedAppsTitles.append(appTitle)
        self.downloadedApps[appTitle] = DownloadedApp(title: appTitle)
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
//    return appTitles
  }
  
  /**
   Access each downloaded app's properties and retrieve the path of the icon and the AI versioning to make sure that
   downloaded apps are valid and can be launched within the Companion App's system
   */
  private func getAppProperties(){
    let libraryPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
          .appendingPathComponent("apps", isDirectory: true)
    for app in self.downloadedApps.keys {
      let appPropertiesPath = libraryPath.appendingPathComponent("\(app)/youngandroidproject/project.properties")
      
      var iconPath = "default"
      var appAIVersioning = -1
      
      do {
        let content = try String(contentsOf: appPropertiesPath)
        let lines = content.components(separatedBy: .newlines)
        
        for line in lines {
          if line.trimmingCharacters(in: .whitespaces).hasPrefix("#") || line.trimmingCharacters(in: .whitespaces).isEmpty {
                          continue
                      }
          let keyValue = line.components(separatedBy: "=")
          if keyValue.count == 2 {
              let key = keyValue[0].trimmingCharacters(in: .whitespaces)
              let value = keyValue[1].trimmingCharacters(in: .whitespaces)
              if key == "icon" {
                iconPath = libraryPath.appendingPathComponent("\(app)/assets/\(value)").path
              } else if key == "aiversioning" {
                appAIVersioning = Int(value)!
              }
          }
        }
        
        self.downloadedApps[app]?.iconPath = iconPath
        self.downloadedApps[app]?.aiVersioning = appAIVersioning
        
      } catch {
        print("Was not able to read project.properties file of \(app).aia properly.")
      }
    }
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return self.isSearching ? self.filteredApps.keys.count : self.downloadedApps.keys.count
  }
  
  func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    guard (self.isSearching ? self.filteredAppsTitles : self.downloadedAppsTitles).indices.contains(indexPath.row) else {
        return 92
    }
    
    let title = isSearching ? self.filteredAppsTitles[indexPath.row] : self.downloadedAppsTitles[indexPath.row]
    
    let projectAIVersion = self.downloadedApps[title]!.aiVersioning!
    
    if  projectAIVersion < AppLibraryViewController.AIVersioning {
      return 152
    } else {
      return 92
    }
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    guard (self.isSearching ? self.filteredAppsTitles : self.downloadedAppsTitles).indices.contains(indexPath.row) else {
        return UITableViewCell()
    }
    
    let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! AppTableViewCell
    
    let title = isSearching ? self.filteredAppsTitles[indexPath.row] : self.downloadedAppsTitles[indexPath.row]
    let iconPath = isSearching ? self.filteredApps[title]!.iconPath : self.downloadedApps[title]!.iconPath
    let projectVersioning = isSearching ? self.filteredApps[title]!.aiVersioning : self.downloadedApps[title]!.aiVersioning
    
    cell.appName.text = title
    cell.lastOpened.text = "Last opened: NA"
    if iconPath == "default" {
      //TODO determine default icon
      cell.appIconImage.image = UIImage(named: "Onboard-1")
    } else {
      cell.appIconImage.image = UIImage(named: iconPath!)
    }
    if projectVersioning! < AppLibraryViewController.AIVersioning {
      cell.warningLabel.isHidden = false
    } else {
      cell.warningLabel.isHidden = true
    }
    
    cell.delegate = self
    
    return cell
  }
  
  func didTapSettingsButton(for cell: AppTableViewCell) {
    let controller = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
    controller.addAction(UIAlertAction(title: "Delete App", style: .destructive) {_ in
      guard let appName = cell.appName.text else {
          return
      }
      
      let confirmAction = UIAlertController(
        title: "Delete this app?",
        message: "This will remove \(appName) and all of its data from your phone.",
        preferredStyle: .alert)
      
      confirmAction.addAction(UIAlertAction(title: "No, Keep It", style: .cancel, handler: nil))
      confirmAction.addAction(UIAlertAction(title: "Yes, Delete It", style: .destructive, handler: {_ in
        let libraryPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let appDirectoryPath = libraryPath.appendingPathComponent("apps/\(appName)", isDirectory: true)
        let appAIAPath = libraryPath.appendingPathComponent("samples/\(appName).aia", isDirectory: false)
        do {
          try FileManager.default.removeItem(at: appDirectoryPath)
          try FileManager.default.removeItem(at: appAIAPath)
          
          if let index = self.downloadedAppsTitles.firstIndex(of: appName) {
            self.downloadedAppsTitles.remove(at: index)
            self.downloadedApps.removeValue(forKey: appName)
          }
          self.tableView.reloadData()
          self.updateUIOnAppAvailability()
          DispatchQueue.main.async {
            guard let window = UIApplication.shared.keyWindow else {
              return
            }
            let center = CGPoint(x: window.frame.size.width / 2.0, y: window.frame.size.height / 2.0)
            window.makeToast("\(appName) was successfully removed from device.", point: center,
                             title: nil, image: nil, completion: nil)
          }
        } catch {
          DispatchQueue.main.async {
            guard let window = UIApplication.shared.keyWindow else {
              return
            }
            let center = CGPoint(x: window.frame.size.width / 2.0, y: window.frame.size.height / 2.0)
            window.makeToast("Failed to removed \(appName) from device.", point: center,
                             title: nil, image: nil, completion: nil)
          }
        }
      }))
      self.present(confirmAction, animated: true, completion: nil)
    })
    controller.addAction(UIAlertAction(title: "Close", style: .cancel) { (UIAlertAction) in
      controller.dismiss(animated: true)
    })
    present(controller, animated: true)
  }
  
  func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    let name = self.downloadedAppsTitles[indexPath.row]
    let newapp = BundledApp(aiaPath: FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
      .appendingPathComponent("samples/\(name).aia", isDirectory: false))
    newapp.makeCurrent()
    newapp.loadScreen1(self.form)
    self.navigationController?.popViewController(animated: true)
  }
  
  func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    if searchText.isEmpty {
      self.isSearching = false
    } else {
      self.isSearching = true
      self.filteredApps = [:]
      self.filteredAppsTitles = []
      for title in self.downloadedApps.keys {
        if title.lowercased().contains(searchText.lowercased()) {
          self.filteredApps[title] = self.downloadedApps[title]
          self.filteredAppsTitles.append(title)
        }
      }
      print(self.filteredApps)
    }
    self.tableView.reloadData()
  }
}
