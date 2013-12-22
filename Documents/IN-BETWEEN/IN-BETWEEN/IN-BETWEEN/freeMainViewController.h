//
//  freeMainViewController.h
//  IN-BETWEEN
//
//  Created by Ki Hyun Won on 11/12/13.
//  Copyright (c) 2013 Ki Hyun Won. All rights reserved.
//

#import "freeFlipsideViewController.h"

@interface freeMainViewController : UIViewController <freeFlipsideViewControllerDelegate, UIPopoverControllerDelegate>

@property (strong, nonatomic) UIPopoverController *flipsidePopoverController;

@end
