import XCTest
import RealityKit
import ARKit
@testable import AIComponentKit

@available(iOS 14.0, *)
class ARView3DTests: AppInventorTestCase {
    
    var arView: ARView3D!
    var mockContainer: MockComponentContainer!
    
    override func setUpWithError() throws {
      super.setUp()
        
      mockContainer = MockComponentContainer(container: nil, Width: 480, Height: 768, form: form)
      arView = ARView3D(mockContainer)
    }
    
    override func tearDownWithError() throws {
        // Clean up
        arView?.onDelete()
        arView = nil
        mockContainer = nil
        
        try super.tearDownWithError()
    }
    
    // MARK: - Configuration Tests
    
    func testInitialConfiguration() throws {
        XCTAssertNotNil(arView)
      XCTAssertFalse(arView.IsSessionRunning)
        XCTAssertEqual(arView.TrackingType, 1) // world tracking
    }
    
    func testSceneReconstructionEnabled() throws {
        guard let config = arView.CurrentConfig as? ARWorldTrackingConfiguration else {
            XCTFail("Configuration should be ARWorldTrackingConfiguration")
            return
        }
        
      XCTAssertNotEqual(config.sceneReconstruction, nil)
    }
    
    func testDebugOptionsInitialState() throws {
        // Initially should be empty
        XCTAssertTrue(arView._arView.debugOptions.isEmpty)
    }
    
    // MARK: - Debug Options Tests
    
    func testShowWireframesToggle() throws {
        // Initially false
        XCTAssertFalse(arView.ShowWireframes)
        
        // Enable
        arView.ShowWireframes = true
        XCTAssertTrue(arView.ShowWireframes)
        
        // Disable
        arView.ShowWireframes = false
        XCTAssertFalse(arView.ShowWireframes)
    }
    
    func testDebugOptionsRawValues() throws {
        let expectation = XCTestExpectation(description: "Debug options applied")
        
        arView.ShowWireframes = true
        arView.reapplyDebugOptions()
        
        // Wait for async application
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            let hasSceneUnderstanding = self.arView._arView.debugOptions.contains(.showSceneUnderstanding)
            XCTAssertTrue(hasSceneUnderstanding, "Scene understanding should be enabled")
            expectation.fulfill()
        }
        
        wait(for: [expectation], timeout: 2.0)
    }
    
    // MARK: - Session Lifecycle Tests
    
    func testSessionStartStop() throws {
        let startExpectation = XCTestExpectation(description: "Session started")
        
        arView.StartTracking()
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            XCTAssertTrue(self.arView.IsSessionRunning)
            startExpectation.fulfill()
        }
        
        wait(for: [startExpectation], timeout: 3.0)
        
        // Test pause
        arView.PauseTracking()
        XCTAssertFalse(arView.IsSessionRunning)
    }
    
    func testResetTracking() throws {
        let expectation = XCTestExpectation(description: "Reset completed")
        
        // Start session
        arView.StartTracking()
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            // Reset
            self.arView.ResetTracking()
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                // Should be running again
                XCTAssertTrue(self.arView.IsSessionRunning)
                expectation.fulfill()
            }
        }
        
        wait(for: [expectation], timeout: 5.0)
    }
    
    // MARK: - Floor Tests
    
    func testInvisibleFloorCreation() throws {
        arView.ensureFloorExists()
        XCTAssertTrue(arView.hasInvisibleFloor)
        XCTAssertNotNil(!arView.InvisibleFloorIsNil)
    }
    
    func testFloorRemoval() throws {
        arView.ensureFloorExists()
        XCTAssertTrue(arView.hasInvisibleFloor)
        
        arView.removeInvisibleFloor()
        XCTAssertFalse(arView.hasInvisibleFloor)
        XCTAssertNil(!arView.InvisibleFloorIsNil)
    }
    
    // MARK: - Node Management Tests
    
    func testAddNode() throws {
        let sphere = SphereNode(arView)
        sphere.Initialize()
        
        arView.addNode(sphere)
        
        XCTAssertTrue(arView.CurrentAnchors.keys.contains(sphere))
    }
    
    func testRemoveNode() throws {
        let sphere = SphereNode(arView)
        sphere.Initialize()
        
        arView.addNode(sphere)
        XCTAssertTrue(arView.CurrentAnchors.keys.contains(sphere))
        
        arView.removeNode(sphere)
        XCTAssertFalse(arView.CurrentAnchors.keys.contains(sphere))
    }
}

// MARK: - Mock Container

class MockComponentContainer: ComponentContainer {

  var children: [any AIComponentKit.Component] = []
  var visible: Bool = true
  var containerId: String?
  var Width: Int32
  var Height: Int32
  var form: Form? = nil
  
  init(container: (any AIComponentKit.ComponentContainer)? = nil, Width: Int32, Height: Int32, form: Form? = nil) {
    self.container = container
    self.Width = Width
    self.Height = Height
    self.form = form
  }
  var container: (any AIComponentKit.ComponentContainer)?
  
  func isVisible(component: AIComponentKit.ViewComponent) -> Bool {
    return true
  }
  
  func setVisible(component: AIComponentKit.ViewComponent, to visibility: Bool) {
    visible = visibility
  }
  
  func getChildren() -> [any AIComponentKit.Component] {
    return children
  }
  

  func add(_ component: ViewComponent) {}
  func setChildWidth(of component: ViewComponent, to width: Int32) {}
  func setChildHeight(of component: ViewComponent, to height: Int32) {}
}
