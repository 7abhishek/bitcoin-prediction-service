Bitcoin prediction service

**API end points**
<br/>
**GET /v1/historicalprices**
<br/>
Examples: 
<br/>
GET /v1/historicalprices?duration=week
<br/>
GET /v1/historicalprices?duration=month
<br/>
GET /v1/historicalprices?date=2018-01-02
<br/>
GET /v1/historicalprices?date=2017-06-02


**GET /v1/historicalpricesbyinterval**
<br/>
Examples:
<br/>
GET /v1/historicalpricesbyinterval?startDate=2018-02-01&endDate=2018-03-02
<br/>

**GET /v1/forecast**
Example:
<br/>
GET /v1/forecast?days=12
<br/>
GET /v1/forecast?days=15
<br/>

**GET /v1/movingaverage**
<br/>
Example:
<br/>
GET /v1/movingaverage?startDate=2018-02-01&endDate=2018-03-02&movingAverageNumber=6
<br/>
GET /v1/movingaverage?startDate=2018-02-01&endDate=2018-03-02&movingAverageNumber=7
<br/>
