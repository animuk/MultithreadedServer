The server is designed via multi threaded mode and configured to run on 9000 port.
The port could be configured from the MainServer class.

Architecture is as follows below:

 										create new thread
 										to service the req
Client ----------> Server----------------- ----------------> Worker Thread
                  |____|
                  resume listening
                  to other client
                  request
                  
File Store:

1. 914bbs.txt
2. testimage.jpg
3. love.jpg
4. Architecture.png

Some sample request:

http://<IP Address>:9000/testimage.jpg
http://<IP Address>:9000/914bbs.txt
http://<IP Address>:9000/love2.jpg

You need to run the MainServer class to make the server alive for 200 second.