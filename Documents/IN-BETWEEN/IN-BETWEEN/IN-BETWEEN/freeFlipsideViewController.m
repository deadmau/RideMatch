//
//  freeFlipsideViewController.m
//  IN-BETWEEN
//
//  Created by Ki Hyun Won on 11/12/13.
//  Copyright (c) 2013 Ki Hyun Won. All rights reserved.
//

#import "freeFlipsideViewController.h"

@interface freeFlipsideViewController ()

@end

@implementation freeFlipsideViewController

- (void)awakeFromNib
{
    self.preferredContentSize = CGSizeMake(320.0, 480.0);
    [super awakeFromNib];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Actions

- (IBAction)done:(id)sender
{
    [self.delegate flipsideViewControllerDidFinish:self];
}

@end
