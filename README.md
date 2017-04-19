# FTP-Server-Client

multi-threaded FTP client-server for windows. The client can connect via command prompt, access the server, manage directories
and transfer files. 

How to run:

Run ServerMain.java

Open windows command prompt and type: "ftp localhost"

Server will ask for username/pasword: For testing purposes it will allow any username and password(they must be the same, eg: a / a)

After logging into the server, some possible commands:

 "cd dirNname" go into directory
 
 "cd .." goes back
 
 "mkdir dirName" creates directory
 
 "delete dirName" deletes directory
 
 "get filename"
 
 "put filename"
