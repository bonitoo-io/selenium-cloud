#!/usr/bin/python

import boto3
import argparse
import sys

parser = argparse.ArgumentParser(description='Process some inputs.')
parser.add_argument('-s',
                    '--spot-ids',
                    help='<Required> Set list of SpotInstanceRequestIds delimited by a comma',
                    required=True,
                    type=str,
                    dest='spot_ids')

args = parser.parse_args()
spot_ids_list = [str(item) for item in args.spot_ids.split(',')]

client = boto3.client('ec2')

response = client.cancel_spot_instance_requests(SpotInstanceRequestIds=spot_ids_list)

sys.stdout.write(str(response))
sys.stdout.flush()
