'''
Created on 16.04.2021

@author: lukas
'''
import sqlite3
import csv
import time

buildt1= time.time()
conn = sqlite3.connect('planeTest.db')

cursor = conn.cursor()
print ("Created database successfully!")

#For now i handle the fields mostly as Strings in order to simplify debugging, when i reach a certain point in development i will switch to use proper data types for the database

conn.execute('''CREATE TABLE airlines
         (ID INTEGER PRIMARY KEY AUTOINCREMENT,
         icaotag          CHAR(50) NOT NULL,
         name             CHAR(50),
         country          CHAR(50)
         );''')
print("Table \"airlines\" successfully created!")
print("    Populating table \"airlines\"")
t1 = time.time()
i = 0
with open('airline-codes.csv', newline='\n', encoding="utf8") as csvfile:
    reader = csv.DictReader(csvfile, delimiter=',')
    for row in reader:
        if row["icao"] in (None, ""):
            pass
        else:
            values = (None, row["icao"],row["name"],row["country"])
            #print("| {} | {} | {} | {} |".format(*values))
            sql = '''INSERT INTO airlines VALUES (?, ?, ?, ?)'''
            cursor.execute(sql, values)
            i = i+1
    t2 = time.time()
    tdiff = t2-t1    
print("        Inserted {} datasets in {} seconds!".format(i, str(tdiff)))

conn.execute('''CREATE TABLE airports
         (ID INTEGER PRIMARY KEY AUTOINCREMENT,
         iatatag          CHAR(50) NOT NULL,
         name             CHAR(50),
         country          CHAR(50),
         lat              REAL,
         lon              REAL
         );''')
print("Table \"airports\" successfully created!")
print("    Populating table \"airports\"...")
t1 = time.time()
i=0
with open('airport-codes.csv', newline='\n', encoding="utf8") as csvfile:
    reader = csv.DictReader(csvfile, delimiter=',')
    for row in reader:
        if row["iata_code"] in (None, ""):
            pass
        else:
            coords = row["coordinates"].split(", ")
            values = (None, row["iata_code"],row["name"],row["iso_country"], coords[0], coords[1])
            #print("| {} | {} | {} | {} | {} |".format(*values))
            sql = '''INSERT INTO airports VALUES (?, ?, ?, ?, ?, ?)'''
            cursor.execute(sql, values)
            i = i+1
    t2 = time.time()
    tdiff = t2-t1
print("        Inserted {} datasets in {} seconds!".format(i, str(tdiff)))

#this table is honestly kinda unnessecary for now, i have to manually fill the csv doc that is needed for populating the table
conn.execute('''CREATE TABLE type
         (ID INTEGER PRIMARY KEY AUTOINCREMENT,
         short            TEXT,
         longn            TEXT,
         manufacturer     TEXT,
         technical        TEXT
         );''')
print("Table \"type\" successfully created!")
print("    Populating Table \"type\"")
t1 = time.time()
i = 0
with open('airplane-types.csv', newline='\n', encoding="utf8") as csvfile:
    reader = csv.DictReader(csvfile, delimiter=',')
    for row in reader:
        if row["shortn"] in (None, ""):
            pass
        else:
            values = (None, row["shortn"],row["longn"],row["manufacturer"], row["technical"])
            #print("| {} | {} | {} | {} |".format(*values))
            sql = '''INSERT INTO type VALUES (?, ?, ?, ?, ?)'''
            cursor.execute(sql, values)
            i = i+1
    t2 = time.time()
    tdiff = t2-t1    
print("        Inserted {} datasets in {} seconds!".format(i, str(tdiff)))

conn.execute('''CREATE TABLE planes
         (ID INTEGER PRIMARY KEY AUTOINCREMENT,
         tailnr           TEXT,
         icaonr           TEXT,
         registration     TEXT,
         type             TEXT,
         airline          TEXT REFERENCES airlines(icaotag)
         );''')
print("Table \"planes\" successfully created!")

conn.execute('''CREATE TABLE flights
         (ID INTEGER PRIMARY KEY AUTOINCREMENT,
         plane            TEXT REFERENCES planes(icaonr),
         src              TEXT REFERENCES airports(icaotag),
         dest             TEXT REFERENCES airports(icaotag),
         flightnr         TEXT,
         callsign         TEXT,
         start            TEXT,
         endTime          TEXT
         );''')
print("Table \"flights\" successfully created!")

conn.execute('''CREATE TABLE tracking
         (ID INTEGER PRIMARY KEY AUTOINCREMENT,
         flightid         INTEGER REFERENCES flights(ID),
         latitude         REAL,
         longitude        REAL,
         altitude         TEXT,
         groundspeed      TEXT,
         heading          TEXT,
         squawk           INTEGER,
         timestamp        TEXT
         );''')
print ("Table \"tracking\" successfully created!")
conn.commit()
conn.close()
buildt2 = time.time()
buildtdiff= buildt2-buildt1
print("Built Database in {} seconds".format(buildtdiff))
