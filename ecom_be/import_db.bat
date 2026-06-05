@echo off
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot ecom_db < "D:\Desktop\ecom\ecom_be\ecom_schema.sql"
echo Import done with exit code: %errorlevel%
