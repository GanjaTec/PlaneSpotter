'''
Created on 16.04.2021

@author: lukas
'''
import requests

url = "https://data-live.flightradar24.com/zones/fcgi/feed.js?faa=1&bounds=54.241%2C48.576%2C-14.184%2C13.94&satellite=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=0&estimated=1&maxage=14400&gliders=0&stats=0"
headers = {"User-Agent" : "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0"}

r = requests.get(url, headers=headers)
print(r.json())
print(r)