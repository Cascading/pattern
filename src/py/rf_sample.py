#!/usr/bin/env python
# encoding: utf-8

import random
import sys
import uuid


debug = False # True

rand_vars = (
    [True, 0, 1, 0],
    [False, 0, 0, 1],
    [True, 1, 1, 0],
    [False, 1, 0, 0]
)

MAX_COUNT = 10
COIN_FLIP_THRESHOLD = 0.9


if __name__ == '__main__':
    MAX_COUNT = int(sys.argv[1])

    headers = map(lambda x: "var" + str(x), range(0, len(rand_vars[0]) - 1))
    headers.insert(0, "label")
    headers.append("order_id")
    print "\t".join(headers)

    for i in range(0, MAX_COUNT):
        var = random.choice(rand_vars)

        if debug:
            print var

        label = var[0]
        var = var[1:]
        coin_flip = random.random()
        order_id = str(uuid.uuid1()).split("-")[0]

        if label:
            label_val = 1
        else:
            label_val = 0

        if coin_flip >= COIN_FLIP_THRESHOLD:
            label_val = abs(1 - label_val)

        print "\t".join(map(lambda x: str(x), [label_val] + var + [order_id]))

