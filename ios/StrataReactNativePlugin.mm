#import "StrataReactNativePlugin.h"
#import <UIKit/UIKit.h>

@implementation StrataReactNativePlugin

RCT_EXPORT_MODULE()

- (NSDictionary *)constantsToExport
{
  return @{ @"platform": @"ios" };
}

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

RCT_EXPORT_METHOD(getDeviceInfo:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  UIDevice *device = [UIDevice currentDevice];
  NSString *deviceType = (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) ? @"tablet" : @"mobile";
  
  resolve(@{
    @"platform": @"ios",
    @"deviceType": deviceType,
    @"model": device.model,
    @"systemName": device.systemName,
    @"systemVersion": device.systemVersion,
    @"name": device.name
  });
}

RCT_EXPORT_METHOD(triggerHaptic:(NSString *)intensity
                  resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  if (@available(iOS 10.0, *)) {
    UIImpactFeedbackStyle style = UIImpactFeedbackStyleMedium;
    if ([intensity isEqualToString:@"light"]) {
      style = UIImpactFeedbackStyleLight;
    } else if ([intensity isEqualToString:@"heavy"]) {
      style = UIImpactFeedbackStyleHeavy;
    }
    
    UIImpactFeedbackGenerator *generator = [[UIImpactFeedbackGenerator alloc] initWithStyle:style];
    [generator prepare];
    [generator impactOccurred];
    resolve(@YES);
  } else {
    resolve(@NO);
  }
}

RCT_EXPORT_METHOD(setOrientation:(NSString *)orientation)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    UIInterfaceOrientation orientationValue = UIInterfaceOrientationUnknown;
    if ([orientation isEqualToString:@"portrait"]) {
      orientationValue = UIInterfaceOrientationPortrait;
    } else if ([orientation isEqualToString:@"landscape"]) {
      orientationValue = UIInterfaceOrientationLandscapeLeft;
    }
    
    if (@available(iOS 16.0, *)) {
      UIWindowScene *windowScene = (UIWindowScene *)[[[UIApplication sharedApplication] connectedScenes] anyObject];
      if (windowScene) {
        UIInterfaceOrientationMask mask = UIInterfaceOrientationMaskAll;
        if (orientationValue == UIInterfaceOrientationPortrait) {
          mask = UIInterfaceOrientationMaskPortrait;
        } else if (orientationValue == UIInterfaceOrientationLandscapeLeft) {
          mask = UIInterfaceOrientationMaskLandscape;
        }
        
        #pragma clang diagnostic push
        #pragma clang diagnostic ignored "-Wdeprecated-declarations"
        UIViewController *rootViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
        #pragma clang diagnostic pop
        
        [rootViewController setNeedsUpdateOfSupportedInterfaceOrientations];
        
        UIWindow *window = windowScene.windows.firstObject;
        [window.rootViewController setNeedsUpdateOfSupportedInterfaceOrientations];
      }
    } else {
      [[UIDevice currentDevice] setValue:@(orientationValue) forKey:@"orientation"];
      [UIViewController attemptRotationToDeviceOrientation];
    }
  });
}

RCT_EXPORT_METHOD(getPerformanceMode:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  NSString *mode = @"high";
  BOOL isLowPowerMode = NO;
  
  if (@available(iOS 9.0, *)) {
    isLowPowerMode = [NSProcessInfo processInfo].isLowPowerModeEnabled;
  }
  
  if (isLowPowerMode) {
    mode = @"low";
  } else {
    // Check RAM (approximate)
    unsigned long long physicalMemory = [NSProcessInfo processInfo].physicalMemory;
    if (physicalMemory < 2ULL * 1024 * 1024 * 1024) { // Less than 2GB
      mode = @"low";
    } else if (physicalMemory < 4ULL * 1024 * 1024 * 1024) { // Less than 4GB
      mode = @"medium";
    }
  }
  
  resolve(@{
    @"mode": mode,
    @"isLowPowerMode": @(isLowPowerMode),
    @"totalMemory": @([NSProcessInfo processInfo].physicalMemory)
  });
}

RCT_EXPORT_METHOD(getSafeAreaInsets:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    UIEdgeInsets insets = UIEdgeInsetsZero;
    if (@available(iOS 11.0, *)) {
      UIWindow *window = [UIApplication sharedApplication].keyWindow;
      insets = window.safeAreaInsets;
    }
    
    resolve(@{
      @"top": @(insets.top),
      @"right": @(insets.right),
      @"bottom": @(insets.bottom),
      @"left": @(insets.left)
    });
  });
}

@end
