'''
Created on 16.04.2021

@author: lukas
'''
import sqlite3

conn = sqlite3.connect("../../plane.db")
cursor = conn.cursor()

sql = '''SELECT * FROM planes ORDER BY ID;'''

data = cursor.execute(sql)

for a in data:
    print(a)
