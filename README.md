# recommendation

To run it on your machine you need Kafka and Docker (+ docker-compose), postgresql database installed.

## **PRELIMINARIES**
1. Run kafka, then specify kafka address in application.yaml of recommendation-service
2. Run redis - `cd redis && docker-compose up -d`, then specify redis address in application.yaml of recommendation-service 
3. Create database, specify connection url, username and password in environmental variables.
4. Download combined_data_1.txt from https://www.kaggle.com/netflix-inc/netflix-prize-data and place it into resource directory of recommendation-algorithm, rename it to 'netflix.txt'

Model loading is configured by properties in application.yaml of recommendation-service.

You have 2 options to build the model:
1. Set model.initializer.source to NETFLIX - model will be loaded from NetflixPrize dataset.
2. Set model.initizlizer.source to DATABASE - model will be loaded from database (if it was saved before)
