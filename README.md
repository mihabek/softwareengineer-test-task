### What you need for this application
1. java 11
2. gradle 7

### Run application
Open new window from command line and navigate to project root folder. Run:
1. `./gradlew clean build` to build files needed for this application
2. `./gradlew bootRun` to start application

The application accepts gRPC and REST requests
- gRPC calls are available through port: 9090
- REST calls are available through port: 8080

### Testing application with gRPC
Open new command line window or tab and run:
1. `./gradlew installDist` to compile client for testing gRPC requests
2. `./build/install/scoring-api/bin/scoring-client` to run client that executes scoring-api requests through gRPC
3. you can see and update request params for gRPC calls in file `GrpcClient.java`

### Testing application with REST
1. for executing REST calls you can use CURL or Postman
2. available services:
   - category scores: `localhost:8080/score/categories?startDate=2020-01-01&endDate=2020-02-01`
   - ticket scores: `localhost:8080/score/tickets?startDate=2020-01-01&endDate=2020-02-01`
   - overall score: `localhost:8080/score/overall?startDate=2020-01-01&endDate=2020-02-01`
   - overall score change: `localhost:8080/score/overall-change?startDatePrevious=2020-01-01&endDatePrevious=2020-02-01&startDateSelected=2020-02-01&endDateSelected=2020-03-01`

# Software Engineer Test Task

As a test task for [Klaus](https://www.klausapp.com) software engineering position we ask our candidates to build a small [gRPC](https://grpc.io) service using language of their choice. Prefered language for new services in Klaus is [Go](https://golang.org).

The service should be using provided sample data from SQLite database (`database.db`).

Please fork this repository and share the link to your solution with us.

### Tasks

1. Come up with ticket score algorithm that accounts for rating category weights (available in `rating_categories` table). Ratings are given in a scale of 0 to 5. Score should be representable in percentages from 0 to 100. 

2. Build a service that can be queried using [gRPC](https://grpc.io/docs/tutorials/basic/go/) calls and can answer following questions:

    * **Aggregated category scores over a period of time**
    
        E.g. what have the daily ticket scores been for a past week or what were the scores between 1st and 31st of January.

        For periods longer than one month weekly aggregates should be returned instead of daily values.

        From the reponse the following UI representation should be possible:

        | Category | Ratings | Date 1 | Date 2 | ... | Score |
        |----|----|----|----|----|----|
        | Tone | 1 | 30% | N/A | N/A | X% |
        | Grammar | 2 | N/A | 90% | 100% | X% |
        | Random | 6 | 12% | 10% | 10% | X% |

    * **Scores by ticket**

        Aggregate scores for categories within defined period by ticket.

        E.g. what aggregate category scores tickets have within defined rating time range have.

        | Ticket ID | Category 1 | Category 2 |
        |----|----|----|
        | 1   |  100%  |  30%  |
        | 2   |  30%  |  80%  |

    * **Overal quality score**

        What is the overall aggregate score for a period.

        E.g. the overall score over past week has been 96%.

    * **Period over Period score change**

        What has been the change from selected period over previous period.

        E.g. current week vs. previous week or December vs. January change in percentages.


### Bonus

* How would you build and deploy the solution?

    At Klaus we make heavy use of containers and [Kubernetes](https://kubernetes.io).
