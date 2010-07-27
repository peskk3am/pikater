CREATE TABLE fileMapping (userID INTEGER NOT NULL, externalFilename VARCHAR(256) NOT NULL, internalFilename CHAR(32) NOT NULL, PRIMARY KEY (userID, externalFilename))
CREATE TABLE results (agentName VARCHAR (256), agentType VARCHAR (256), options VARCHAR (256), dataFile VARCHAR (50), testFile VARCHAR (50), errorRate DOUBLE)
