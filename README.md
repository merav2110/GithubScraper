# GithubScraper
scrape the 5 pages results of github website. Looking for the string "selenium" and taking the results into mySQL on docker

Hello,

prerequisite:
1. Download chrome driver for Mac or Windows from https://www.seleniumhq.org/  
The application will look for it under user.dir/chromedriver for mac and user.dir\\chromedriver.exe for windows.
2. From docker:
docker run --name=mysqlnew -d mysql/mysql-server
3. From MySQL command line
USE results;
CREATE TABLE selenium (title VARCHAR(60), description VARCHAR(120), tags VARCHAR(50), time VARCHAR(30), language VARCHAR(20), stars VARCHAR(15));
4. Put the jar in the folder and run it
java -jar Githubscraper.jar
5. Links validation, and timing and reporting to the console.

Enjoy :)
Merav
