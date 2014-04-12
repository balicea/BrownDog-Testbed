##Overview
The CI-BER Brown Dog Testbed is a web application that periodically runs a battery of tests on services offered by Brown Dog. The results are analyzed and presented, so that problems can be identified by the Brown Dog development team.

The Testbed samples the diverse formats in the CI-BER collection to find those meeting a pre-defined test data profile. Tests are run in parallel to simulate future server load and each test result is recorded in a database, as well as overall performance of the test battery. In this way the system allows diagnosis of individual service failures and performance under high load.

So far we have finished the CI-BER test data provider, the test code for Polyglot services (DAP) and the recording of test outcomes to MongoDB. We are able to runs tests against the DAP and record the results.

A dashboard is under development that will present trends in service performance and recent test runs with links to more detailed reports and logs. The dashboard will also allow specification of the data profile for future test batteries.

The Testbed is currently running parallel tests from a single server, but there is a plan to distribute test jobs to servers and better simulate real world conditions. The Testbed will also be extended to test Brown Dog DTS in a similar fashion.

##Technologies

* Spring DI
* MongoDB
* JAX-RS
* JQuery + JSON services
* D3 visualizations
