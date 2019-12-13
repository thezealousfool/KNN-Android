import json
import os

PATH = os.path.abspath('.')

allOneHot = []

def beaconDataToOneHot(beaconData):
    order = [1,2,3,4,5,6,7,8,11,9,87,12,10,13,14,15,
             16,17,18,19,20,21,41,42,43,44,45,46,47,
             48,49,50,51,52,53,54,188,190,191,194,
             195,196,199,200,201,202,253,89]
    oneHot = [0]*len(order)
    for beacon in beaconData:
        if beacon['major_id'] == '65535' and int(beacon['minor_id']) in order:
            idx = order.index(int(beacon['minor_id']))
            oneHot[idx] = -int(beacon['rssi'])
    return oneHot
    # arrayOf(1,2,3,4) to (2 to 2)

for filename in os.listdir(PATH):
    filename = os.path.join(PATH, filename)
    if not filename.endswith('.json'): continue
    with open(filename, 'r') as jsonFile:
        jsonData = json.load(jsonFile)
        for timestep in jsonData:
            beaconData = timestep['beacon_data']
            oneHot = beaconDataToOneHot(beaconData)
            for x in oneHot:
                print(x, end=",")
            print(':', end="")
            print('{},{}'.format(timestep['pos_x'], timestep['pos_y']))
