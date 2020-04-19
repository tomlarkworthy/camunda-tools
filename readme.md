
# Open API notes



# At least once notes

REST API access

    curl https://camunda-flxotk3pnq-ew.a.run.app/engine-rest/job/

Modler supports bearer token deployment

Problem: Repeat deliverys loop indefinately
Problem: Sequential flows often the delivery fails first
Problem: Parrallel flows sometimes the receive starts first (async before publish)
Problem: Async jobs are not starting (Spin serialization issue)
TODO turn on jobExecutorDeploymentAware?

Problem: Preformance was terrible, 3 seconds to print to console -> switch to groovy
Problem: double sneding (wrong type of gateway)
Problem: Lots of repear deliveries (asyncAfter) optimist locking exceptions 
