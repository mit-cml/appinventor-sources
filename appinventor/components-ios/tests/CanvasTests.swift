import XCTest
@testable import AIComponentKit

class CanvasTests: AppInventorTestCase {
  var canvas: Canvas!
  var view: CanvasView!

  override func setUp() {
    super.setUp()
    super.setUp()
    canvas = Canvas(form)
    view = canvas.canvasView
    // Force layout to set bounds
    view.frame = CGRect(x: 0, y: 0, width: 100, height: 100)
    canvas.Width = 100
    canvas.Height = 100
    canvas.BackgroundColor = Int32(bitPattern: 0xFFFF0000) // Red (ARGB)
  }

  func testDrawing() {
    // Draw a blue line
    canvas.PaintColor = Int32(bitPattern: 0xFF0000FF) // Blue
    canvas.LineWidth = 10.0
    canvas.DrawLine(0, 0, 100, 100)

    // Check pixel at 50,50 (midpoint of line)
    // Should be Blue
    let pixelColor = canvas.GetBackgroundPixelColor(50, 50)
    XCTAssertEqual(pixelColor, Int32(bitPattern: 0xFF0000FF), "Pixel at 50,50 should be Blue")
    
    // Check pixel at 0, 100 (not on line)
    // Should be Red (Background)
    let bgColor = canvas.GetBackgroundPixelColor(0, 100)
    XCTAssertEqual(bgColor, Int32(bitPattern: 0xFFFF0000), "Pixel at 0,100 should be Red")
  }

  func testErasing() {
    // 1. Fill entire canvas with Blue
    canvas.PaintColor = Int32(bitPattern: 0xFF0000FF) // Blue
    canvas.LineWidth = 200.0 // Large enough to cover
    canvas.DrawLine(50, 50, 50, 50) // Draw a huge dot/line covering everything
    
    let midColor = canvas.GetBackgroundPixelColor(50, 50)
    XCTAssertEqual(midColor, Int32(bitPattern: 0xFF0000FF), "Canvas should be Blue initially")

    // 2. Erase the center with Transparent color
    canvas.PaintColor = Int32(bitPattern: 0x00FFFFFF) // Transparent (Alpha 0)
    canvas.LineWidth = 20.0
    canvas.DrawLine(0, 0, 100, 100) // Cut a line through it

    // 3. Check pixel at 50,50 (on the cut line)
    // Should be Red (Background Color) because Blue was erased
    let erasedColor = canvas.GetBackgroundPixelColor(50, 50)
    XCTAssertEqual(erasedColor, Int32(bitPattern: 0xFFFF0000), "Pixel at 50,50 should be erased to Red background")
  }
  
  func testDrawPointRespectsLineWidth() {
    canvas.PaintColor = Int32(bitPattern: 0xFF00FF00) // Green
    canvas.LineWidth = 20.0
    canvas.DrawPoint(50, 50)
    
    // Check center -> Green
    let centerColor = canvas.GetBackgroundPixelColor(50, 50)
    XCTAssertEqual(centerColor, Int32(bitPattern: 0xFF00FF00))
    
    // Check radius edge (approx 9px away) -> Green
    let edgeColor = canvas.GetBackgroundPixelColor(59, 50)
    XCTAssertEqual(edgeColor, Int32(bitPattern: 0xFF00FF00))
    
    // Check outside radius (approx 11px away) -> Red
    let outsideColor = canvas.GetBackgroundPixelColor(61, 50)
    XCTAssertEqual(outsideColor, Int32(bitPattern: 0xFFFF0000))
  }
}
