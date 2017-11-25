Assumption :
    - All inputs will be in the format of <date in the epoch format>,<IP>,<Username>,<SUCCESS or FAILURE> as json in payload of request
        Ex: 1507365137,187.218.83.136,John.Smith,SUCCESS
    - All events come in order by time. So I have search for failure logins during period of 5 minutes in past not including extra 5 minutes in future.
      If needed to receive them without order, then it's fine only we need to add extra filter and consider timestamp of event + 300 sec to check it as well

PreRequists as on mac:
      brew install sbt
      brew install cassandra
  Steps to Run :
  1- Install Cassandra DB : I selected as it will be easy handle millions of recoreds of logs per clustered DB and parition keys
  2- Create Schema using file Schema.txt located at "./HackerDetectorService/conf/cql"
  3- This is Intellij project created by Playframwork and you can run it by calling commands
    -> sbt clean cleanFiles update compile
    then
    -> sbt run
    
  4- you can test endpoint by either postman or curl command as follow :
    curl -X POST http://localhost:9000/parselogline -H 'content-type: application/json' -d '"1907365745,187.218.83.190,John.Smith,FAILURE"'
  
  5- I have added endpoint /test which run about 1000 parallel thread to hit server to make sure all requests are processed fine.
  