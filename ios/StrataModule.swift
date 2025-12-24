import Foundation
import UIKit
import CoreHaptics

/**
 * Strata React Native module for iOS.
 * 
 * Provides device detection, input handling, and haptic feedback.
 */
@objc(StrataModule)
class StrataModule: NSObject {
    
    private var hapticEngine: CHHapticEngine?
    
    override init() {
        super.init()
        setupHaptics()
    }
    
    private func setupHaptics() {
        guard CHHapticEngine.capabilitiesForHardware().supportsHaptics else { return }
        
        do {
            hapticEngine = try CHHapticEngine()
            try hapticEngine?.start()
        } catch {
            print("Haptics error: \(error)")
        }
    }
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return false
    }
    
    /**
     * Get device profile information.
     */
    @objc
    func getDeviceProfile(_ resolve: @escaping RCTPromiseResolveBlock,
                          reject: @escaping RCTPromiseRejectBlock) {
        DispatchQueue.main.async {
            let screen = UIScreen.main
            
            let result: [String: Any] = [
                "platform": "ios",
                "deviceType": self.getDeviceType(),
                "hasTouch": true,
                "hasGamepad": false, // TODO: detect MFi controller
                "screenWidth": screen.bounds.width,
                "screenHeight": screen.bounds.height,
                "pixelRatio": screen.scale,
                "safeAreaInsets": self.getSafeAreaInsets()
            ]
            
            resolve(result)
        }
    }
    
    /**
     * Trigger haptic feedback.
     */
    @objc
    func triggerHaptics(_ intensity: String,
                        resolve: @escaping RCTPromiseResolveBlock,
                        reject: @escaping RCTPromiseRejectBlock) {
        DispatchQueue.main.async {
            let generator: UIImpactFeedbackGenerator
            
            switch intensity {
            case "heavy":
                generator = UIImpactFeedbackGenerator(style: .heavy)
            case "light":
                generator = UIImpactFeedbackGenerator(style: .light)
            default:
                generator = UIImpactFeedbackGenerator(style: .medium)
            }
            
            generator.prepare()
            generator.impactOccurred()
            
            resolve(nil)
        }
    }
    
    private func getDeviceType() -> String {
        switch UIDevice.current.userInterfaceIdiom {
        case .phone:
            return "mobile"
        case .pad:
            return "tablet"
        default:
            return "mobile"
        }
    }
    
    private func getSafeAreaInsets() -> [String: CGFloat] {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first else {
            return ["top": 0, "right": 0, "bottom": 0, "left": 0]
        }
        
        let insets = window.safeAreaInsets
        return [
            "top": insets.top,
            "right": insets.right,
            "bottom": insets.bottom,
            "left": insets.left
        ]
    }
}
