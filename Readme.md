Bitcoin prediction service

API end points:
GET /v1/historicalprices 
Example: 
GET /v1/historicalprices?duration=week
GET /v1/historicalprices?duration=month
GET /v1/historicalprices?date=2018-01-02
GET /v1/historicalprices?date=2017-06-02


GET /v1/historicalpricesbyinterval
Example:
GET /v1/historicalpricesbyinterval?startDate=2018-02-01&endDate=2018-03-02

GET /v1/forecast
Example:
GET /v1/forecast?days=12
GET /v1/forecast?days=15

GET /v1/movingaverage
Example:
GET /v1/movingaverage?startDate=2018-02-01&endDate=2018-03-02&movingAverageNumber=6
GET /v1/movingaverage?startDate=2018-02-01&endDate=2018-03-02&movingAverageNumber=7