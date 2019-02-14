// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import AVKit

public func setCategory(_ category: AVAudioSession.Category, for session: AVAudioSession, mode: AVAudioSession.Mode = .default) {
  if #available(iOS 10.0, *) {
    do {
      try session.setCategory(category, mode: mode)
      return;
    } catch {
      // The documentation doesn't say why this can throw, so we'll fall through to the old implementation
    }
  }
  let sel = NSSelectorFromString("setCategory:error:")
  if session.responds(to: sel) {
    session.performSelector(onMainThread: sel, with: category, waitUntilDone: true)
  }
}
