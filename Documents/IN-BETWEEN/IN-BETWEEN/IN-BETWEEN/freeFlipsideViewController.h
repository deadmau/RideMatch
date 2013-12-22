//
//  freeFlipsideViewController.h
//  IN-BETWEEN
//
//  Created by Ki Hyun Won on 11/12/13.
//  Copyright (c) 2013 Ki Hyun Won. All rights reserved.
//

#import <UIKit/UIKit.h>

@class freeFlipsideViewController;

@protocol freeFlipsideViewControllerDelegate
- (void)flipsideViewControllerDidFinish:(freeFlipsideViewController *)controller;
@end

@interface freeFlipsideViewController : UIViewController

@property (weak, nonatomic) id <freeFlipsideViewControllerDelegate> delegate;

- (IBAction)done:(id)sender;

@end
