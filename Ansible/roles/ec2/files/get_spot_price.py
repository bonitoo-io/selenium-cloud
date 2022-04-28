#!/usr/bin/python

import boto3
import argparse
import sys

parser = argparse.ArgumentParser(description='Process some inputs.')
parser.add_argument('--instance-type', action='store', dest='instance_type', help='EC2 instance type')
parser.add_argument('--a-zone', action='store', dest='a_zone', help='Target availability zone')

args = parser.parse_args()

client = boto3.client('ec2')

prices = client.describe_spot_price_history(InstanceTypes=[args.instance_type],
                                            MaxResults=1,
                                            ProductDescriptions=['Linux/UNIX (Amazon VPC)'],
                                            AvailabilityZone=args.a_zone)

sys.stdout.write(prices['SpotPriceHistory'][0]['SpotPrice'].strip())
sys.stdout.flush()
