Bitcoin prediction service

**API end points**
<br/>
**GET /v1/historicalprices**
<br/>
**params**
<br/>
duration = week, month, year etc
<br/>
date = 2018-03-01 (yyyy-MM-dd)
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
**params**
<br/>
startDate = 2018-02-01 (yyyy-MM-dd)
<br/>
endDate = 2018-03-02 (yyyy-MM-dd)
<br/>
Examples:
<br/>
GET /v1/historicalpricesbyinterval?startDate=2018-02-01&endDate=2018-03-02
<br/>

**GET /v1/forecast**
<br/>
**params**
<br/>
days = number of days the forecast needs to be done.
<br/>
Examples:
<br/>
GET /v1/forecast?days=12
<br/>
GET /v1/forecast?days=15
<br/>

**GET /v1/movingaverage**
<br/>
**params**
<br/>
startDate = 2018-02-01 (yyyy-MM-dd)
<br/>
endDate = 2018-03-02 (yyyy-MM-dd)
<br/>
movingAverageNumber = number of terms to be considered for averaging in moving average method
<br/>
Example:
<br/>
GET /v1/movingaverage?startDate=2018-02-01&endDate=2018-03-02&movingAverageNumber=6
<br/>
GET /v1/movingaverage?startDate=2018-02-01&endDate=2018-03-02&movingAverageNumber=7
<br/>
