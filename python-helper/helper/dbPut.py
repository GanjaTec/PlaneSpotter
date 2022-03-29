'''
Created on 17.04.2021

@author: lukas
'''
import sqlite3
import csv

conn = sqlite3.connect("plane.db")
cursor = conn.cursor()
print("Populating Table \"airports\"...")
with open('airport-codes.csv', newline='\n') as csvfile:
    reader = csv.DictReader(csvfile, delimiter=',')
    for row in reader:
        if row["iata_code"] in (None, ""):
            pass
        else:
            values = (None, row["iata_code"],row["name"],row["iso_country"],row["coordinates"])
            print("| {} | {} | {} | {} | {} |".format(*values))
            sql = '''INSERT INTO airports VALUES (?, ?, ?, ?, ?)'''
            cursor.execute(sql, values)
            
conn.commit()
conn.close()