// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit
import AIComponentKit


class AppLibraryViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, UISearchBarDelegate, AppTableCellDelegate {
  
  private static var AIVersioning: Int = 231;
  
  @IBOutlet var libraryView: UIView!
  @IBOutlet var tableView: UITableView!
  @IBOutlet var searchBar: UISearchBar!
  @IBOutlet var libraryViewHeaderTitle: UILabel!
  @IBOutlet var sortButton: UIButton!
  public var form: ReplForm!
  private var downloadedAppsMap: [String : DownloadedApp] = [:]
  private var downloadedApps: [DownloadedApp] = []
  private var filteredApps: [DownloadedApp] = []
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
  
  private struct DownloadedApp : Comparable {
    var title: String
    var iconPath: String?
    var aiVersioning: Int?
    var lastOpened: String?
    
    static func < (left: DownloadedApp, right: DownloadedApp) -> Bool {
      left.title.localizedCaseInsensitiveCompare(right.title) == .orderedAscending
    }
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    self.initDownloadedApps()

    self.configureScreen()
    self.updateUIOnAppAvailability()
    
    self.sortApps()
      
    self.searchBar.delegate = self
    self.tableView.delegate = self
    self.tableView.dataSource = self
  }
  
  private func configureScreen(){
    sortButton.addTarget(self, action: #selector(showSortingMenu(_:)), for: .touchUpInside)
    libraryViewHeaderTitle.translatesAutoresizingMaskIntoConstraints = false
    searchBar.translatesAutoresizingMaskIntoConstraints = false
    if let field = searchBar.value(forKey: "searchField") as? UITextField {
      field.layer.cornerRadius = 8
      field.layer.masksToBounds = true
      field.layer.borderWidth = 2
      field.layer.borderColor = UIColor.purple.cgColor
      field.leftView?.tintColor = .purple
    }
    tableView.translatesAutoresizingMaskIntoConstraints = false
    tableView.separatorInset = UIEdgeInsets(top: 0, left: 24, bottom: 0, right: 24)
    sortButton.translatesAutoresizingMaskIntoConstraints = false
    libraryView.addSubview(noDownloadedAppsTitle)
    libraryView.addSubview(noDownloadedAppsDescription)

    NSLayoutConstraint.activate([
      libraryViewHeaderTitle.topAnchor.constraint(equalTo: libraryView.topAnchor, constant: 16),
      libraryViewHeaderTitle.leadingAnchor.constraint(equalTo: libraryView.leadingAnchor, constant: 16),

      searchBar.topAnchor.constraint(equalTo: libraryViewHeaderTitle.bottomAnchor, constant: 8),
      searchBar.centerXAnchor.constraint(equalTo: libraryView.centerXAnchor),
      searchBar.leadingAnchor.constraint(equalTo: libraryView.leadingAnchor, constant: 8),
      searchBar.trailingAnchor.constraint(equalTo: libraryView.trailingAnchor, constant: -8),
      searchBar.heightAnchor.constraint(equalToConstant: 44),
      
      sortButton.bottomAnchor.constraint(equalTo: searchBar.topAnchor, constant: -8),
      sortButton.trailingAnchor.constraint(equalTo: libraryView.trailingAnchor, constant: -16),
      sortButton.widthAnchor.constraint(equalToConstant: 80),

      tableView.topAnchor.constraint(equalTo: searchBar.bottomAnchor, constant: 8),
      tableView.leadingAnchor.constraint(equalTo: libraryView.leadingAnchor),
      tableView.trailingAnchor.constraint(equalTo: libraryView.trailingAnchor),
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
    let hasDownloadedApps = downloadedAppsMap.isEmpty
    tableView.isHidden = hasDownloadedApps
    libraryViewHeaderTitle.isHidden = hasDownloadedApps
    searchBar.isHidden = hasDownloadedApps
    sortButton.isHidden = hasDownloadedApps
    noDownloadedAppsTitle.isHidden = !hasDownloadedApps
    noDownloadedAppsDescription.isHidden = !hasDownloadedApps
  }
  
  @objc private func showSortingMenu(_ sender: UIButton){
    let alert = UIAlertController(title: "Sort By:", message: nil, preferredStyle: .actionSheet)
    
    let actions: [(String, () -> Void)] = [
      ("Recently Opened \(SystemVariables.sortModeValue == .mostRecent ? "✔" : "")", {
        SystemVariables.sortModeValue = SystemVariables.sortMode.mostRecent
        self.sortApps()
      }),
      ("App Name: A-Z \(SystemVariables.sortModeValue == .AZ ? "✔" : "")", {
        SystemVariables.sortModeValue = SystemVariables.sortMode.AZ
        self.sortApps()
      }),
      ("App Name: Z-A \(SystemVariables.sortModeValue == .ZA ? "✔" : "")", {
        SystemVariables.sortModeValue = SystemVariables.sortMode.ZA
        self.sortApps()
      })
    ]
    
    for (title, handler) in actions {
      let alertAction = UIAlertAction(title: title, style: .default) {_ in handler()}
      alert.addAction(alertAction)
    }
    
    alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
    
    present(alert, animated: true)
  }
  
  /**
   Access each downloaded app's properties and retrieve the path of the icon and the AI versioning to make sure that
   downloaded apps are valid and can be launched within the Companion App's system
   */
  private func initDownloadedApps() {
    let samplesPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        .appendingPathComponent("samples", isDirectory: true)
    let appPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        .appendingPathComponent("apps", isDirectory: true)
    do {
      // Creates the samples directory for new users in case that it has not be created yet
      try FileManager.default.createDirectory(
          at: samplesPath,
          withIntermediateDirectories: true,
          attributes: nil)
      let apps = try FileManager.default.contentsOfDirectory(at: samplesPath, includingPropertiesForKeys: nil)
      for app in apps {
        let projectFile = app.path.components(separatedBy: "/").last!
        let appTitle = projectFile.components(separatedBy: ".")[0]
        var curApp = DownloadedApp(title: appTitle)
        
        let appPropertiesPath = appPath.appendingPathComponent("\(appTitle)/youngandroidproject/project.properties")
        
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
                iconPath = appPath.appendingPathComponent("\(appTitle)/assets/\(value)").path
              } else if key == "aiversioning" {
                appAIVersioning = Int(value)!
              }
            }
          }
          
          curApp.iconPath = iconPath
          curApp.aiVersioning = appAIVersioning
          
          if let lastOpenedDate = SystemVariables.lastOpenedTable[appTitle] {
            let timeComponents = Calendar.current.dateComponents([.day, .hour, .minute], from: lastOpenedDate, to: Date())
            if timeComponents.day != 0 {
              curApp.lastOpened = "\(timeComponents.day!) day\(timeComponents.day! == 1 ? "" : "s") ago"
            } else if timeComponents.hour != 0 {
              curApp.lastOpened = "\(timeComponents.hour!) hour\(timeComponents.hour! == 1 ? "" : "s") ago"
            } else if timeComponents.minute != 0 {
              curApp.lastOpened = "\(timeComponents.minute!) minute\(timeComponents.minute! == 1 ? "" : "s") ago"
            } else {
              curApp.lastOpened = "<1 minute ago"
            }
          } else {
            curApp.lastOpened = "NA"
          }
        } catch {
          print("Was not able to read project.properties file of \(app).aia properly.")
        }
        
        self.downloadedApps.append(curApp)
        self.downloadedAppsMap[appTitle] = curApp
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
  
  private func sortApps() {
    let sortingMode = SystemVariables.sortModeValue
    
    switch sortingMode {
    case SystemVariables.sortMode.mostRecent:
      self.downloadedApps.sort {(left: DownloadedApp, right: DownloadedApp) in SystemVariables.lastOpenedTable[left.title] ?? .distantPast > SystemVariables.lastOpenedTable[right.title] ?? .distantPast}
      break
    case SystemVariables.sortMode.AZ:
      self.downloadedApps.sort {(left: DownloadedApp, right: DownloadedApp) in left.title.localizedCaseInsensitiveCompare(right.title) == .orderedAscending}
      break
    case SystemVariables.sortMode.ZA:
      self.downloadedApps.sort {(left: DownloadedApp, right: DownloadedApp) in left.title.localizedCaseInsensitiveCompare(right.title) == .orderedDescending}
      break
    }
    
    self.tableView.reloadData()
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return self.isSearching ? self.filteredApps.count : self.downloadedApps.count
  }
  
  func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    guard (self.isSearching ? self.filteredApps : self.downloadedApps).indices.contains(indexPath.row) else {
        return 86
    }
    
    let app = (self.isSearching ? self.filteredApps : self.downloadedApps)[indexPath.row]
    let projectAIVersion = app.aiVersioning ?? -1
    
    if  projectAIVersion < AppLibraryViewController.AIVersioning {
      return 152
    } else {
      return 86
    }
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    guard (self.isSearching ? self.filteredApps : self.downloadedApps).indices.contains(indexPath.row) else {
        return UITableViewCell()
    }
    
    let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! AppTableViewCell
    
    let app = isSearching ? self.filteredApps[indexPath.row] : self.downloadedApps[indexPath.row]
    let title = app.title
    let iconPath = app.iconPath ?? "default"
    let projectVersioning = app.aiVersioning ?? -1
    let lastOpened = app.lastOpened ?? "NA"
    
    cell.appName.text = title
    cell.lastOpened.text = "Last opened: " + lastOpened
    if iconPath == "default" {
      //TODO determine default icon
      cell.appIconImage.image = UIImage(named: "Onboard-1")
    } else {
      cell.appIconImage.image = UIImage(named: iconPath)
    }
    if projectVersioning < AppLibraryViewController.AIVersioning {
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
          
          // Remove the app from the app dictionary and the corresponding array lists
          self.downloadedAppsMap.removeValue(forKey: appName)
          for (index, app) in self.downloadedApps.enumerated() {
            if app.title == appName {
              self.downloadedApps.remove(at: index)
              break
            }
          }
          if self.isSearching {
            for (index, app) in self.filteredApps.enumerated() {
              if app.title == appName {
                self.filteredApps.remove(at: index)
                break
              }
            }
          }
          
          SystemVariables.lastOpenedTable.removeValue(forKey: appName)
          
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
    let app = self.downloadedApps[indexPath.row]
    SystemVariables.lastOpenedTable[app.title] = Date()
    let newapp = BundledApp(aiaPath: FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
      .appendingPathComponent("samples/\(app.title).aia", isDirectory: false))
    newapp.makeCurrent()
    newapp.loadScreen1(self.form)
    self.navigationController?.popViewController(animated: false)
    (self.navigationController as? ViewController)?.showMenuButton()
  }
  
  func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    if searchText.isEmpty {
      self.isSearching = false
    } else {
      self.isSearching = true
      self.filteredApps = []
      for appTitle in self.downloadedAppsMap.keys {
        if appTitle.lowercased().contains(searchText.lowercased()) {
          self.filteredApps.append(self.downloadedAppsMap[appTitle]!)
        }
      }
    }
    self.tableView.reloadData()
  }
}
