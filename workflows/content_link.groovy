def path = execution.getVariable("path")
def startDate = execution.getVariable("startDate")
def days = execution.getVariable("days")

def days_url = ""
def format = new java.text.SimpleDateFormat("yyyyMMdd")
def cal = java.util.Calendar.getInstance();
cal.setTime(format.parse(startDate));
for (i = 0; i < days; i++) {
    days_url = days_url + '%EE%80%80' + format.format(cal.getTime())
    cal.add(Calendar.DATE, 1);  
}

def data = [
  "df4":"include%EE%80%800%EE%80%80IN%EE%80%80${path}",
  "df5":"include%EE%80%800%EE%80%80IN${days_url}"
]

def data_url = java.net.URLEncoder.encode(groovy.json.JsonOutput.toJson(data), "UTF-8")

def url = "https://datastudio.google.com/reporting/1XAGzyAcXG2wLSaKqe1tBsmmzUVoqvktl/page/Kq7UB?params=${data_url}"

println("URL: " + url)